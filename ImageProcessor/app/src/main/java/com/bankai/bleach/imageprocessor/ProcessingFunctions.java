package com.bankai.bleach.imageprocessor;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Size;
import org.opencv.core.Core;
public class ProcessingFunctions {

    public static void GaussianBlur(String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName,0);
        Mat result = new Mat();
        Imgproc.GaussianBlur(img, result, new Size(4, 4) ,2 ,2);
        Imgcodecs.imwrite(imageName, result);
    }

    public static void SobelEdge (String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName,0);
        Mat result = new Mat();
        Imgproc.Sobel(img,result,50,300,100);
        Imgcodecs.imwrite(imageName, result);
    }

    public static void CannyContour (String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName,0);
        Mat result = new Mat();
        Imgproc.Canny(img,result,53.0,20.0);
        Imgcodecs.imwrite(imageName, result);
    }

}
