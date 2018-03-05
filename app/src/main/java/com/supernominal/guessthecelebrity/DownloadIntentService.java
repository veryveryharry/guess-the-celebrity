package com.supernominal.guessthecelebrity;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadIntentService extends IntentService {
    private OkHttpClient client = new OkHttpClient();

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String url = intent.getStringExtra(MainActivity.KEY_URL);
        try {
            String result = getUrl(url);
            Intent localIntent = new Intent(MainActivity.BROADCAST_ACTION)
                    .putExtra(MainActivity.KEY_RESULT, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


}
