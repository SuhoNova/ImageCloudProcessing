package com.processimage.api;

import java.util.HashSet;

/**
 * This is an application class that handles the request sent to 
 * $PATH + "/services"
 * 
 * It contains the available Resources class that it can redirect 
 * the request to the correct Resource class depending on the URI.
 * 
 */
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/services")
public class ProcessImageApplication extends Application {
	  private Set<Object> singletons = new HashSet<Object>();
	  private Set<Class<?>> classes = new HashSet<Class<?>>();

	   public ProcessImageApplication()
	   {
		   ProcessImageResource resource = new ProcessImageResource();
		   
		   singletons.add(resource);
	      
		   classes.add(ProcessImageResource.class);
	   }

	   @Override
	   public Set<Object> getSingletons()
	   {
	      return singletons;
	   }
	   
	   @Override
	   public Set<Class<?>> getClasses()
	   {
	      return classes;
	   }
}
