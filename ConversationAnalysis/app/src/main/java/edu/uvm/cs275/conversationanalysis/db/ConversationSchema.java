package edu.uvm.cs275.conversationanalysis.db;

public class ConversationSchema {
    public static final class ConversationTable {
        public static final String NAME = "conversation";

        public static final class Cols {
            public static final String UUID = "UUID";
            public static final String DATE = "date";
            public static final String UPLOADED = "uploaded";
        }
    }
}
