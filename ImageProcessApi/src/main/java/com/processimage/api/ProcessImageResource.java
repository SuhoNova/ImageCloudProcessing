package com.processimage.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;  

@Path("/process")
public class ProcessImageResource {
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/jpeg")
	public byte[] processImage(MultipartFormDataInput multipart) throws IOException {
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
            	
            	return imageInByte;
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
