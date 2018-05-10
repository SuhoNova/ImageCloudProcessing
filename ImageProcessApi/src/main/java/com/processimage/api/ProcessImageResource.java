package com.processimage.api;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

@Path("/process")
public class ProcessImageResource {
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.MULTIPART_FORM_DATA)
	public Response processImage(InputStream imageStream) {
		if(imageStream == null) {	
			return Response.status(404).build();
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat.format(new Date());

		byte[] processedImage;
		try {
			processedImage = IOUtils.toByteArray(imageStream);
		} catch (IOException e) {
			processedImage = null;
			//return Response.status(404).build();
		}

		Response response = Response.ok().entity(processedImage)
				.build();
		
		
		return response;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkConnection() {
		Response response = Response.status(200).entity("helpmeplz").type(MediaType.APPLICATION_JSON)
				.build();
		
		return response;
	}
}
