package org.restdoc.server.impl;

import org.restdoc.api.ParamDefinition;
import org.restdoc.api.Schema;

/**
 * 
 */
public class MyExt extends RestDocGeneratorExtensionAdapter {

	@Override
	public void pathParam(final String name, final ParamDefinition definition, final Class<?> paramType, final AnnotationMap map) {
		definition.setAdditionalField("javaClass", paramType.getCanonicalName());
	}

	@Override
	public void newSchema(final String schemaURI, final Schema s, final Class<?> schemaClass) {
		s.setAdditionalField("javaClass", schemaClass.getCanonicalName());
	}

}
