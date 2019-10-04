package edu.uvm.cs275.conversationanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageButton mRecordButton;
    private TextView mContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPress();
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
    }
}
