package edu.uvm.cs275.conversationanalysis;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mNavDrawer;
    private ImageButton mRecordButton;
    private Button mMenuButton;
    private TextView mContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavDrawer = (DrawerLayout) findViewById(R.id.drawer_layout); // grab the navigation drawer

        buttonPress();
    }

    /* Override the back button if the navigation drawer is open. If it is open, we want the back
    *  button to close the menu, not the entire activity. */
    @Override
    public void onBackPressed(){
        if(mNavDrawer.isDrawerOpen(GravityCompat.END)){
            mNavDrawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    // This method contains the calls for when a button is pressed.
    private void buttonPress(){
        // Pressing the record button.
        mRecordButton = (ImageButton) findViewById(R.id.record_button);
        /* This toast is a placeholder until we implement the recording function. */
        mRecordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, R.string.record_toast,Toast.LENGTH_SHORT).show();
            }
        });

        // Pressing "Contact Us"
        mContactInfo = (TextView) findViewById(R.id.contact);
        mContactInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mContactInfo.setText(R.string.contact_email);
            }
        });

        // Pressing the menu button
        mMenuButton = (Button) findViewById(R.id.menu_button);
        mMenuButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!mNavDrawer.isDrawerOpen(Gravity.RIGHT)){
                    mNavDrawer.openDrawer(Gravity.RIGHT);
                } else {
                    mNavDrawer.closeDrawer(Gravity.LEFT);
                }
            }
        });
    }
}
