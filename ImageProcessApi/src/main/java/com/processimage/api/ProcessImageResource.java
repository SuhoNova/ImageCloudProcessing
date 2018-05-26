package com.processimage.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;  
/**
 * 
 * This is a ProcessImage Resource class that the Application class redirects to
 * if the URI path has $PATH + "/process"
 * 
 * For that path, it offers a GET request that sends back a string that confirms the connection of the request
 * And a PATH request that handles the image processing (blur)
 * 
 * Remote processing time is measured at the start of this function and ends before a Response is sent
 * Blurred image is sent back in a Response as a byte array
 *
 */
@Path("/process")
public class ProcessImageResource {
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/jpeg")
	public Response processImage(MultipartFormDataInput multipart) throws IOException {
		long begin = System.currentTimeMillis();

		InputStream inputStream = multipart.getFormDataPart("file", InputStream.class, null);
		int blurSigma = multipart.getFormDataPart("sigma", Integer.class, null);

        try {
        		BufferedImage image = ImageIO.read(inputStream);
            	BufferedImage processedImage = BlurImage.blurImage(image, blurSigma);
            	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	ImageIO.write(processedImage, "jpeg", baos );
            	baos.flush();
            	byte[] imageInByte = baos.toByteArray();
            	baos.close();
            	
            	long end = System.currentTimeMillis();
        		
            	double processTimeTakenInSeconds = (double)(end - begin) / 1000;
            	DecimalFormat decimalFormat = new DecimalFormat("#.000");
            	decimalFormat.format(processTimeTakenInSeconds);
            	
            	return Response.ok(imageInByte).header("remoteTime", processTimeTakenInSeconds).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkConnection() {
		return Response.ok().entity("Connection Successful!").build();
	}
}
