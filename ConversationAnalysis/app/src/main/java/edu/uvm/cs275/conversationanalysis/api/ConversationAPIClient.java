package edu.uvm.cs275.conversationanalysis.api;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import edu.uvm.cs275.conversationanalysis.BuildConfig;

public class ConversationAPIClient {
    private static final String BASE_URL = BuildConfig.APIURL + "api";
    private static SyncHttpClient sClient = new SyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        sClient.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        System.out.println(getAbsoluteUrl(url));
        sClient.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
