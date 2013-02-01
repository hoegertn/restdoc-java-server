package org.restdoc.server.impl;

import java.lang.reflect.Method;

import org.restdoc.api.HeaderDefinition;
import org.restdoc.api.MethodDefinition;
import org.restdoc.api.ParamDefinition;
import org.restdoc.api.RestDoc;
import org.restdoc.api.RestResource;
import org.restdoc.api.Schema;

/**
 * 
 */
public interface IRestDocGeneratorExtension {

	/**
	 * invoked on every new rest resource
	 * 
	 * @param restResource
	 *            the {@link RestResource}
	 */
	void newResource(RestResource restResource);

	/**
	 * invoked for every query parameter
	 * 
	 * @param name
	 *            the parameter name
	 * @param definition
	 *            the {@link ParamDefinition}
	 * @param paramType
	 *            the Java class of the parameter
	 * @param map
	 *            the map of annotations
	 */
	void queryParam(String name, ParamDefinition definition, Class<?> paramType, AnnotationMap map);

	/**
	 * invoked for every path parameter
	 * 
	 * @param name
	 *            the parameter name
	 * @param definition
	 *            the {@link ParamDefinition}
	 * @param paramType
	 *            the Java class of the parameter
	 * @param map
	 *            the map of annotations
	 */
	void pathParam(String name, ParamDefinition definition, Class<?> paramType, AnnotationMap map);

	/**
	 * invoked for every header parameter
	 * 
	 * @param name
	 *            the parameter name
	 * @param definition
	 *            the {@link HeaderDefinition}
	 * @param paramType
	 *            the Java class of the parameter
	 * @param map
	 *            the map of annotations
	 */
	void headerParam(String name, HeaderDefinition definition, Class<?> paramType, AnnotationMap map);

	/**
	 * invoked for every method
	 * 
	 * @param restResource
	 *            the super resource
	 * @param def
	 *            the {@link MethodDefinition}
	 * @param method
	 *            the Java reflection {@link Method}
	 */
	void newMethod(RestResource restResource, MethodDefinition def, Method method);

	/**
	 * invoked for every used schema class
	 * 
	 * @param schemaURI
	 *            the schema URI
	 * @param s
	 *            the {@link Schema}
	 * @param schemaClass
	 *            the Java class
	 */
	void newSchema(String schemaURI, Schema s, Class<?> schemaClass);

	/**
	 * invoked of RestDoc generation
	 * 
	 * @param path
	 *            the requested path
	 * @param doc
	 *            the generated doc
	 */
	void renderDoc(String path, RestDoc doc);

}
