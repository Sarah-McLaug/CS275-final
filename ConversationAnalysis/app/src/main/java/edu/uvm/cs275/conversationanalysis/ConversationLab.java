package edu.uvm.cs275.conversationanalysis;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationLab {
    private static ConversationLab sConversationLab;
    private List<Conversation> mConversations;

    public static ConversationLab get(Context context){
        if(sConversationLab == null) {
            sConversationLab = new ConversationLab(context);
        }
        return sConversationLab;
    }

    private ConversationLab(Context context) {
        mConversations = new ArrayList<>();
        for (int i=0; i<20; i++) {
            Conversation conversation = new Conversation();
            mConversations.add(conversation);
        }
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    public Conversation getConversation(UUID id) {
        for(Conversation conversation : mConversations) {
            if(conversation.getUUID().equals(id)) {
                return conversation;
            }
        }
        return null;
    }
}
