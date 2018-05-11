package com.bankai.bleach.imageprocessor;
import java.io.File;
import android.os.Environment;


public class ProcessingFunctions {

    public static File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separatorChar+"ImageProc");

   // TODO Add BoofCV functions

}
