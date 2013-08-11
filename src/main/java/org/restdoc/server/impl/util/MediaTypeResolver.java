package org.restdoc.server.impl.util;

import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * Copyright 2013 Cinovo AG<br>
 * <br>
 * 
 * @author Thorsten Hoeger
 * 
 */
public final class MediaTypeResolver {
	
	private MediaTypeResolver() {
		// private utility class constructor
	}
	
	/**
	 * @param method the method to scan
	 * @return the declared produces types
	 */
	public static String[] getProducesMediaType(Method method) {
		Produces produces = method.getAnnotation(Produces.class);
		if (produces == null) {
			produces = method.getDeclaringClass().getAnnotation(Produces.class);
		}
		if (produces != null) {
			return produces.value();
		}
		return null;
	}
	
	/**
	 * @param method the method to scan
	 * @return the declared consumes types
	 */
	public static String[] getConsumesMediaType(Method method) {
		Consumes consumes = method.getAnnotation(Consumes.class);
		if (consumes == null) {
			consumes = method.getDeclaringClass().getAnnotation(Consumes.class);
		}
		if (consumes != null) {
			return consumes.value();
		}
		return null;
	}
}
