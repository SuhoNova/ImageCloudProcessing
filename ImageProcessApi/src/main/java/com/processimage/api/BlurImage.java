package com.processimage.api;

import java.awt.image.BufferedImage;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;

public class BlurImage {
	public static BufferedImage blurImage(BufferedImage imageToBlur, int sigma){
		GrayU8 image = new GrayU8(imageToBlur.getWidth(),imageToBlur.getHeight());
		
		ConvertBufferedImage.convertFrom(imageToBlur, image);

        GrayU8 blurredGrayU8Img = blurring(image, sigma);
        
        BufferedImage blurredBuffImg = new BufferedImage(imageToBlur.getWidth(), imageToBlur.getHeight(), imageToBlur.getType()); 
        
        ConvertBufferedImage.convertTo(blurredGrayU8Img, blurredBuffImg);
        return blurredBuffImg;
    }
	
	public static GrayU8 blurring( GrayU8 image, int sigma) {
		GrayU8 output = new GrayU8(image.width,image.height);
		
		BlurImageOps.gaussian(image, output, sigma, -1, null);
		
		return output;
	}
}