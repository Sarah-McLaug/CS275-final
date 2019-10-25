package edu.uvm.cs275.conversationanalysis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import edu.uvm.cs275.conversationanalysis.Conversation;
import edu.uvm.cs275.conversationanalysis.ConversationManager;

public class DetailView extends AppCompatActivity {

    private static final String GAMMATONE_UUID = "GAMMATONE_UUID";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(); //TODO: make layout

        UUID gammatoneID = (UUID) getIntent().getSerializableExtra(GAMMATONE_UUID);

        ConversationManager cm = ConversationManager.getInstance(getApplicationContext());
        Conversation conversation = cm.getConversation(gammatoneID);
        Path imagePath = conversation.getImageFile(getApplicationContext());
        File image =  imagePath.toFile();

        // TODO: set layout image and UUID
    }

}
