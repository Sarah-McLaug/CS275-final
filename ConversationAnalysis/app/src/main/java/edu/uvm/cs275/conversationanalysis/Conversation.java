package edu.uvm.cs275.conversationanalysis;

import java.util.Date;
import java.util.UUID;

public class Conversation {

    private UUID mUUID;
    private Date mDate;

    public Conversation(UUID uuid, Date date) {
        this.mUUID = uuid;
        this.mDate = date;
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
}
