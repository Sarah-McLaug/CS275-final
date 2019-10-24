package edu.uvm.cs275.conversationanalysis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Audio Recording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORDING_DURATION = 15000;

    private static final String EXTRA_CONVERSATION_DISCARDED = "edu.uvm.cs275.conversationanalysis.conversation_discarded";

    private DrawerLayout mNavDrawer;
    private ImageButton mRecordButton;
    private ImageButton mStopButton;
    private Button mMenuButton;
    private TextView mContactInfo;
    private MediaRecorder mRecorder = new MediaRecorder();
    private Handler mRecordHandler;

    private ConversationManager mConversationManager;

    private boolean recordPermission = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationManager = ConversationManager.getInstance(this);
        setContentView(R.layout.activity_main);
        mNavDrawer = findViewById(R.id.drawer_layout); // grab the navigation drawer

        if (getIntent().getBooleanExtra(EXTRA_CONVERSATION_DISCARDED, false)) {
            Toast.makeText(this, R.string.error_recording, Toast.LENGTH_SHORT).show();
        }

        buttonPress();
    }

    /* Override the back button if the navigation drawer is open. If it is open, we want the back
     *  button to close the menu, not the entire activity. */
    @Override
    public void onBackPressed() {
        if (mNavDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        if (!recordPermission) finish();
    }

    // starts recording audio
    private void startRecording() {
        Toast.makeText(MainActivity.this, R.string.record_toast, Toast.LENGTH_SHORT).show();
        mRecordButton.setVisibility(View.INVISIBLE);
        mStopButton.setVisibility(View.VISIBLE);
//        TODO: make this not crash things; perhaps broken because emulator?
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//        mRecorder.setOutputFile(ProcessingActivity.getAudioFile(getApplicationContext()));
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        try {
//            mRecorder.prepare();
//        } catch (IOException e){
//            Log.e(LOG_TAG, "prepare() failed");
//        }
//
//        // start recording
//        mRecorder.start();

        // after 15 sec. do run() command which stops recording
        mRecordHandler = new Handler();
        mRecordHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecording();
                completeRecording();
            }
        }, RECORDING_DURATION);
    }

    // stops audio if user decides to end recording early
    private void stopRecording() {
//        mRecorder.stop(); // TODO: make this not crash things; perhaps broken because emulator?
        mStopButton.setVisibility(View.INVISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        // cancel the timer
        if (mRecordHandler != null) {
            mRecordHandler.removeCallbacksAndMessages(null);
        }
    }

    // transitions to processing activity after successful recording
    private void completeRecording() {
        // TODO: pass necessary data
        Intent intent = new Intent(MainActivity.this, ProcessingActivity.class);
        startActivity(intent);
    }

    // This method contains the calls for when a button is pressed.
    private void buttonPress() {
        mRecordButton = (ImageButton) findViewById(R.id.record_button);
        mStopButton = (ImageButton) findViewById(R.id.stop_button);
        mContactInfo = (TextView) findViewById(R.id.contact);
        mMenuButton = (Button) findViewById(R.id.menu_button);

        // pressing record button
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        // pressing the stop button
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                Toast.makeText(MainActivity.this, R.string.error_recording, Toast.LENGTH_SHORT).show();
            }
        });

        // pressing "Contact Us"
        mContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContactInfo.setText(R.string.contact_email);
            }
        });

        // pressing the menu button
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNavDrawer.isDrawerOpen(Gravity.LEFT)) {
                    mNavDrawer.openDrawer(Gravity.LEFT);
                } else {
                    mNavDrawer.closeDrawer(Gravity.RIGHT);
                }
            }
        });
    }

    public static Intent newIntent(Context context, boolean conversationDiscarded) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_DISCARDED, conversationDiscarded);
        return intent;
    }
}
