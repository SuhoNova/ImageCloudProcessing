package com.bankai.bleach.imageprocessor;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.support.v4.content.FileProvider.getUriForFile;
import static android.util.Log.ASSERT;

public class MainActivity extends AppCompatActivity{
    // Permission Trackers
    private final int RC_CAMERA = 123;
    private final int RC_GALLERY = 456;

    private final int THUMBNAIL_HEIGHT = 400;

    private Button _cameraBtn;
    private Button _galleryBtn;
    private Button _localBtn;
    private Button _remoteBtn;
    private HorizontalScrollView _horzScrollView;
    private LinearLayout _imgArray;
    private Spinner _imgProcSpinner;

    private ArrayList<Uri> _uriList;
    private File _latestPhoto;
    private Uri _latestPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _cameraBtn = findViewById(R.id.cameraButton);
        _galleryBtn = findViewById(R.id.galleryButton);
        _localBtn = findViewById(R.id.localProcButton);
        _remoteBtn = findViewById(R.id.remoteProcButton);
        _imgArray = findViewById(R.id.imgArray);
        _horzScrollView = findViewById(R.id.horizontalScrollView);
        _imgProcSpinner = findViewById(R.id.imgProcSpinner);

        _uriList = new ArrayList<>();

        setOnClickListenersForButtons();
        populateImgProcSpinner();

    }

    protected void populateImgProcSpinner(){
        ArrayList<String> imageProcOptions = new ArrayList<>();
        imageProcOptions.add("Seam Carving");
        imageProcOptions.add("Beautify Face");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, imageProcOptions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _imgProcSpinner.setAdapter(dataAdapter);
    }

    private void setOnClickListenersForButtons(){
        _cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCameraBtnClicked();
            }
        });
        _galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGalleryBtnClicked();
            }
        });
        _localBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLocalBtnClicked();
            }
        });
        _remoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoteBtnClicked();
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
                    Log.println(ASSERT,"Camera Pic Path", _latestPhotoUri.getPath());

                    _uriList.add(_latestPhotoUri);

                    displayImageThumbnail(_latestPhotoUri);
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

                        displayImageThumbnail(intent.getData());
                    } else {
                        Toast.makeText(this, "File selected is not a photo.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void displayImageThumbnail(final Uri imgUri){
        final ImageView imageView = new ImageView(this);
        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            thumbnail = rotateImageIfRequired(thumbnail,imgUri);

            int desiredWidth = Math.round((((float) thumbnail.getWidth() / thumbnail.getHeight()) * THUMBNAIL_HEIGHT));

            thumbnail = Bitmap.createScaledBitmap(thumbnail, desiredWidth, THUMBNAIL_HEIGHT, true);

            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setImageBitmap(thumbnail);
            imageView.setPadding(10,0,0,0);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayImage(imgUri);
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Image Removal");
                    alertBuilder.setMessage("Are you sure to remove the following image from being processed?");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            _uriList.remove(imgUri);
                            _imgArray.removeView(view);
                        }
                    }).setNegativeButton(android.R.string.no, null);
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                    return true;
                }
            });

            _imgArray.addView(imageView,0);
            if(_horzScrollView != null){
                _horzScrollView.smoothScrollTo(0,0);
            }
        }catch (Exception e){
            Log.println(ASSERT,"Exception", e.toString());
        }
    }

    private void displayImage(Uri imgUri){
        Uri contentUri = null;
        if (imgUri.getScheme().equals("content")) {
            contentUri = imgUri;
        } else {
            contentUri = getUriForFile(this,
                    "com.bankai.bleach.fileprovider",
                    new File(imgUri.getPath()));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, contentUri);
        intent.setDataAndType(contentUri, "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(intent);

    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri imageUri) throws IOException {
        int rotation = 0;
        InputStream in = getContentResolver().openInputStream(imageUri);
        ExifInterface ei = new ExifInterface(in);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation =  270;
                break;
            default:
                return img;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void onCameraBtnClicked(){
        try {
            if (checkAndRequestPermission(this, Manifest.permission.CAMERA, RC_CAMERA)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                _latestPhoto = getEmptyFile();

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


    private File getEmptyFile() {
        String uniqueName = "IPROC_" + new SimpleDateFormat("yyMMddHHmmss").format(new Date());

        File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separatorChar+"ImageProc");

        if(!photoDirectory.exists()) {
            if (!photoDirectory.mkdirs()){
                Log.println(ASSERT,"PhotoDir","mkdirs Failed");
            }
        }

        File emptyFile = new File(photoDirectory,uniqueName+".jpeg");

        return emptyFile;
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

    private void onLocalBtnClicked(){

    }
    private void onRemoteBtnClicked(){

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
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity ,new String[]{permissionNeeded}, permissionTracker);
                        }
                    });
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
