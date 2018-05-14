package com.bankai.bleach.imageprocessor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static android.util.Log.ASSERT;

public class MainActivity extends AppCompatActivity{
    // Permission Trackers
    private final int RC_CAMERA = 1;
    private final int RC_GALLERY = 2;

    public final static String ID_DO_LOCAL_PROCESSING = "3";
    public final static String ID_BLUR_SIGMA = "4";
    public final static String ID_URIS = "5";

    private HorizontalScrollView _horzScrollView;
    private LinearLayout _imgArray;
    private EditText _blurSigma;

    private ArrayList<Uri> _uriList;
    private Uri _latestPhotoUri;
    private File _latestPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Image Blurrer");

        Button cameraBtn = findViewById(R.id.cameraButton);
        Button galleryBtn = findViewById(R.id.galleryButton);
        Button localBtn = findViewById(R.id.localProcButton);
        Button remoteBtn = findViewById(R.id.remoteProcButton);

        _blurSigma = findViewById(R.id.sigmaText);

        _imgArray = findViewById(R.id.imgArray);
        _horzScrollView = findViewById(R.id.horizontalScrollView);

        _uriList = new ArrayList<>();

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCameraBtnClicked();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGalleryBtnClicked();
            }
        });
        localBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProcessingBtnClicked(true);
            }
        });
        remoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProcessingBtnClicked(false);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_CAMERA) {
                try {
                    Log.println(ASSERT,"Camera Pic Uri", _latestPhotoUri.toString());

                    _uriList.add(_latestPhotoUri);

                    Utility.correctImageRotation(this,_latestPhotoUri,_latestPhoto);

                    Utility.displayImageThumbnail(this, _latestPhotoUri, _uriList, _imgArray, _horzScrollView);
                } catch (Exception e) {
                    Log.println(ASSERT,"Exception",e.toString());
                }
            } else if (requestCode == RC_GALLERY){
                if (intent != null) {
                    String type = getContentResolver().getType(intent.getData());
                    if(type.contains("image/")) {
                        Log.println(ASSERT, "Gallery Pic Uri", intent.getData().toString());
                        Log.println(ASSERT, "Gallery Pic Path", intent.getData().getPath());

                        _uriList.add(intent.getData());

                        Utility.displayImageThumbnail(this, intent.getData(), _uriList, _imgArray, _horzScrollView);
                    } else {
                        Toast.makeText(this, "File selected is not a photo.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void onCameraBtnClicked(){
        try {
            if (checkAndRequestPermission(this, Manifest.permission.CAMERA, RC_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                _latestPhoto = Utility.getEmptyFileThatIsNotCreated();

                // wrap File object into a content provider
                // required for API >= 24
                // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
                _latestPhotoUri = FileProvider.getUriForFile(MainActivity.this, "com.bankai.bleach.fileprovider", _latestPhoto);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, _latestPhotoUri);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, RC_CAMERA);
                }
            }
        } catch (Exception e){
            Log.println(ASSERT,"gg",e.toString());
        }
    }

    private void onGalleryBtnClicked(){
        if(checkAndRequestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, RC_GALLERY)) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, RC_GALLERY);
            }
        }
    }

    private void onProcessingBtnClicked(boolean doLocalProcessing){
        try{
            int sigma = Integer.parseInt(_blurSigma.getText().toString());
            if(sigma < 1) {
                Toast.makeText(this,"Std  must be greater than 1.",Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra(ID_DO_LOCAL_PROCESSING, doLocalProcessing);
                intent.putExtra(ID_BLUR_SIGMA, sigma);
                intent.putExtra(ID_URIS, _uriList);
                startActivity(intent);
                finish();
            }
        } catch (NumberFormatException nfe){
            Toast.makeText(this,"Sigma must be a positive integer.",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndRequestPermission(final Activity activity, final String permissionNeeded, final int permissionTracker)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(activity, permissionNeeded) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionNeeded)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("This permission is needed for the proper functioning of the app.");
                    alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> ActivityCompat.requestPermissions(activity ,new String[]{permissionNeeded}, permissionTracker));
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{permissionNeeded}, permissionTracker);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}