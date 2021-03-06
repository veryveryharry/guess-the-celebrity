package com.supernominal.guessthecelebrity;

import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_URL = "key_url";
    public static final String KEY_RESULT_DATA = "key_result";
    public static final String KEY_RECEIVER = "key_receiver";
    private static final String STATE_CORRECT_CELEBRITY = "state_correct_celebrity";
    private static final String STATE_CELEBRITY_IMAGE_URLS = "celebrity_image_urls";
    private static final String STATE_CELEBRITY_NAMES = "celebrity_names";
    private static final String STATE_SHUFFLE_LIST = "shuffle_list";
    private static final String STATE_DOWNLOAD_COMPLETED = "download_completed";
    ArrayList<String> celebrityImageUrls = new ArrayList<>();
    ArrayList<String> celebrityNames = new ArrayList<>();
    ArrayList<Integer> shuffleList = new ArrayList<>();
    int correctCelebrity = 0;
    private boolean downloadCompleted = false;
    private DownloadResultReceiver resultReceiver;

    ImageView imageView;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    private void buildCelebrityList() {
        resultReceiver = new DownloadResultReceiver(new Handler());
        Intent intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra(KEY_RECEIVER, resultReceiver);
        intent.putExtra(KEY_URL, "http://www.imdb.com/list/ls052283250/");
        startService(intent);
    }

    private class DownloadResultReceiver extends ResultReceiver {
        DownloadResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }
            String resultString = resultData.getString(KEY_RESULT_DATA);
            if (resultCode != DownloadIntentService.RESULT_OK || resultString == null || resultString.isEmpty()) {
                Toast.makeText(MainActivity.this, "Unable to download celebrity list", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("<div class=\"lister-item-image\">.*?<img.*?alt=\"(.*?)\".*?src=\"(.*?)\"", Pattern.DOTALL);
            Matcher m = p.matcher(resultString);
            while (m.find()) {
                celebrityNames.add(m.group(1));
                celebrityImageUrls.add(m.group(2)
                        .replace("140,209_", "214,317_")
                        .replace("_UX140_", "_UX214_")
                        .replace("_UY209_", "_UY317_")
                );
            }
            downloadCompleted = true;
            generateQuestion();
            endProgressBar();
        }
    }

    private void endProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        button0.setVisibility(View.VISIBLE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        button3.setVisibility(View.VISIBLE);
    }

    private void generateQuestion() {
        int lastCelebrity = shuffleList.get(correctCelebrity);
        Random rand = new Random();
        correctCelebrity = rand.nextInt(4);
        do {
            Collections.shuffle(shuffleList);
        } while (shuffleList.get(correctCelebrity) == lastCelebrity);
        showQuestion();
    }

    private void showQuestion() {
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
            toast.setGravity(Gravity.CENTER, 0, 70);
            toast.show();
        } else {
            String celebrityName = celebrityNames.get(shuffleList.get(correctCelebrity));
            Toast toast = Toast.makeText(this, "Incorrect! It was " + celebrityName, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 70);
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

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_DOWNLOAD_COMPLETED)) {
            celebrityImageUrls = savedInstanceState.getStringArrayList(STATE_CELEBRITY_IMAGE_URLS);
            celebrityNames = savedInstanceState.getStringArrayList(STATE_CELEBRITY_NAMES);
            shuffleList = savedInstanceState.getIntegerArrayList(STATE_SHUFFLE_LIST);
            correctCelebrity = savedInstanceState.getInt(STATE_CORRECT_CELEBRITY);
            downloadCompleted = true;
            endProgressBar();
            showQuestion();
        } else {
            for (int i = 0; i < 100; i++) {
                shuffleList.add(i);
            }
            buildCelebrityList();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(STATE_CELEBRITY_IMAGE_URLS, celebrityImageUrls);
        outState.putStringArrayList(STATE_CELEBRITY_NAMES, celebrityNames);
        outState.putIntegerArrayList(STATE_SHUFFLE_LIST, shuffleList);
        outState.putInt(STATE_CORRECT_CELEBRITY, correctCelebrity);
        outState.putBoolean(STATE_DOWNLOAD_COMPLETED, downloadCompleted);
        super.onSaveInstanceState(outState);
    }
}
