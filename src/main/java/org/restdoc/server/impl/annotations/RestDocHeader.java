package org.restdoc.server.impl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HeaderParam;

/**
 * @author thoeger
 * 
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestDocHeader {
	
	/**
	 * @return the description of the header
	 */
	String description();
	
	/**
	 * @return if this header is mandatory
	 */
	boolean required() default false;
	
	/**
	 * @return the name of this header if not already defined by {@link HeaderParam}
	 */
	String name() default "";
	
}
