package edu.uvm.cs275.conversationanalysis.db;

import android.sax.StartElementListener;

public class ConversationSchema {
    public static final class ConversationTable {
        public static final String NAME = "conversation";

        public static final class Cols {
            public static final String UUID = "UUID";
            public static final String DATE = "date";
            public static final String UPLOADED = "uploaded";
            public static final String START_TIME = "start_time";
        }
    }
}
