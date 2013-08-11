package org.restdoc.server.impl;

import java.lang.reflect.Type;

import org.restdoc.api.ParamDefinition;
import org.restdoc.api.Schema;

/**
 * 
 */
public class MyExt extends RestDocGeneratorExtensionAdapter {
	
	@Override
	public void pathParam(final String name, final ParamDefinition definition, final Type paramType, final AnnotationMap map) {
		if (paramType instanceof Class) {
			Class<?> clz = (Class<?>) paramType;
			definition.setAdditionalField("javaClass", clz.getCanonicalName());
		}
	}
	
	@Override
	public void newSchema(final String schemaURI, final Schema s, final Class<?> schemaClass) {
		s.setAdditionalField("javaClass", schemaClass.getCanonicalName());
	}
	
}
