package edu.uvm.cs275.conversationanalysis;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.uvm.cs275.conversationanalysis.db.ConversationBaseHelper;
import edu.uvm.cs275.conversationanalysis.db.ConversationCursorWrapper;
import edu.uvm.cs275.conversationanalysis.db.ConversationSchema.ConversationTable;

public class ConversationManager {
    private static ConversationManager sInstance;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private ConversationManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ConversationBaseHelper(mContext).getWritableDatabase();
    }

    public static ConversationManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ConversationManager(context);
        }
        return sInstance;
    }

    private static ContentValues getContentValues(Conversation c) {
        ContentValues values = new ContentValues();
        values.put(ConversationTable.Cols.UUID, c.getUUID().toString());
        values.put(ConversationTable.Cols.DATE, c.getDate().getTime());
        return values;
    }

    public void addConversation(Conversation c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(ConversationTable.NAME, null, values);
    }

    public void updateConversation(Conversation c) {
        String uuidString = c.getUUID().toString();
        ContentValues values = getContentValues(c);
        mDatabase.update(ConversationTable.NAME, values, ConversationTable.Cols.UUID + "=?", new String[]{uuidString});
    }

    private ConversationCursorWrapper queryConversations(String whereClause, String[] whereArgs) {
        return new ConversationCursorWrapper(mDatabase.query(
                ConversationTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        ));
    }

    public List<Conversation> getConversations() {
        List<Conversation> conversations = new ArrayList<>();

        try (ConversationCursorWrapper cursor = queryConversations(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                conversations.add(cursor.getConversation());
                cursor.moveToNext();
            }
        }
        return conversations;
    }

    public Conversation getConversation(UUID uuid) {
        try (ConversationCursorWrapper cursor = queryConversations(
                ConversationTable.Cols.UUID + "=?",
                new String[]{uuid.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getConversation();
        }
    }
}
