package edu.uvm.cs275.conversationanalysis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.nio.file.Path;

public class ProcessingActivity extends AppCompatActivity {

    public static final String AUDIO_FILE_NAME = "audio.wav";

    private Conversation mConversation;

    private ImageView mGammatoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        setupPython();

        mGammatoneView = this.findViewById(R.id.image_gammatone);

        mConversation = new Conversation();

        new ProcessAudioTask().execute();

    }

    private class ProcessAudioTask extends AsyncTask<Void, Void, Conversation> {

        @Override
        protected Conversation doInBackground(Void... params) {
            // TODO: adding to database should be done when user taps the save button
            if (processAudio()) {
                ConversationManager.getInstance(getApplicationContext()).addConversation(mConversation);
            }

            return mConversation;
        }

        @Override
        protected void onPostExecute(Conversation c) {
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
        Path imageDir = ConversationManager.getInstance(getApplicationContext()).getImageDir();
        File outFile = mConversation.getImageFile(getApplicationContext()).toFile();
        File inFile = getApplicationContext().getFilesDir().toPath().resolve(AUDIO_FILE_NAME).toFile();
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


}
