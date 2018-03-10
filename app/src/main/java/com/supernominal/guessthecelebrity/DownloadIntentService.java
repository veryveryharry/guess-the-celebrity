package com.supernominal.guessthecelebrity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadIntentService extends IntentService {
    public static final int RESULT_OK = 9000;
    protected ResultReceiver resultReceiver;

    private OkHttpClient client = new OkHttpClient();

    public DownloadIntentService() {
        super("DownloadIntentService");
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;
        resultReceiver = intent.getParcelableExtra(MainActivity.KEY_RECEIVER);
        String url = intent.getStringExtra(MainActivity.KEY_URL);
        try {
            String result = getUrl(url);
            deliverResultToReceiver(RESULT_OK, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void deliverResultToReceiver(int resultCode, String result) {
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.KEY_RESULT_DATA, result);
        resultReceiver.send(resultCode, bundle);
    }
}