package org.restdoc.server.impl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author thoeger
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RestDoc {

	/**
	 * @return the description of the resource method
	 */
	String methodDescription();

	/**
	 * @return the id of the resource
	 */
	String id() default "";

	/**
	 * @return the description of the resource
	 */
	String resourceDescription() default "";

}
