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
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
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

    private long _startTime;

    private long _timeTaken = 0;
    private AtomicInteger _remoteProcessingLeft = new AtomicInteger(0);

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

        _startTime = System.currentTimeMillis();
        Log.println(ASSERT,"Yes","Ni hao"+_startTime);

        if(_doLocalProcessing){
            setProgressText("Processing Locally");
            new LocalProcessingTask().execute();
        } else {
            setProgressText("Processing Remotely");

            if(_uriList.size()>1){
                _remoteProcessingLeft.addAndGet(2);
            } else {
                _remoteProcessingLeft.addAndGet(1);
            }

            new RemoteProcessingTask(true).execute();
            if(_uriList.size()>1){
                new RemoteProcessingTask(false).execute();
            }
        }
    }


    class LocalProcessingTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... voids) {
            long timeTaken = 0;
            try {
                timeTaken = localProcessing();

            } catch (Exception e) {
                Log.println(ASSERT,"Processing", e.toString());
            }

            return timeTaken;
        }

        @Override
        protected void onPostExecute(Long timeTaken) {
            _timeTaken += timeTaken;
            processingDone();
        }
    }


    private void remoteProcessingDone(){
        int processingLeft = _remoteProcessingLeft.decrementAndGet();
        Log.println(ASSERT,"Yes","Two step to contacto "+System.currentTimeMillis());
        if(processingLeft == 0){
            final long endTime = System.currentTimeMillis();
            long timeTaken = endTime - _startTime;
            _timeTaken += timeTaken;
            Log.println(ASSERT,"Remote time taken",_timeTaken+"");
            processingDone();
        }
    }

    private void processingDone(){
        _timeTakenLabel.setText("Time taken: "+(double)_timeTaken/1000+"s");

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() -> {
            displayProcessingResults();
            showProgressView(false);
        });

    }


    class RemoteProcessingTask extends AsyncTask<Void, Void, Void> {
        boolean firstServer;
        public RemoteProcessingTask(boolean useFirstServer){
            firstServer = useFirstServer;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(firstServer){
                    remoteProcessing(_uriList.get(0),firstServer);
                } else {
                    remoteProcessing(_uriList.get(1),firstServer);
                }

            } catch (Exception e) {
                Log.println(ASSERT,"Processing", e.toString());
            }

            return null;
        }

    }

    private long localProcessing() throws Exception{
        for(Uri imgUri: _uriList) {
            File outputFile = Utility.getEmptyFileThatIsNotCreated("");
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            bitmap = Utility.rotateImageIfRequired(this,bitmap,imgUri);

            bitmap = Processing.gaussianBlur(bitmap,_blurSigma);


            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Uri processedImgUri = FileProvider.getUriForFile(ResultActivity.this, "com.bankai.bleach.fileprovider", outputFile);

            _processedUriList.add(processedImgUri);
        }
        final long endTime = System.currentTimeMillis();
        long timeTaken = endTime - _startTime;
        Log.println(Log.ASSERT, "Local Time taken", timeTaken+"");

        return timeTaken;
    }

    private String getServerName(boolean firstServer){
        if(firstServer){
            return "https://imagecloudprocessing.azurewebsites.net/services/process/";
        } else {
            return "https://imagecloudprocessing2.azurewebsites.net/services/process/";
        }
    }

    private void remoteProcessing(Uri imgUri, boolean firstServer) throws Exception{
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .build();

        File outputFile = Utility.getEmptyFileThatIsNotCreated(firstServer+"");

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
                .url(getServerName(firstServer))
                .post(requestBody)
                .build();

        Log.println(ASSERT,"Convert time taken",((double)(System.currentTimeMillis()-_startTime)/1000)+"");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.println(ASSERT,"Yes","One step to contacto "+System.currentTimeMillis());

                Bitmap resultImage;

                InputStream imageStream = response.body().byteStream();
                resultImage = BitmapFactory.decodeStream(imageStream);
                FileOutputStream fos = new FileOutputStream(outputFile);
                resultImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                Uri processedImgUri = FileProvider.getUriForFile(ResultActivity.this, "com.bankai.bleach.fileprovider", outputFile);
                _processedUriList.add(processedImgUri);

                remoteProcessingDone();
            }
        });
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
            // possible problem with Security Exception of trying to reuse Uris, non reproduceable. possibly solved, put here for safety
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
