package org.restdoc.server.impl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author thoeger
 * 
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestDocType {
	
	/**
	 * @return the response type e.g. text/plain or application/json
	 */
	String type();
	
	/**
	 * @return the schema URI
	 */
	String schema() default "";
	
	/**
	 * @return the schema Class
	 */
	Class<?> schemaClass() default Object.class;
	
}
