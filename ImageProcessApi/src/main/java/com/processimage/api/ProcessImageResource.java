package com.processimage.api;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.*;  

@Path("/process")
public class ProcessImageResource {
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.MULTIPART_FORM_DATA)
	public Response processImage(InputStream inputStream) throws IOException {
//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//		dateFormat.format(new Date());
		
		//byte[] imageData = IOUtils.toByteArray(inputStream);
        BufferedImage image = ImageIO.read(inputStream);
        if(image == null) {
        	return Response.ok("ccopommmem on").build();
        }
		//byte[] imageData = IOUtils.toByteArray(inputStream);
		//InputStream in = new ByteArrayInputStream(imageData);
		//BufferedImage bImageFromConvert = ImageIO.read(in);
		//BufferedImage processedImage = BlurImage.blurImage(image);
		return Response.ok(image).build();
		/*
		byte[] imageData = IOUtils.toByteArray(inputStream);
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

		inputStream.close();
		//BufferedImage processedImage = BlurImage.blurImage(image);
		
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    //ImageIO.write(image, "png", baos);
	    //byte[] imageData = baos.toByteArray();
		
		return Response.ok(image).build();
		//*/
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkConnection() {
		Response response = Response.status(200).entity("helpmeplz").type(MediaType.APPLICATION_JSON)
				.build();
		
		return response;
	}
}
