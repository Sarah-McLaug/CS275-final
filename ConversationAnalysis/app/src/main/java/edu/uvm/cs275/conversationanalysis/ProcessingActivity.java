package edu.uvm.cs275.conversationanalysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ProcessingActivity extends AppCompatActivity {

    public static final String AUDIO_FILE_NAME = "audio.wav";
    public static final String RAW_AUDIO_FILE_NAME = "audio.pcm";
    private static final String TAG = "ProcessingActivity";

    private static final String EXTRA_DURATION = "duration";

    private Conversation mConversation;
    private long mDuration;
    private ImageView mGammatoneView;
    private Button mSendButton;
    private Button mCancelButton;
    private TextView mTimeInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        setupPython();

        mConversation = new Conversation();

        mGammatoneView = this.findViewById(R.id.image_gammatone);

        mSendButton = this.findViewById(R.id.send_data);
        mSendButton.setOnClickListener((View v) -> {
            // save to db
            ConversationManager cm = ConversationManager.getInstance(getApplicationContext());
            cm.addConversation(mConversation);
            // return with OK and conversation
            Intent returnIntent = MainActivity.newReturnIntent(mConversation);
            setResult(MainActivity.RESULT_OK, returnIntent);
            finish();
        });

        mCancelButton = this.findViewById(R.id.cancel_data);
        mCancelButton.setOnClickListener((View v) -> {
            mConversation.getImageFile(getApplicationContext()).toFile().delete();
            mConversation = null;
            Intent returnIntent = MainActivity.newReturnIntent(null);
            setResult(MainActivity.RESULT_CANCELED, returnIntent);
            finish();
        });

        mDuration = getIntent().getLongExtra(EXTRA_DURATION, 0);

        new ProcessAudioTask(this).execute();
    }

    private class ProcessAudioTask extends AsyncTask<Void, Void, Conversation> {
        private ProgressDialog dialog;

        public ProcessAudioTask(ProcessingActivity activity) {
            dialog = new ProgressDialog(activity);
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Processing audio...");
        }

        @Override
        protected Conversation doInBackground(Void... params) {
            if (!processAudio()) {
                mConversation = null;
            }
            return mConversation;
        }

        @Override
        protected void onPostExecute(Conversation c) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (c == null) {
                Intent returnIntent = new Intent();
                setResult(MainActivity.RESULT_FAILURE, returnIntent);
                finish();
                return;
            }

            final File imageFile = c.getImageFile(getApplicationContext()).toFile();
            if (imageFile == null) {
                mGammatoneView.setImageDrawable(null);
            } else {
                Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                mGammatoneView.setImageBitmap(bmp);
            }
        }

    }

    protected void setupPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
    }

    protected boolean processAudio() {
        File audioInputFile = getRawAudioFile(getApplicationContext());

        // start time is random between 0 and max time where length is still correct
        long start = ThreadLocalRandom.current().nextLong(mDuration - ConversationManager.CONVERSATION_LENGTH);

        // convert to wav and trim audio
        String cmd = String.format(
                "-f s16le -ar 44.1k -ac 2 -ss '%s' -t '%s' -i %s -y %s",
                formatDuration(start),
                formatDuration(ConversationManager.CONVERSATION_LENGTH),
                audioInputFile.toString(),
                getAudioFile(getApplicationContext()).toString()
        );
        mConversation.setStartTime(formatDuration(start));
        mConversation.setEndTime(formatDuration(start + 15000));

        Log.d(TAG, "running: " + cmd);
        FFmpeg.execute(cmd);
        int rc = FFmpeg.getLastReturnCode();
        if (rc == FFmpeg.RETURN_CODE_SUCCESS) {
            Log.i(TAG, "Command execution completed successfully.");
        } else if (rc == FFmpeg.RETURN_CODE_CANCEL) {
            Log.i(TAG, "Command execution cancelled by user.");
        } else {
            Log.i(TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            FFmpeg.printLastCommandOutput(Log.INFO);
        }

        File inFile = getAudioFile(getApplicationContext());
        Path imageDir = ConversationManager.getInstance(getApplicationContext()).getImageDir();
        File outFile = mConversation.getImageFile(getApplicationContext()).toFile();

        mTimeInterval = this.findViewById(R.id.time_interval);
        mTimeInterval.setText(mConversation.getInterval());

        if (!inFile.exists()) {
            Log.d("inFile", "The inFile does not exist");
            return false;
        }

        if (!imageDir.toFile().exists()) {
            if (!imageDir.toFile().mkdirs()) {
                return false;
            }
        }

        try {
            Python py = Python.getInstance();
            py.getModule("process_audio").callAttr("main", inFile.getAbsolutePath(), outFile.getAbsolutePath());
        } catch (Exception e) {
            // delete file if created but something went wrong
            outFile.delete();
            return false;
        }

        // delete the files
        inFile.delete();
        audioInputFile.delete();

        return outFile.exists();
    }

    public static File getAudioFile(Context context) {
        return context.getFilesDir().toPath().resolve(AUDIO_FILE_NAME).toFile();
    }

    public static File getRawAudioFile(Context context) {
        return context.getFilesDir().toPath().resolve(RAW_AUDIO_FILE_NAME).toFile();
    }

    public static Intent newIntent(Context context, long duration) {
        Intent intent = new Intent(context, ProcessingActivity.class);
        intent.putExtra(EXTRA_DURATION, duration);
        return intent;
    }

    public static String formatDuration(long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}