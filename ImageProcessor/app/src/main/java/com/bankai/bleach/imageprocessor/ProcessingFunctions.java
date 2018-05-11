package com.bankai.bleach.imageprocessor;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Size;
import org.opencv.core.Core;
import java.util.ArrayList;
import java.io.File;
import android.os.Environment;
import android.util.Log;

import static android.util.Log.ASSERT;

public class ProcessingFunctions {

    public static File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separatorChar+"ImageProc");

    public static void GaussianBlur(ArrayList<String> array){
        Log.println(ASSERT,"string", photoDirectory.getAbsolutePath());
        String imageName = array.get(0);
        Log.println(ASSERT,"string", imageName);
            Mat img = Imgcodecs.imread(imageName);
            Mat result = new Mat();
            Imgproc.GaussianBlur(img, result, new Size(4, 4) ,2 ,2);
            File file = new File(photoDirectory+imageName);
            Imgcodecs.imwrite(file.toString(), result);
    }

    public static void SobelEdge (ArrayList<String> array){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String imageName = array.get(0);
        Mat img = Imgcodecs.imread(imageName);
        Mat result = new Mat();
        Imgproc.Sobel(img,result,50,300,100);
        File file = new File(photoDirectory+imageName);
        Imgcodecs.imwrite(file.toString(), result);
    }

    public static void CannyContour (ArrayList<String> array){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String imageName = array.get(0);
        Mat img = Imgcodecs.imread(imageName);
        Mat result = new Mat();
        Imgproc.Canny(img,result,53.0,20.0);
        File file = new File(photoDirectory+imageName);
        Imgcodecs.imwrite(file.toString(), result);
    }

    public static void CombinationTransform (ArrayList<String> array){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String imageName = array.get(0);
        Mat img = Imgcodecs.imread(imageName);
        Mat result1 = new Mat();
        Imgproc.GaussianBlur(img, result1, new Size(4, 4) ,2 ,2);
        Mat result2 = new Mat();
        Imgproc.Sobel(result1,result2,50,300,100);
        Mat result3 = new Mat();
        Imgproc.Canny(result2,result3,53.0,20.0);
        File file = new File(photoDirectory+imageName);
        Imgcodecs.imwrite(file.toString(), result3);
    }

}
