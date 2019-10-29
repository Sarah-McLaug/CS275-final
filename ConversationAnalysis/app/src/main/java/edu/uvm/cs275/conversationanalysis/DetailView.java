package edu.uvm.cs275.conversationanalysis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import edu.uvm.cs275.conversationanalysis.Conversation;
import edu.uvm.cs275.conversationanalysis.ConversationManager;

public class DetailView extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String GAMMATONE_UUID = "GAMMATONE_UUID";

    private ImageView mImage;
    private TextView mUUID;
    private DrawerLayout mDrawer;
    private Button mMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);

        UUID gammatoneID = (UUID) getIntent().getSerializableExtra(GAMMATONE_UUID);
        String UUID_string = "ID: " + gammatoneID;

        ConversationManager cm = ConversationManager.getInstance(getApplicationContext());
        Conversation conversation = cm.getConversation(gammatoneID);
        Path imagePath = conversation.getImageFile(getApplicationContext());
        File image =  imagePath.toFile();

        mDrawer = findViewById(R.id.drawer_layout);
        mImage = findViewById(R.id.gammatone);
        mUUID = findViewById(R.id.uuid);

        mUUID.setText(UUID_string);
        Bitmap bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
        mImage.setImageBitmap(bmp);

        mMenuButton = findViewById(R.id.menu_button);
        mDrawer = findViewById(R.id.drawer_layout); // grab the navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_view);

        // pressing the menu button
        mMenuButton.setOnClickListener(v -> {
            if (!mDrawer.isDrawerOpen(Gravity.LEFT)) {
                mDrawer.openDrawer(Gravity.LEFT);
            } else {
                mDrawer.closeDrawer(Gravity.RIGHT);
            }
        });
    }

    /* Override the back button if the navigation drawer is open. If it is open, we want the back
     *  button to close the menu, not the entire activity. */
    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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