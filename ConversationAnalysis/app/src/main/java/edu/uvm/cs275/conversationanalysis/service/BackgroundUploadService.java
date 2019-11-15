package edu.uvm.cs275.conversationanalysis.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import edu.uvm.cs275.conversationanalysis.Conversation;
import edu.uvm.cs275.conversationanalysis.ConversationManager;

public class BackgroundUploadService extends IntentService {
    private static final String NAME = "BackgroundUploadService";

    public BackgroundUploadService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ConversationManager cm = ConversationManager.getInstance(getApplicationContext());
        for (Conversation conversation : cm.getConversations()) {
            if (!conversation.isUploaded()) {
                cm.uploadConversation(conversation, false, null);
            }
        }
    }
}
