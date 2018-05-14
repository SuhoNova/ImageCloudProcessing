package com.bankai.bleach.imageprocessor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.util.Log.ASSERT;

public class ResultActivity extends AppCompatActivity {
    private View _progressView;
    private View _resultView;

    private TextView _progressText;
    private TextView _timeTakenLabel;

    private LinearLayout _beforeArray;
    private LinearLayout _afterArray;
    private HorizontalScrollView _beforeScrollView;
    private HorizontalScrollView _afterScrollView;
    private ArrayList<Uri> _uriList;
    private ArrayList<Uri> _processedUriList;

    private boolean _doLocalProcessing;
    private int _blurSigma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        _progressView = findViewById(R.id.progressView);
        _resultView = findViewById(R.id.resultView);
        _progressText = findViewById(R.id.progressText);
        _timeTakenLabel = findViewById(R.id.timeTakenLabel);
        _beforeArray = findViewById(R.id.beforeArray);
        _beforeScrollView = findViewById(R.id.beforeScrollView);
        _afterArray = findViewById(R.id.afterArray);
        _afterScrollView = findViewById(R.id.afterScrollView);

        Button returnButton = findViewById(R.id.returnButton);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _blurSigma = getIntent().getIntExtra(MainActivity.ID_BLUR_SIGMA,1);
        TextView processingUsedLabel = findViewById(R.id.processingUsedLabel);
        processingUsedLabel.setText("Gaussian blur with σ of "+_blurSigma);


        _uriList = (ArrayList<Uri>) getIntent().getSerializableExtra(MainActivity.ID_URIS);
        _processedUriList = new ArrayList<>();

        showProgressView(true);

        _doLocalProcessing = getIntent().getBooleanExtra(MainActivity.ID_DO_LOCAL_PROCESSING,true);
        if(_doLocalProcessing){
            setProgressText("Processing Locally");
        } else {
            setProgressText("Processing Remotely");
        }
        new ProcessingTask().execute();
    }

    private void setTimeTaken(long time){
        _timeTakenLabel.setText("Time taken: "+(double)time/1000+"s");
    }

    class ProcessingTask  extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... voids) {
            long timeTaken = 0;
            try {
                if(_doLocalProcessing){
                    timeTaken = localProcessing();
                } else {
                    timeTaken = remoteProcessing();
                }

            } catch (Exception e) {
                Log.println(ASSERT,"Processing", e.toString());
            }

            return timeTaken;
        }

        @Override
        protected void onPostExecute(Long timeTaken) {
            setTimeTaken(timeTaken);
            displayProcessingResults();
            showProgressView(false);
        }
    }

    private long localProcessing() throws Exception{
        final long startTime = System.currentTimeMillis();

        for(Uri imgUri: _uriList) {
            File outputFile = Utility.getEmptyFileThatIsNotCreated();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            bitmap = Utility.rotateImageIfRequired(this,bitmap,imgUri);

            bitmap = Processing.gaussianBlur(bitmap,_blurSigma);


            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Uri processedImgUri = FileProvider.getUriForFile(ResultActivity.this, "com.bankai.bleach.fileprovider", outputFile);

            _processedUriList.add(processedImgUri);
        }
        final long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        Log.println(Log.ASSERT, "Local Time taken", timeTaken+"");

        return timeTaken;
    }

    private String cycleThroughServers(){
     return "https://imagecloudprocessing.azurewebsites.net/services/process/";
    }

    private long remoteProcessing() throws Exception{
        final long startTime = System.currentTimeMillis();
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .build();

        for(Uri imgUri: _uriList){
            File outputFile = Utility.getEmptyFileThatIsNotCreated();

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            bitmap = Utility.rotateImageIfRequired(this,bitmap,imgUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap.recycle();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", null, RequestBody.create(null,byteArray))
                    .addFormDataPart("sigma", _blurSigma+"")
                    .build();

            Request request = new Request.Builder()
                    .header("Content-Type", "multipart/form-data")
                    .url(cycleThroughServers())
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    Bitmap resultImage;

                    InputStream imageStream = response.body().byteStream();
                    resultImage = BitmapFactory.decodeStream(imageStream);
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    resultImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    Uri processedImgUri = FileProvider.getUriForFile(ResultActivity.this, "com.bankai.bleach.fileprovider", outputFile);
                    _processedUriList.add(processedImgUri);

                }
            }

        }
        final long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        Log.println(Log.ASSERT, "Remote Time taken", timeTaken+"");
        return timeTaken;
    }

    public void setProgressText(String text){
        _progressText.setText(text);
    }

    public void displayProcessingResults(){
        try {
            for (Uri uri : _uriList) {
                Utility.displayImageThumbnail(this, uri, null, _beforeArray, _beforeScrollView);
            }
        } catch (Exception e) {
            Log.println(ASSERT,"Displaying Old", e.toString());
            // TODO possible problem with Security Exception of trying to reuse Uris, non reproduceable. possibly solved, put here for safety
            Toast.makeText(this,"Problem displaying Images from before",Toast.LENGTH_SHORT).show();
        }
        for(Uri uri : _processedUriList){
            Utility.displayImageThumbnail(this, uri, null, _afterArray, _afterScrollView);
        }
    }

    /**
     * Shows the progress UI and hides the result view.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgressView(final boolean show) {
        if(show){
            setTitle("Processing");
        } else {
            setTitle("Results");
        }
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        _resultView.setVisibility(show ? View.GONE : View.VISIBLE);
        _resultView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _resultView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        _progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
