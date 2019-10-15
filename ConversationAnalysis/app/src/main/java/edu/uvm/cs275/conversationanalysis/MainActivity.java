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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Audio Recording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = "audio.wav";

    private DrawerLayout mNavDrawer;
    private ImageButton mRecordButton;
    private Button mMenuButton;
    private TextView mContactInfo;
    private MediaRecorder mRecorder;
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

    // This method starts recording audio
    private void startRecording(){
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    // This method contains the calls for when a button is pressed.
    private void buttonPress() {
        // Pressing the record button.
        mRecordButton = (ImageButton) findViewById(R.id.record_button);
        /* This toast is a placeholder until we implement the recording function. */
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.record_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Pressing "Contact Us"
        mContactInfo = (TextView) findViewById(R.id.contact);
        mContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContactInfo.setText(R.string.contact_email);
            }
        });

        // Pressing the menu button
        mMenuButton = (Button) findViewById(R.id.menu_button);
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
