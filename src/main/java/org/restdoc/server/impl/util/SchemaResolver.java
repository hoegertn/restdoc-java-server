package org.restdoc.server.impl.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.restdoc.api.Schema;
import org.restdoc.api.util.RestDocParser;
import org.restdoc.server.impl.IRestDocGeneratorExtension;
import org.restdoc.server.impl.RestDocException;
import org.restdoc.server.impl.annotations.RestDocSchema;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

/**
 * Copyright 2013 Cinovo AG<br>
 * <br>
 * 
 * @author Thorsten Hoeger
 * 
 */
public final class SchemaResolver {
	
	private static final ObjectMapper mapper = RestDocParser.createMapper();
	
	
	private SchemaResolver() {
		// private utility class constructor
	}
	
	/**
	 * same as getSchemaFromClassOrNull but throws {@link RestDocException} if no schema is found
	 * 
	 * @param type the type to scan
	 * @param schemaMap the map to add the schema to
	 * @param ext the IRestDocGeneratorExtension to invoke on new schema
	 * @return the schema URI
	 */
	public static String getSchemaFromType(final Type type, Map<String, Schema> schemaMap, IRestDocGeneratorExtension ext) {
		String schema = SchemaResolver.getSchemaFromTypeOrNull(type, schemaMap, ext);
		if (schema != null) {
			return schema;
		}
		final String s = String.format("SchemaType %s is not annotated with RestDocSchema.", type);
		throw new RestDocException(s);
	}
	
	/**
	 * @param type the type to scan
	 * @param schemaMap the map to add the schema to
	 * @param ext the IRestDocGeneratorExtension to invoke on new schema
	 * @return the schema URI
	 */
	public static String getSchemaFromTypeOrNull(final Type type, Map<String, Schema> schemaMap, IRestDocGeneratorExtension ext) {
		if (type instanceof Class) {
			return SchemaResolver.getSchemaFromClassOrNull((Class<?>) type, schemaMap, ext);
		}
		// find generics
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type rawType = pt.getRawType(); // the surrounding type
			Type[] arguments = pt.getActualTypeArguments();
			
			if (rawType instanceof Class) {
				Class<?> rawClass = (Class<?>) rawType;
				// something like Collection<T>
				if (Collection.class.isAssignableFrom(rawClass) && (arguments.length == 1)) {
					String nestedSchema = SchemaResolver.getSchemaFromTypeOrNull(arguments[0], schemaMap, ext);
					return nestedSchema + "[]";
				}
				if (Map.class.isAssignableFrom(rawClass)) {
					return "object";
				}
			}
		}
		return null;
	}
	
	private static String getSchemaFromClassOrNull(final Class<?> schemaClass, Map<String, Schema> schemaMap, IRestDocGeneratorExtension ext) {
		if (schemaClass.isArray()) {
			return SchemaResolver.getSchemaFromClassOrNull(schemaClass.getComponentType(), schemaMap, ext) + "[]";
		}
		if (schemaClass.isAssignableFrom(String.class)) {
			return "string";
		}
		if (schemaClass.isAssignableFrom(Integer.class) || schemaClass.equals(int.class)) {
			return "integer";
		}
		if (schemaClass.isAssignableFrom(Long.class) || schemaClass.equals(long.class)) {
			return "long";
		}
		if (schemaClass.isAssignableFrom(Boolean.class) || schemaClass.equals(boolean.class)) {
			return "boolean";
		}
		if (schemaClass.isAssignableFrom(Double.class) || schemaClass.equals(double.class)) {
			return "double";
		}
		if (schemaClass.isAssignableFrom(BigDecimal.class)) {
			return "double";
		}
		if (schemaClass.isAnnotationPresent(RestDocSchema.class)) {
			final RestDocSchema docSchema = schemaClass.getAnnotation(RestDocSchema.class);
			final String schemaURI = docSchema.value();
			if (!schemaMap.containsKey(schemaURI)) {
				try {
					JsonSchemaGenerator gen = new JsonSchemaGenerator(SchemaResolver.mapper);
					final JsonSchema schema = gen.generateSchema(schemaClass);
					final Schema s = new Schema();
					s.setSchema(schema);
					schemaMap.put(schemaURI, s);
					if (ext != null) {
						ext.newSchema(schemaURI, s, schemaClass);
					}
				} catch (final JsonMappingException e) {
					throw new RestDocException("Error creating schema for URI: " + schemaURI, e);
				}
			}
			return schemaURI;
		}
		return null;
	}
}
