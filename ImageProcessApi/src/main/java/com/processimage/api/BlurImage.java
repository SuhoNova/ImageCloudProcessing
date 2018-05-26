package com.processimage.api;

import java.awt.image.BufferedImage;

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
/**
 * This is a static class that blurs an image
 * It takes a bufferedimage and a sigma value as input
 * and gives back a blurred bufferedimage
 * 
 * It uses an external library called Boofcv to handle the image processing
 * and coverting to required image data structure (GrayU8) before processing 
 *
 */
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