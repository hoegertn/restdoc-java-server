package org.restdoc.server.impl;

import java.util.HashMap;

import org.restdoc.api.RestResource;
import org.restdoc.api.Schema;

/**
 * This class provide methods to retrieve the RestDoc without parsing the Annotations
 * 
 * @author thoeger
 * 
 */
public interface IProvideRestDoc {

	/**
	 * @return the array of resources this class provides
	 */
	RestResource[] getRestDocResources();

	/**
	 * @return a map of provided schemas
	 */
	HashMap<String, Schema> getRestDocSchemas();

}
