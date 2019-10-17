package edu.uvm.cs275.conversationanalysis;

import android.content.Context;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

public class Conversation {

    private UUID mUUID;
    private Date mDate;
    private boolean mUploaded;

    public Conversation(UUID uuid, Date date, boolean uploaded) {
        this.mUUID = uuid;
        this.mDate = date;
        this.mUploaded = false;
    }

    public Conversation() {
        this(UUID.randomUUID(), new Date(), false);
    }

    public UUID getUUID() {
        return mUUID;
    }

    public void setUUID(UUID UUID) {
        mUUID = UUID;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isUploaded() {
        return mUploaded;
    }

    public void setUploaded(boolean uploaded) {
        mUploaded = uploaded;
    }

    public Path getImageFile(Context context) {
        return ConversationManager.getInstance(context).getImageDir().resolve(this.mUUID.toString() + ConversationManager.IMAGE_EXT);
    }
}
