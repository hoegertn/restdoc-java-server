package org.restdoc.server.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.restdoc.api.HeaderDefinition;
import org.restdoc.api.MethodDefinition;
import org.restdoc.api.ParamDefinition;
import org.restdoc.api.RestDoc;
import org.restdoc.api.RestResource;
import org.restdoc.api.Schema;

/**
 * 
 */
public class RestDocGeneratorExtensionAdapter implements IRestDocGeneratorExtension {
	
	@Override
	public void newResource(final RestResource restResource) {
		//
	}
	
	@Override
	public void queryParam(final String name, final ParamDefinition definition, final Type paramType, final AnnotationMap map) {
		//
	}
	
	@Override
	public void pathParam(final String name, final ParamDefinition definition, final Type paramType, final AnnotationMap map) {
		//
	}
	
	@Override
	public void headerParam(final String name, final HeaderDefinition definition, final Type paramType, final AnnotationMap map) {
		//
	}
	
	@Override
	public void newMethod(final RestResource restResource, final MethodDefinition def, final Method method) {
		//
	}
	
	@Override
	public void newSchema(final String schemaURI, final Schema s, final Class<?> schemaClass) {
		//
	}
	
	@Override
	public void renderDoc(final String path, final RestDoc doc) {
		//
	}
	
}
