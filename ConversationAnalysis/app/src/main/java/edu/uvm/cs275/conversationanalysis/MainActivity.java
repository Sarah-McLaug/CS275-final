package edu.uvm.cs275.conversationanalysis;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import edu.uvm.cs275.conversationanalysis.service.BackgroundUploadReceiver;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Audio Recording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORDING_DURATION = 15000;
    private static final int SAMPLE_RATE = 44100;

    private static final int PROCESSING_RESULT = 1;
    public static final int RESULT_FAILURE = 2;
    private static final String RESULT_INTENT_UUID = "edu.uvm.cs275.conversationanalysis.conversation_uuid";

    private BottomNavigationView mNavMenu;
    private ImageButton mRecordButton;
    private ImageButton mStopButton;
    private boolean mRecordingActive = false;
    private AudioRecord mRecorder;
    private Handler mRecordHandler;

    private Chronometer timer;

    private ConversationManager mConversationManager;

    private boolean recordPermission = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationManager = ConversationManager.getInstance(this);
        setContentView(R.layout.activity_main);
        scheduleAlarm();

        mNavMenu = findViewById(R.id.bottom_navigation);
        BottomNavigationItemView navigationView = findViewById(R.id.nav_view);

        timer = (Chronometer) findViewById(R.id.chronometer);

        buttonPress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecordButton.setEnabled(true);
        mStopButton.setEnabled(true);
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), BackgroundUploadReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, BackgroundUploadReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // execute background service now, then roughly ever hour
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HOUR, pIntent);
    }

    /* Override the permissions request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                recordPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    public static Intent newReturnIntent(Conversation conversation) {
        Intent intent = new Intent();
        if (conversation != null) {
            intent.putExtra(RESULT_INTENT_UUID, conversation.getUUID());
        }
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROCESSING_RESULT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.successful, Toast.LENGTH_SHORT).show();
                UUID conversationUUID = (UUID) data.getSerializableExtra(RESULT_INTENT_UUID);
                Conversation conversation = ConversationManager.getInstance(getApplicationContext()).getConversation(conversationUUID);
                Intent intent = DetailView.newIntent(this, conversation);
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), R.string.error_recording, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_FAILURE) {
                Toast.makeText(getApplicationContext(), R.string.error_processing, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // starts recording audio
    private void startRecording() {
        // get permission if needed
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        Toast.makeText(MainActivity.this, R.string.record_toast, Toast.LENGTH_SHORT).show();
        mRecordButton.setVisibility(View.INVISIBLE);
        mStopButton.setVisibility(View.VISIBLE);

        // must declare handler from UI thread
        mRecordHandler = new Handler();
        // new thread to handle audio recording on
        new Thread(() -> {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            // buffer size in bytes
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = SAMPLE_RATE * 2;
            }

            short[] audioBuffer = new short[bufferSize / 2];

            mRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(LOG_TAG, "Audio Record can't initialize!");
                return;
            }

            // create file
            OutputStream os;
            try {
                os = new FileOutputStream(ProcessingActivity.getRawAudioFile(getApplicationContext()).toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            // start timer
            runOnUiThread(() -> {
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
            });

            // after 15 sec run handler which stops recording
            mRecordHandler.postDelayed(() -> {
                mRecordButton.setEnabled(false);
                mStopButton.setEnabled(false);
                stopRecording();
                completeRecording();
            }, RECORDING_DURATION);

            long shortsRead = 0;
            mRecordingActive = true;
            mRecorder.startRecording();
            while (mRecordingActive) {
                int numberOfShort = mRecorder.read(audioBuffer, 0, audioBuffer.length);
                shortsRead += numberOfShort;
                try {
                    os.write(short2byte(audioBuffer), 0, audioBuffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Do something with the audioBuffer
            }

            // release
            mRecorder.stop();
            mRecorder.release();
            // close file
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
        }).start();

    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    // stops audio if user decides to end recording early
    private void stopRecording() {
        // indicate that we want the timer to stop
        mRecordingActive = false;

        // stop the timer and reset the base
        timer.stop();
        timer.setBase(SystemClock.elapsedRealtime());

        mStopButton.setVisibility(View.INVISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        // cancel the timer
        if (mRecordHandler != null) {
            mRecordHandler.removeCallbacksAndMessages(null);
        }
    }

    // transitions to processing activity after successful recording
    private void completeRecording() {
        Intent intent = new Intent(MainActivity.this, ProcessingActivity.class);
        startActivityForResult(intent, PROCESSING_RESULT);
    }

    // This method contains the calls for when a button is pressed.
    private void buttonPress() {
        mRecordButton = findViewById(R.id.record_button);
        mStopButton = findViewById(R.id.stop_button);

        // pressing record button
        mRecordButton.setOnClickListener(v -> startRecording());

        // pressing the stop button
        mStopButton.setOnClickListener(v -> {
            stopRecording();
            Toast.makeText(MainActivity.this, R.string.error_recording, Toast.LENGTH_SHORT).show();
        });
    }
}