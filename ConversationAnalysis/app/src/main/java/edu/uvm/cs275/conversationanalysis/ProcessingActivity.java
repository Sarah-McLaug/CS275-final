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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.nio.file.Path;

import javax.security.auth.callback.Callback;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

public class ProcessingActivity extends AppCompatActivity {

    public static final String AUDIO_FILE_NAME = "audio.wav";

    private Conversation mConversation;
    private ImageView mGammatoneView;
    private Button mSendButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        setupPython();

        // initialize audio converter
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG, "AudioConverter success!");
            }
            @Override
            public void onFailure(Exception error) {
                Log.i(LOG_TAG, "FFmpeg is not supported by device");
            }
        });

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

        mConversation = new Conversation();

        new ProcessAudioTask(this).execute();

    }

    private class ProcessAudioTask extends AsyncTask<Void, Void, Conversation> {
        private ProgressDialog dialog;

        public ProcessAudioTask(ProcessingActivity activity) {
            dialog = new ProgressDialog(activity);
            dialog.show();
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
        File inFile = getAudioFile(getApplicationContext());

        // convert inFile to .wav file
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Log.d("IConvertCallback", "Success!");
            }

            @Override
            public void onFailure(Exception error) {
                Log.d("IConvertCallBack", "Failure.");
            }
        };
        // TODO: figure out why this isn't working

        if (!inFile.exists()) {
            Log.d("inFile", "The inFile does not exist");
            return false;
        }

        AndroidAudioConverter.with(ProcessingActivity.this).setFile(inFile).setFormat(AudioFormat.WAV).setCallback(callback).convert();

        Path imageDir = ConversationManager.getInstance(getApplicationContext()).getImageDir();
        File outFile = mConversation.getImageFile(getApplicationContext()).toFile();

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

        return outFile.exists();
    }

    public static File getAudioFile(Context context) {
        return context.getFilesDir().toPath().resolve(AUDIO_FILE_NAME).toFile();
    }
}