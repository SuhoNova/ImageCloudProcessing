package com.bankai.bleach.imageprocessor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    private View _progressView;
    private View _resultView;

    private ProgressBar _progressBar;
    private TextView _progressText;

    private LinearLayout _beforeArray;
    private LinearLayout _afterArray;
    private HorizontalScrollView _beforeScrollView;
    private HorizontalScrollView _afterScrollView;
    private ArrayList<Uri> _uriList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        _progressView = findViewById(R.id.progressView);
        _resultView = findViewById(R.id.resultView);
        _progressBar = findViewById(R.id.progressBar);
        _progressText = findViewById(R.id.progressText);
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

        TextView processingUsedLabel = findViewById(R.id.processingUsedLabel);
        processingUsedLabel.setText(getIntent().getStringExtra(MainActivity.ID_PROCESSING_TYPE));

        _uriList = (ArrayList<Uri>) getIntent().getSerializableExtra(MainActivity.ID_URIS);

        showProgressView(true);

        boolean _doLocalProcessing = getIntent().getBooleanExtra(MainActivity.ID_DO_LOCAL_PROCESSING,true);
        if(_doLocalProcessing){
            new LocalProcessingTask(this).execute();
        } else {
            new RemoteProcessingTask(this).execute();
        }
    }

    public void setProgressText(String text){
        _progressText.setText(text);
    }

    public void toBeChangedFunctionToPopulateImageArraysAfterGettingResults(){
        for(Uri uri : _uriList){
            Utility.displayImageThumbnail(this, uri, null, _beforeArray, _beforeScrollView);
            Utility.displayImageThumbnail(this, uri, null, _afterArray, _afterScrollView);
        }
    }

    /**
     * Shows the progress UI and hides the result view.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgressView(final boolean show) {
        if(show){
            setTitle("Professor Image の Progress");
        } else {
            setTitle("Professor Image の Results");
        }
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            _resultView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
