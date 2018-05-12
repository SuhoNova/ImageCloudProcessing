package com.bankai.bleach.imageprocessor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.support.v4.content.FileProvider.getUriForFile;
import static android.util.Log.ASSERT;

public class Utility {
    private static final int THUMBNAIL_HEIGHT = 400;

    public static void displayImageThumbnail(final Activity activity, final Uri imgUri, final ArrayList<Uri> uriList, final LinearLayout imgArray, final HorizontalScrollView scrollView){
        final ImageView imageView = new ImageView(activity);
        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imgUri);
            thumbnail = rotateImageIfRequired(activity,thumbnail,imgUri);

            int desiredWidth = Math.round((((float) thumbnail.getWidth() / thumbnail.getHeight()) * THUMBNAIL_HEIGHT));

            thumbnail = Bitmap.createScaledBitmap(thumbnail, desiredWidth, THUMBNAIL_HEIGHT, true);

            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setImageBitmap(thumbnail);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayImageWithGallery(activity,imgUri);
                }
            });

            if(uriList != null){
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Image Removal");
                        alertBuilder.setMessage("Are you sure to remove the following image from being processed?");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                uriList.remove(imgUri);
                                imgArray.removeView(view);
                            }
                        }).setNegativeButton(android.R.string.no, null);
                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        return true;
                    }
                });
            }

            imgArray.addView(imageView,0);

            if(scrollView != null){
                scrollView.smoothScrollTo(0,0);
            }
        }catch (Exception e){
            Log.println(ASSERT,"Exception", e.toString());
        }
    }

    private static void displayImageWithGallery(Activity activity, Uri imgUri){
        Uri contentUri = null;
        if (imgUri.getScheme().equals("content")) {
            contentUri = imgUri;
        } else {
            // "content://" scheme is required to pass Uri to another app like Gallery
            // can convert this way if the image of that Uri is in one of the directories in file_paths.xml
            contentUri = getUriForFile(activity,
                    "com.bankai.bleach.fileprovider",
                    new File(imgUri.getPath()));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, contentUri);
        intent.setDataAndType(contentUri, "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);

    }

    /**
     * Android camera takes photo in the phone orientation, so you have to read the
     * orientation information to find the rotation then rotate.
     */
    private static Bitmap rotateImageIfRequired(Activity activity, Bitmap img, Uri imageUri) throws IOException {
        int rotation;
        InputStream in = activity.getContentResolver().openInputStream(imageUri);
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



    public static File getEmptyFileThatIsNotCreated() {
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
}
