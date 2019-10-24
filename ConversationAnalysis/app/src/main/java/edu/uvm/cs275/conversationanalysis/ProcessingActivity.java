package edu.uvm.cs275.conversationanalysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.nio.file.Path;

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

        mGammatoneView = this.findViewById(R.id.image_gammatone);

        mSendButton = this.findViewById(R.id.send_data);
        mSendButton.setOnClickListener((View v) -> {
            // TODO: transition screen to detail view and do upload there
            ConversationManager cm = ConversationManager.getInstance(getApplicationContext());
            cm.addConversation(mConversation);
            ProgressDialog dialog = new ProgressDialog(ProcessingActivity.this);
            dialog.setMessage("Uploading audio...");
            dialog.show();
            if (cm.uploadConversation(mConversation)) {
                Toast.makeText(this, R.string.upload_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.upload_failure, Toast.LENGTH_SHORT).show();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        mCancelButton = this.findViewById(R.id.cancel_data);
        mCancelButton.setOnClickListener((View v) -> {
            mConversation = null;
            Intent intent = MainActivity.newIntent(ProcessingActivity.this, true);
            startActivity(intent);
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
                Toast.makeText(ProcessingActivity.this, R.string.error_processing, Toast.LENGTH_LONG).show();
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
        Path imageDir = ConversationManager.getInstance(getApplicationContext()).getImageDir();
        File outFile = mConversation.getImageFile(getApplicationContext()).toFile();

        if (!inFile.exists()) {
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

        return outFile.exists();
    }

    public static File getAudioFile(Context context) {
        return context.getFilesDir().toPath().resolve(AUDIO_FILE_NAME).toFile();
    }


}
