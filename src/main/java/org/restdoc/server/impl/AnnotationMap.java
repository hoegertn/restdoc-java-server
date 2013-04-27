package org.restdoc.server.impl;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * a map of annotations
 */
public class AnnotationMap {
	
	final Map<Class<? extends Annotation>, Annotation> paMap = new HashMap<Class<? extends Annotation>, Annotation>();
	
	
	/**
	 * @param annotations the annotations
	 */
	public AnnotationMap(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			this.paMap.put(annotation.annotationType(), annotation);
		}
	}
	
	/**
	 * @param a
	 * @return true if this map contains given annotation
	 */
	public boolean hasAnnotation(final Class<? extends Annotation> a) {
		return this.paMap.containsKey(a);
	}
	
	/**
	 * @param type
	 * @return the found annotation or null
	 */
	public <T extends Annotation> T getAnnotation(final Class<T> type) {
		final Annotation annotation = this.paMap.get(type);
		if (type.isInstance(annotation)) {
			return type.cast(annotation);
		}
		return null;
	}
	
}