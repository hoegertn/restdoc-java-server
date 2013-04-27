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
public @interface RestDocReturnCode {
	
	/**
	 * @return the array of response types
	 */
	String code();
	
	/**
	 * @return the description of the return code
	 */
	String description();
	
}
