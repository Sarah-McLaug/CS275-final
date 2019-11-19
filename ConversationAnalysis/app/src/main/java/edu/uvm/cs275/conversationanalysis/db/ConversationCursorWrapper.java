package edu.uvm.cs275.conversationanalysis.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

import edu.uvm.cs275.conversationanalysis.Conversation;

public class ConversationCursorWrapper extends CursorWrapper {

    public ConversationCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Conversation getConversation() {
        String uuidString = getString(getColumnIndex(ConversationSchema.ConversationTable.Cols.UUID));
        long date = getLong(getColumnIndex(ConversationSchema.ConversationTable.Cols.DATE));
        boolean uploaded = getInt(getColumnIndex(ConversationSchema.ConversationTable.Cols.UPLOADED)) == 1;
        String startTime = getString(getColumnIndex(ConversationSchema.ConversationTable.Cols.START_TIME));

        return new Conversation(UUID.fromString(uuidString), new Date(date), uploaded, startTime);
    }

}
