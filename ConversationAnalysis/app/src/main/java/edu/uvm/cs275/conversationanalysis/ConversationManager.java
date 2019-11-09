package edu.uvm.cs275.conversationanalysis;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import edu.uvm.cs275.conversationanalysis.api.ConversationAPIClient;
import edu.uvm.cs275.conversationanalysis.db.ConversationBaseHelper;
import edu.uvm.cs275.conversationanalysis.db.ConversationCursorWrapper;
import edu.uvm.cs275.conversationanalysis.db.ConversationSchema.ConversationTable;

public class ConversationManager {
    public static final String IMAGE_EXT = ".png";
    public static final long CONVERSATION_LENGTH = 15000;

    private static final String TAG = "ConversationManager";
    private static final String IMAGE_DIR_NAME = "images";
    private static final String PREFS_NAME = "CONVERSATION_PREFS";
    private static final String DEVICE_UUID = "DEVICE_UUID";
    private static final String DEVICE_REGISTERED = "DEVICE_REGISTERED";

    private static ConversationManager sInstance;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private UUID mDeviceUUID;

    private ConversationManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ConversationBaseHelper(mContext).getWritableDatabase();

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        if (settings.getString(DEVICE_UUID, null) == null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(DEVICE_UUID, UUID.randomUUID().toString());
            editor.apply();
        }

        mDeviceUUID = UUID.fromString(settings.getString(DEVICE_UUID, null));
        Log.i(TAG, "Device has UUID: " + mDeviceUUID.toString());
        if (!getDeviceRegistered()) {
            registerDevice();
        } else {
            Log.d(TAG, "Device has been registered.");
        }
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
        values.put(ConversationTable.Cols.UPLOADED, c.isUploaded());
        return values;
    }

    public boolean getDeviceRegistered() {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(DEVICE_REGISTERED, false);
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

    public Path getImageDir() {
        return mContext.getFilesDir().toPath().resolve(IMAGE_DIR_NAME);
    }

    private void registerDevice() {
        Log.i(TAG, "Attempting to register device with UUID " + mDeviceUUID.toString());
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        if (getDeviceRegistered()) {
            Log.d(TAG, "Skipping registration of registered device");
            return;
        }

        String deviceURL = String.format("/devices/%s/", mDeviceUUID);
        // mark device registered if can retrieve it or if successfully registered
        ConversationAPIClient.get(deviceURL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i(TAG, "Device already registered in server.");
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(DEVICE_REGISTERED, true);
                editor.apply();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (statusCode != 404) {
                    Log.i(TAG, "Device registration status return statuscode " + statusCode);
                    return;
                }
                RequestParams params = new RequestParams();
                params.put("uuid", mDeviceUUID.toString());
                ConversationAPIClient.post("/devices/", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i(TAG, "Device registration successful");
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(DEVICE_REGISTERED, true);
                        editor.apply();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.i(TAG, "Failed to register device with status code " + statusCode);
                    }


                }, true);
            }
        }, true);


    }

    // returns true if conversation has been successfully uploaded, either now, or in the past
    public boolean uploadConversation(final Conversation conversation) {
        Log.i(TAG, "uploadConversation");
        if (conversation.isUploaded()) {
            return true;
        }
        if (!getDeviceRegistered()) {
            registerDevice();
        }

        // set params
        File gammatone = conversation.getImageFile(mContext).toFile();
        RequestParams params = new RequestParams();
        params.put("device", mDeviceUUID.toString());
        params.put("uuid", conversation.getUUID().toString());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        params.put("date", df.format(conversation.getDate()));
        try {
            params.put("gammatone", gammatone);
        } catch (FileNotFoundException e) {
            return false;
        }

        // make request and handle response
        ConversationAPIClient.post("/conversations/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                conversation.setUploaded(true);
                updateConversation(conversation);
                Log.i(TAG, "Successfully uploaded conversation w/ status code" + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Failed to upload conversation, got status " + statusCode + "; errors:");
                for (Iterator<String> it = errorResponse.keys(); it.hasNext(); ) {
                    String key = it.next();
                    try {
                        Log.e(TAG, key + ": " + errorResponse.getString(key));
                    } catch (JSONException e) {
                    }
                }
            }

        });

        return conversation.isUploaded();
    }


    public static class UploadTask extends AsyncTask<Conversation, Void, Boolean> {


        private final TaskListener mTaskListener;
        private ConversationManager mConversationManager;

        public UploadTask(Context context, TaskListener listener) {
            mConversationManager = getInstance(context);
            this.mTaskListener = listener;
        }

        @Override
        protected Boolean doInBackground(Conversation... conversations) {
            if (conversations.length != 1) {
                return false;
            }
            return mConversationManager.uploadConversation(conversations[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (this.mTaskListener != null) {
                this.mTaskListener.onFinished(result);
            }
        }

        public interface TaskListener {
            void onFinished(Boolean result);
        }
    }

}
