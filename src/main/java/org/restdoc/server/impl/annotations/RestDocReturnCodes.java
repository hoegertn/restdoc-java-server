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
public @interface RestDocReturnCodes {
	
	/**
	 * @return the array of return codes
	 */
	RestDocReturnCode[] value();
	
}
