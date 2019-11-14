package edu.uvm.cs275.conversationanalysis.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.uvm.cs275.conversationanalysis.db.ConversationSchema.ConversationTable;

public class ConversationBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "db.sqlite3";

    public ConversationBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ConversationTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ConversationTable.Cols.UUID + ", " +
                ConversationTable.Cols.DATE + ", " +
                ConversationTable.Cols.UPLOADED + ", " +
                ConversationTable.Cols.START_TIME +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("alter table " + ConversationTable.NAME +
                    " add column " + ConversationTable.Cols.START_TIME + " default \"00:00:00\";");
        }
    }
}
