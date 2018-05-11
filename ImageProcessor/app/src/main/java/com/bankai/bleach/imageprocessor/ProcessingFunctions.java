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

   // TODO Add BoofCV functions

}
