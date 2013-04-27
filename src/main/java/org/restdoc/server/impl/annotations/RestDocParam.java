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
public @interface RestDocParam {
	
	/**
	 * @return the description of the parameter
	 */
	String description();
	
	/**
	 * @return the parameter validations
	 */
	RestDocValidation[] validations() default {};
	
}
