package com.bankai.bleach.imageprocessor;

import android.graphics.Bitmap;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.android.ConvertBitmap;
import boofcv.struct.image.GrayU8;


public class Processing {

    public static Bitmap gaussianBlur(Bitmap imageToBlur){
        GrayU8 image = ConvertBitmap.bitmapToGray(imageToBlur, (GrayU8)null, null);

        GrayU8 blurred = new GrayU8(imageToBlur.getWidth(),imageToBlur.getHeight());

        BlurImageOps.gaussian(image,blurred,30, -1,null);

        return ConvertBitmap.grayToBitmap(blurred, Bitmap.Config.ARGB_8888);
    }

}
