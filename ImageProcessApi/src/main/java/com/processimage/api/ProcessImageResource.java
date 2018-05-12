package com.processimage.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;  

@Path("/process")
public class ProcessImageResource {
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response processImage(MultipartFormDataInput multipart) throws IOException {
        try {
        	BufferedImage image = ImageIO.read(multipart.getFormDataPart("file", InputStream.class, null));
            if(image == null) {
            	return Response.status(400).build();
            } else {
            	BufferedImage processedImage = BlurImage.blurImage(image);
            	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	ImageIO.write(processedImage, "jpg", baos );
            	baos.flush();
            	byte[] imageInByte = baos.toByteArray();
            	baos.close();
            	
            	return Response.ok(imageInByte).build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	@GET
	public Response checkConnection() {
		return Response.ok().build();
	}
}
