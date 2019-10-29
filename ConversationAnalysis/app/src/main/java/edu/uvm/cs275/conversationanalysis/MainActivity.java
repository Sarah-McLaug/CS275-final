package edu.uvm.cs275.conversationanalysis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "Audio Recording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORDING_DURATION = 15000;

    private static final int PROCESSING_RESULT = 1;
    public static final int RESULT_FAILURE = 2;

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
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_record);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROCESSING_RESULT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.successful, Toast.LENGTH_SHORT).show();
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

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(ProcessingActivity.getAudioFile(getApplicationContext()));
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        // start recording
        mRecorder.start();

        // after 15 sec. do run() command which stops recording
        mRecordHandler = new Handler();
        mRecordHandler.postDelayed(() -> {
            mRecordButton.setEnabled(false);
            mStopButton.setEnabled(false);
            stopRecording();
            completeRecording();
        }, RECORDING_DURATION);
    }

    // stops audio if user decides to end recording early
    private void stopRecording() {
        mRecorder.stop();
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
        mContactInfo = findViewById(R.id.contact);
        mMenuButton = findViewById(R.id.menu_button);

        // pressing record button
        mRecordButton.setOnClickListener(v -> startRecording());

        // pressing the stop button
        mStopButton.setOnClickListener(v -> {
            stopRecording();
            Toast.makeText(MainActivity.this, R.string.error_recording, Toast.LENGTH_SHORT).show();
        });

        // pressing "Contact Us"
        mContactInfo.setOnClickListener(v -> mContactInfo.setText(R.string.contact_email));

        // pressing the menu button
        mMenuButton.setOnClickListener(v -> {
            if (!mNavDrawer.isDrawerOpen(Gravity.LEFT)) {
                mNavDrawer.openDrawer(Gravity.LEFT);
            } else {
                mNavDrawer.closeDrawer(Gravity.RIGHT);
            }
        });
    }

    // This method handles what happens when you click on a nav menu item.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.nav_view:
                Intent intent = new Intent(this, ConversationList.class);
                startActivity(intent);
                break;
            case R.id.nav_record:
                // Do nothing because we're already on that activity.
                break;
        }
        return true;
    }
}