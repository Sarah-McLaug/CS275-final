package edu.uvm.cs275.conversationanalysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;

import java.io.IOException;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Audio Recording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = "audio";

    private DrawerLayout mNavDrawer;
    private ImageButton mRecordButton;
    private ImageButton mStopButton;
    private Button mMenuButton;
    private TextView mContactInfo;
    private MediaRecorder mRecorder = new MediaRecorder();;
    private ConversationManager mConversationManager;

    private boolean recordPermission = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationManager = ConversationManager.getInstance(this);
        setContentView(R.layout.activity_main);
        mNavDrawer = (DrawerLayout) findViewById(R.id.drawer_layout); // grab the navigation drawer
        buttonPress();
    }

    /* Override the back button if the navigation drawer is open. If it is open, we want the back
     *  button to close the menu, not the entire activity. */
    @Override
    public void onBackPressed(){
        if(mNavDrawer.isDrawerOpen(GravityCompat.START)){
            mNavDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /* Override the permissions request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                recordPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(!recordPermission) finish();
    }

    // starts recording audio
    private void startRecording() {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }

        // start recording
        mRecorder.start();

        // after 15 sec. do run() command which stops recording
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
            }
        }, 15000);
    }

    // stops audio if user decides to end recording early
    private void stopRecording() {
        mRecorder.stop();
        Toast.makeText(MainActivity.this, R.string.error_recording, Toast.LENGTH_SHORT).show();
    }

    // This method contains the calls for when a button is pressed.
    private void buttonPress() {
        mRecordButton = (ImageButton) findViewById(R.id.record_button);
        mStopButton = (ImageButton) findViewById(R.id.stop_button);
        mContactInfo = (TextView) findViewById(R.id.contact);
        mMenuButton = (Button) findViewById(R.id.menu_button);

        // pressing record button
        // startRecording() method commented out because it crashes program --> think it has to do with no recorder working in emulator
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.record_toast, Toast.LENGTH_SHORT).show();
                mRecordButton.setVisibility(v.INVISIBLE);
                mStopButton.setVisibility(v.VISIBLE);
                // startRecording();
            }
        });

        // pressing the stop button
        // stopRecording() method commented out because it crashes program --> think it has to do with no recorder working in emulator
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopButton.setVisibility(v.INVISIBLE);
                mRecordButton.setVisibility(v.VISIBLE);
                // stopRecording();
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
            public void onClick(View v){
                if(!mNavDrawer.isDrawerOpen(Gravity.LEFT)){
                    mNavDrawer.openDrawer(Gravity.LEFT);
                } else {
                    mNavDrawer.closeDrawer(Gravity.RIGHT);
                }
            }
        });
    }
}
