package com.bankai.bleach.imageprocessor;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Size;
import org.opencv.core.Core;
public class ProcessingFunctions {

    public static void GaussianBlur(String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName);
        Mat result = new Mat();
        Imgproc.GaussianBlur(img, result, new Size(4, 4) ,2 ,2);
        Imgcodecs.imwrite(imageName, result);
    }

    public static void SobelEdge (String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName);
        Mat result = new Mat();
        Imgproc.Sobel(img,result,50,300,100);
        Imgcodecs.imwrite(imageName, result);
    }

    public static void CannyContour (String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName);
        Mat result = new Mat();
        Imgproc.Canny(img,result,53.0,20.0);
        Imgcodecs.imwrite(imageName, result);
    }

    public static void CombinationTransform (String imageName){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread(imageName);
        Mat result1 = new Mat();
        Imgproc.GaussianBlur(img, result1, new Size(4, 4) ,2 ,2);
        Mat result2 = new Mat();
        Imgproc.Sobel(result1,result2,50,300,100);
        Mat result3 = new Mat();
        Imgproc.Canny(result2,result3,53.0,20.0);
        Imgcodecs.imwrite(imageName, result3);
    }

}
