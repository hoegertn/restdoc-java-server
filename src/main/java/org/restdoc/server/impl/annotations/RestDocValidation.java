package org.restdoc.server.impl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author thoeger
 * 
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestDocValidation {
	
	/**
	 * @return the validation type
	 */
	String type();
	
	/**
	 * @return the validation pattern
	 */
	String pattern();
	
}
