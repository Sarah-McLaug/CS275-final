package edu.uvm.cs275.conversationanalysis;

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
}
