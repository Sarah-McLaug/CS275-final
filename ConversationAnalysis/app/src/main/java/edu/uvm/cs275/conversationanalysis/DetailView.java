package edu.uvm.cs275.conversationanalysis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class DetailView extends AppCompatActivity implements ConfirmationFragment.InterfaceCommunicator {

    private static final String TAG = "DetailView";
    private static final String GAMMATONE_UUID = "GAMMATONE_UUID";
    private static final String ACTIVITY_INDEX = "ACTIVITY_INDEX";
    private static final String UUID_Index = "UUID_Index";
    private static final int REQUEST_CONFIRM_DELETE = 3;

    private BottomNavigationView mNavMenu;
    private PhotoView mImage;
    private TextView mTimeInterval;
    private TextView mUUID;

    private Conversation mConversation;
    private ConversationManager cm;
    private ImageButton mUploadButton;

    public static Intent newIntent(Context context, Conversation conversation) {
        int detailViewIndex = 2;
        Intent intent = new Intent(context, DetailView.class);
        intent.putExtra(GAMMATONE_UUID, conversation.getUUID());
        intent.putExtra(ACTIVITY_INDEX, detailViewIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        UUID gammatoneID = (UUID) getIntent().getSerializableExtra(GAMMATONE_UUID);

        String UUID_string = "ID: " + gammatoneID;

        cm = ConversationManager.getInstance(getApplicationContext());
        mConversation = cm.getConversation(gammatoneID);
        Path imagePath = mConversation.getImageFile(getApplicationContext());
        File image = imagePath.toFile();

        mImage = findViewById(R.id.photo_view);
        mUUID = findViewById(R.id.uuid);

        mTimeInterval = findViewById(R.id.time_interval);
        mTimeInterval.setText(mConversation.getStartTime());

        mNavMenu = findViewById(R.id.bottom_navigation);
        mNavMenu.setOnNavigationItemSelectedListener(navListener);

        mUUID.setText(UUID_string);
        Bitmap bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
        mImage.setImageBitmap(bmp);

        mUploadButton = findViewById(R.id.upload_button);
        mUploadButton.setOnClickListener(view -> {
            upload(cm);
        });

        if (mConversation.isUploaded()) {
            mUploadButton.setImageResource(R.drawable.ic_uploaded);
            mUploadButton.setEnabled(false);
        } else {
            upload(cm);
        }
    }

    private void upload(ConversationManager cm) {
        Log.d(TAG, "Uploading conversation...");
        cm.uploadConversation(mConversation, true, result -> {
            if (result) {
                Log.d(TAG, "Successfully uploaded conversation " + mConversation.getUUID().toString());
                Toast.makeText(DetailView.this, R.string.upload_success, Toast.LENGTH_LONG).show();
                mUploadButton.setImageResource(R.drawable.ic_uploaded);
                mUploadButton.setEnabled(false);
            } else {
                Log.i(TAG, "Could not upload conversation " + mConversation.getUUID().toString());
                Toast.makeText(DetailView.this, R.string.upload_failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    // This method handles what happens when the screen rotates
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(UUID_Index, mUUID.toString());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch ((item.getItemId())) {
                        case R.id.nav_record:
                            Intent mainIntent = new Intent(DetailView.this, MainActivity.class);
                            startActivity(mainIntent);
                            break;
                        case R.id.nav_view:
                            Intent listIntent = new Intent(DetailView.this, ConversationListActivity.class);
                            startActivity(listIntent);
                            break;
                        case R.id.delete_button:
                            // delete the entry and open a new recycler view.
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            ConfirmationFragment alertdialog = new ConfirmationFragment();
                            alertdialog.show(fragmentManager, "CONFIRMATION_FRAGMENT");
                            break;
                    }
                    return true;
                }
            };

    // this function handles the callback from the delete alert dialog
    // ask Hibbeler for details
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else {

        //if (requestCode == REQUEST_CONFIRM_DELETE) {
            cm.deleteConversation(mConversation);
            Intent listIntentRefresh = new Intent(DetailView.this, ConversationListActivity.class);
            startActivity(listIntentRefresh);
        }
    }

    // this function handles the callback from the delete alert dialog
    // ask Hibbeler for details
    @Override
    public void sendResultCode(boolean deleteConfirmation) {
        Intent intent = new Intent(DetailView.this, ConversationListActivity.class);
        if(deleteConfirmation == true){
            cm.deleteConversation(mConversation);
        }
        startActivity(intent);
    }
}
