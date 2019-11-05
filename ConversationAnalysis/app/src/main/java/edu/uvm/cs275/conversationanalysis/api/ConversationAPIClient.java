package edu.uvm.cs275.conversationanalysis.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import edu.uvm.cs275.conversationanalysis.BuildConfig;

public class ConversationAPIClient {
    private static final String BASE_URL = BuildConfig.APIURL + "api";
    private static AsyncHttpClient sSyncClient = new SyncHttpClient();
    private static AsyncHttpClient sAsyncClient = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, boolean async) {
        AsyncHttpClient client = async ? sAsyncClient : sSyncClient;
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, boolean async) {
        AsyncHttpClient client = async ? sAsyncClient : sSyncClient;
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        get(url, params, responseHandler, false);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        post(url, params, responseHandler, false);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
