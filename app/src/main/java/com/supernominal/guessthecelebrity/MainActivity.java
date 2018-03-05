package com.supernominal.guessthecelebrity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_URL = "key url";
    public static final String KEY_RESULT = "key result";
    public static final String BROADCAST_ACTION = "broadcast action";
    ArrayList<String> celebrityImageUrls = new ArrayList<>();
    ArrayList<String> celebrityNames = new ArrayList<>();
    ArrayList<Integer> shuffleList = new ArrayList<>();
    int correctCelebrity = 0;

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void buildCelebrityList() {
        Intent intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra(KEY_URL, "http://www.imdb.com/list/ls052283250/");
        startService(intent);
    }

    private class DownloadResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(KEY_RESULT);
            Pattern p = Pattern.compile("<div class=\"lister-item-image\">.*?<img.*?alt=\"(.*?)\".*?src=\"(.*?)\"", Pattern.DOTALL);
            Matcher m = p.matcher(result);
            while (m.find()) {
                celebrityNames.add(m.group(1));
                celebrityImageUrls.add(m.group(2)
                        .replace("140,209_","214,317_")
                        .replace("_UX140_", "_UX214_")
                        .replace("_UY209_", "_UY317_")
                );
            }

            generateQuestion();
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            button0.setVisibility(View.VISIBLE);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
        }
    }

    public void generateQuestion() {
        int lastCelebrity = shuffleList.get(correctCelebrity);
        Random rand = new Random();
        correctCelebrity = rand.nextInt(4);
        do {
            Collections.shuffle(shuffleList);
        } while (shuffleList.get(correctCelebrity) == lastCelebrity);
        String celebrityUrl = celebrityImageUrls.get(shuffleList.get(correctCelebrity));
        Picasso.with(this).load(celebrityUrl).into(imageView);
        button0.setText(celebrityNames.get(shuffleList.get(0)));
        button1.setText(celebrityNames.get(shuffleList.get(1)));
        button2.setText(celebrityNames.get(shuffleList.get(2)));
        button3.setText(celebrityNames.get(shuffleList.get(3)));
    }

    public void checkAnswer(View view) {
        Button button = (Button) view;
        if (Integer.parseInt(button.getTag().toString()) == correctCelebrity) {
            Toast toast = Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0 , 100);
            toast.show();
        } else {
            String celebrityName = celebrityNames.get(shuffleList.get(correctCelebrity));
            Toast toast = Toast.makeText(this, "Incorrect! It was " + celebrityName, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0 , 100);
            toast.show();
        }
        generateQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        IntentFilter statusIntentFilter = new IntentFilter(
                BROADCAST_ACTION);
        DownloadResponseReceiver responseReceiver =
                new DownloadResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                responseReceiver, statusIntentFilter );

        for (int i = 0; i < 100; i++) {
            shuffleList.add(i);
        }

        buildCelebrityList();
    }
}
