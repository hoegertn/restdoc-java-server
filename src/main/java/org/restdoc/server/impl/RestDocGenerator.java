package org.restdoc.server.impl;

/*
 * #%L Java Server implementation %% Copyright (C) 2012 RestDoc org %% Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License. #L%
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.BeanParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.restdoc.annotations.RestDocAccept;
import org.restdoc.annotations.RestDocHeader;
import org.restdoc.annotations.RestDocIgnore;
import org.restdoc.annotations.RestDocParam;
import org.restdoc.annotations.RestDocResponse;
import org.restdoc.annotations.RestDocReturnCode;
import org.restdoc.annotations.RestDocReturnCodes;
import org.restdoc.annotations.RestDocType;
import org.restdoc.annotations.RestDocValidation;
import org.restdoc.api.GlobalHeader;
import org.restdoc.api.HeaderDefinition;
import org.restdoc.api.MethodDefinition;
import org.restdoc.api.ParamDefinition;
import org.restdoc.api.ParamValidation;
import org.restdoc.api.Representation;
import org.restdoc.api.ResponseDefinition;
import org.restdoc.api.RestDoc;
import org.restdoc.api.RestResource;
import org.restdoc.api.Schema;
import org.restdoc.api.util.RestDocParser;
import org.restdoc.server.impl.util.MediaTypeResolver;
import org.restdoc.server.impl.util.SchemaResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Use this class to generate RestDoc
 */
public class RestDocGenerator {
	
	private static final String VALIDATION_MATCH = "match";
	
	private static final String PATTERN_BOOL = "true|false";
	
	private static final String PATTERN_SIGNED_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+";
	
	private static final String PATTERN_SIGNED_INT = "[-+]?[0-9]+";
	
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, RestResource> resources = Maps.newHashMap();
	
	private final Map<String, HeaderDefinition> requestHeaderMap = Maps.newConcurrentMap();
	
	private final Map<String, HeaderDefinition> responseHeaderMap = Maps.newConcurrentMap();
	
	private final Map<String, Schema> schemaMap = Maps.newConcurrentMap();
	
	private final Map<String, Object> globalAdditional = Maps.newConcurrentMap();
	
	private final RDGEWrapper ext = new RDGEWrapper();
	
	
	/**
	 * initialize the RestDoc Generator
	 * 
	 * @param classes the array of JAX-RS classes
	 * @param globalHeader the global headers
	 * @param baseURI an optional base uri like "/api"
	 */
	public void init(final Class<?>[] classes, final GlobalHeader globalHeader, final String baseURI) {
		if (!this.initialized.compareAndSet(false, true)) {
			throw new RestDocException("Generator already initialized");
		}
		this.logger.info("Starting generation of RestDoc");
		this.logger.info("Searching for RestDoc API classes");
		
		if (globalHeader != null) {
			if (globalHeader.getRequestHeader() != null) {
				this.requestHeaderMap.putAll(globalHeader.getRequestHeader());
			}
			if (globalHeader.getResponseHeader() != null) {
				this.responseHeaderMap.putAll(globalHeader.getResponseHeader());
			}
			if ((globalHeader.getAdditionalFields() != null) && !globalHeader.getAdditionalFields().isEmpty()) {
				this.globalAdditional.putAll(globalHeader.getAdditionalFields());
			}
		}
		
		for (final Class<?> apiClass : classes) {
			// check if class provides predefined RestDoc
			boolean scanNeeded = true;
			if (Arrays.asList(apiClass.getInterfaces()).contains(IProvideRestDoc.class)) {
				try {
					this.logger.info("Class {} provides predefined RestDoc", apiClass.getCanonicalName());
					final IProvideRestDoc apiObject = (IProvideRestDoc) apiClass.newInstance();
					final RestResource[] restDocResources = apiObject.getRestDocResources();
					for (final RestResource restResource : restDocResources) {
						this.resources.put(restResource.getPath(), restResource);
					}
					this.schemaMap.putAll(apiObject.getRestDocSchemas());
					scanNeeded = false;
				} catch (final Exception e) {
					// ignore it and fall back to annotation scan
				}
			}
			if (scanNeeded) {
				this.addResourcesOfClass(apiClass, baseURI);
			}
		}
	}
	
	private void addResourcesOfClass(final Class<?> apiClass, final String baseURI) {
		this.logger.info("Scanning class: {}", apiClass.getCanonicalName());
		
		String basepath = baseURI != null ? baseURI : "";
		if (apiClass.isAnnotationPresent(Path.class)) {
			final Path path = apiClass.getAnnotation(Path.class);
			basepath += path.value();
		}
		
		// find methods
		final Method[] methods = apiClass.getMethods();
		for (final Method method : methods) {
			if (method.isAnnotationPresent(Path.class) || (RestDocGenerator.getHTTPVerb(method) != null)) {
				this.logger.debug("Generating RestDoc of method: " + method.toString());
				this.addResourceMethod(basepath, method);
			}
		}
	}
	
	private void addResourceMethod(final String basepath, final Method method) {
		if (method.isAnnotationPresent(RestDocIgnore.class)) {
			this.logger.info("Ignoring method: " + method);
			return;
		}
		
		// get needed annotations from method
		final String methodType = RestDocGenerator.getHTTPVerb(method);
		final org.restdoc.annotations.RestDoc docAnnotation = method.getAnnotation(org.restdoc.annotations.RestDoc.class);
		final Path pathAnnotation = method.getAnnotation(Path.class);
		
		String path = basepath;
		if (pathAnnotation != null) {
			path += pathAnnotation.value();
		}
		
		if ((methodType == null) && (pathAnnotation != null)) {
			this.addResourcesOfClass(method.getReturnType(), path);
			return;
		}
		
		// get parameter
		final Type[] parameterTypes = method.getGenericParameterTypes();
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		
		// values from parameters
		final List<String> queryParams = Lists.newArrayList();
		final HashMap<String, HeaderDefinition> methodRequestHeader = Maps.newHashMap();
		final HashMap<String, ParamDefinition> methodParams = Maps.newHashMap();
		for (int i = 0; i < parameterTypes.length; i++) {
			this.parseMethodParameter(queryParams, methodRequestHeader, methodParams, parameterTypes[i], parameterAnnotations[i]);
		}
		
		for (final String string : queryParams) {
			path += "{?" + string + "}";
		}
		
		final String id;
		if ((docAnnotation != null) && (docAnnotation.id() != null) && !docAnnotation.id().isEmpty()) {
			id = docAnnotation.id();
		} else {
			id = this.getDefaultResourceId(method, path);
		}
		final String resourceDescription = (docAnnotation != null) ? docAnnotation.resourceDescription() : null;
		
		final String methodDescription = (docAnnotation != null) ? docAnnotation.methodDescription() : null;
		if (methodType == null) {
			throw new RestDocException("No suitable method found for method: " + method.toString());
		}
		
		RestResource restResource = this.resources.get(path);
		if (restResource == null) {
			restResource = new RestResource();
			restResource.setPath(path);
			this.resources.put(path, restResource);
		}
		if ((restResource.getId() == null) || restResource.getId().isEmpty()) {
			restResource.setId(id);
			restResource.setDescription(resourceDescription);
			restResource.getParams().putAll(methodParams);
			this.ext.newResource(restResource);
		}
		
		if (restResource.getMethods().containsKey(methodType)) {
			this.logger.warn("Duplicate method detected for resource: " + path + " -> " + methodType);
			return;
		}
		
		final MethodDefinition def = new MethodDefinition();
		def.setDescription(methodDescription);
		def.getHeaders().putAll(methodRequestHeader);
		def.getAccepts().addAll(this.getAccepts(method, parameterTypes, parameterAnnotations));
		def.getStatusCodes().putAll(this.getStatusCodes(method));
		def.setResponse(this.getMethodResponse(method));
		
		restResource.getMethods().put(methodType, def);
		
		this.ext.newMethod(restResource, def, method);
	}
	
	private String getDefaultResourceId(final Method method, String path) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(path.getBytes("UTF-8"));
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);
			// Now we need to zero pad it if you actually want the full 32 chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			this.logger.warn("Failed to generate MD5 sum", e);
		} catch (UnsupportedEncodingException e) {
			this.logger.warn("Failed to generate MD5 sum", e);
		}
		return method.getName();
	}
	
	/**
	 * @param queryParams
	 * @param methodRequestHeader
	 * @param methodParams
	 * @param paramType
	 * @param paramAnnotations
	 */
	protected void parseMethodParameter(final List<String> queryParams, final Map<String, HeaderDefinition> methodRequestHeader, final Map<String, ParamDefinition> methodParams, final Type paramType, final Annotation[] paramAnnotations) {
		final HeaderDefinition headerDefinition = new HeaderDefinition();
		
		final AnnotationMap map = new AnnotationMap(paramAnnotations);
		
		if (map.hasAnnotation(QueryParam.class)) {
			final String name = map.getAnnotation(QueryParam.class).value();
			queryParams.add(name);
			final ParamDefinition definition = new ParamDefinition();
			if (map.hasAnnotation(RestDocParam.class)) {
				this.parseRestDocParameter(definition, map.getAnnotation(RestDocParam.class), paramType);
			}
			this.ext.queryParam(name, definition, paramType, map);
			methodParams.put(name, definition);
		} else if (map.hasAnnotation(PathParam.class)) {
			final String name = map.getAnnotation(PathParam.class).value();
			final ParamDefinition definition = new ParamDefinition();
			if (map.hasAnnotation(RestDocParam.class)) {
				this.parseRestDocParameter(definition, map.getAnnotation(RestDocParam.class), paramType);
			}
			this.ext.pathParam(name, definition, paramType, map);
			methodParams.put(name, definition);
		} else if (map.hasAnnotation(HeaderParam.class)) {
			final String name = map.getAnnotation(HeaderParam.class).value();
			final HeaderDefinition definition = new HeaderDefinition();
			if (!this.requestHeaderMap.containsKey(name)) {
				if (map.hasAnnotation(RestDocHeader.class)) {
					final RestDocHeader docHeader = map.getAnnotation(RestDocHeader.class);
					headerDefinition.setDescription(docHeader.description());
					headerDefinition.setRequired(docHeader.required());
				}
				this.ext.headerParam(name, definition, paramType, map);
				methodRequestHeader.put(name, headerDefinition);
			}
		} else if (map.hasAnnotation(BeanParam.class)) {
			if (paramType instanceof Class) {
				Class<?> beanParamClass = (Class<?>) paramType;
				Field[] fields = beanParamClass.getDeclaredFields();
				for (Field f : fields) {
					this.parseMethodParameter(queryParams, methodRequestHeader, methodParams, f.getType(), f.getAnnotations());
				}
			}
		} else {
			// Param is body type
		}
	}
	
	private ResponseDefinition getMethodResponse(final Method method) {
		final ResponseDefinition def = new ResponseDefinition();
		if (method.isAnnotationPresent(RestDocResponse.class)) {
			final RestDocResponse docResponse = method.getAnnotation(RestDocResponse.class);
			final RestDocType[] types = docResponse.types();
			for (final RestDocType restDocType : types) {
				if (!restDocType.schemaClass().equals(Object.class)) {
					final String schema = SchemaResolver.getSchemaFromType(restDocType.schemaClass(), this.schemaMap, this.ext);
					def.type(restDocType.type(), schema);
				} else {
					def.type(restDocType.type(), restDocType.schema());
				}
			}
			
			final RestDocHeader[] headers = docResponse.headers();
			for (final RestDocHeader restDocHeader : headers) {
				def.header(restDocHeader.name(), restDocHeader.description(), restDocHeader.required());
			}
		}
		if (def.getTypes().isEmpty() && !method.getReturnType().equals(Void.TYPE)) {
			final String schema = SchemaResolver.getSchemaFromTypeOrNull(method.getGenericReturnType(), this.schemaMap, this.ext);
			String[] mediaTypes = MediaTypeResolver.getProducesMediaType(method);
			if (mediaTypes != null) {
				for (String mt : mediaTypes) {
					def.type(mt, schema);
				}
			}
		}
		return def;
	}
	
	private Map<String, String> getStatusCodes(final Method method) {
		final Map<String, String> codeMap = Maps.newHashMap();
		if (method.isAnnotationPresent(RestDocReturnCodes.class)) {
			final RestDocReturnCode[] returnCodes = method.getAnnotation(RestDocReturnCodes.class).value();
			for (final RestDocReturnCode rdrc : returnCodes) {
				codeMap.put(rdrc.code(), rdrc.description());
			}
		}
		return codeMap;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Representation> getAccepts(final Method method, Type[] parameterTypes, Annotation[][] parameterAnnotations) {
		final Collection<Representation> list = Lists.newArrayList();
		if (method.isAnnotationPresent(RestDocAccept.class)) {
			final RestDocAccept docAccept = method.getAnnotation(RestDocAccept.class);
			final RestDocType[] types = docAccept.value();
			for (final RestDocType restDocType : types) {
				if (!restDocType.schemaClass().equals(Object.class)) {
					final String schema = SchemaResolver.getSchemaFromType(restDocType.schemaClass(), this.schemaMap, this.ext);
					list.add(new Representation(restDocType.type(), schema));
				} else {
					list.add(new Representation(restDocType.type(), restDocType.schema()));
				}
			}
		} else {
			String[] mediaTypes = MediaTypeResolver.getConsumesMediaType(method);
			if (mediaTypes != null) {
				for (int i = 0; i < parameterTypes.length; i++) {
					Type param = parameterTypes[i];
					final AnnotationMap map = new AnnotationMap(parameterAnnotations[i]);
					if (!map.hasAnnotation(PathParam.class, QueryParam.class, HeaderParam.class)) {
						final String schema = SchemaResolver.getSchemaFromType(param, this.schemaMap, this.ext);
						for (String mt : mediaTypes) {
							list.add(new Representation(mt, schema));
						}
					}
				}
			}
		}
		return list;
	}
	
	private void parseRestDocParameter(final ParamDefinition definition, final RestDocParam docParam, final Type paramType) {
		definition.setDescription(docParam.description());
		final RestDocValidation[] restDocValidations = docParam.validations();
		for (final RestDocValidation validation : restDocValidations) {
			final ParamValidation v = new ParamValidation();
			v.setType(validation.type());
			v.setPattern(validation.pattern());
			definition.getValidations().add(v);
		}
		if (paramType.equals(Long.class)) {
			definition.getValidations().add(new ParamValidation(RestDocGenerator.VALIDATION_MATCH, RestDocGenerator.PATTERN_SIGNED_INT));
		} else if (paramType.equals(Integer.class)) {
			definition.getValidations().add(new ParamValidation(RestDocGenerator.VALIDATION_MATCH, RestDocGenerator.PATTERN_SIGNED_INT));
		} else if (paramType.equals(Double.class)) {
			definition.getValidations().add(new ParamValidation(RestDocGenerator.VALIDATION_MATCH, RestDocGenerator.PATTERN_SIGNED_DECIMAL));
		} else if (paramType.equals(BigDecimal.class)) {
			definition.getValidations().add(new ParamValidation(RestDocGenerator.VALIDATION_MATCH, RestDocGenerator.PATTERN_SIGNED_DECIMAL));
		} else if (paramType.equals(Boolean.class)) {
			definition.getValidations().add(new ParamValidation(RestDocGenerator.VALIDATION_MATCH, RestDocGenerator.PATTERN_BOOL));
		}
	}
	
	private static String getHTTPVerb(final Method method) {
		final Annotation[] annotations = method.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
				final HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
				return httpMethod.value();
			}
		}
		return null;
	}
	
	// ######################################################
	// Retrieving RestDoc for given path
	// ######################################################
	
	/**
	 * @param path the basepath to start
	 * @return the {@link RestDoc} as string
	 * @throws RestDocException on generation error
	 */
	public String getRestDocStringForPath(final String path) {
		if (!this.initialized.get()) {
			throw new RestDocException("Generator is not yet initialized");
		}
		try {
			final RestDoc doc = this.getDoc(path);
			return RestDocParser.writeRestDoc(doc);
		} catch (final IOException e) {
			throw new RestDocException(e);
		}
	}
	
	private RestDoc getDoc(final String path) {
		final RestDoc doc = new RestDoc();
		// populate schemas
		doc.setSchemas(new HashMap<String, Schema>(this.schemaMap));
		
		// populate header section
		doc.getHeaders().getRequestHeader().putAll(this.requestHeaderMap);
		doc.getHeaders().getResponseHeader().putAll(this.responseHeaderMap);
		doc.getHeaders().getAdditionalFields().putAll(this.globalAdditional);
		
		// populate resource section
		final Set<Entry<String, RestResource>> entrySet = this.resources.entrySet();
		for (final Entry<String, RestResource> entry : entrySet) {
			if (entry.getKey().startsWith(path)) {
				doc.getResources().add(entry.getValue());
			}
		}
		
		this.ext.renderDoc(path, doc);
		
		return doc;
	}
	
	// ##############################################
	// Extension registry and wrapper
	// ##############################################
	
	/**
	 * @param extension the {@link IRestDocGeneratorExtension} to register
	 */
	public void registerGeneratorExtension(final IRestDocGeneratorExtension extension) {
		this.ext.exts.add(extension);
	}
	
	
	private class RDGEWrapper implements IRestDocGeneratorExtension {
		
		private final List<IRestDocGeneratorExtension> exts = new LinkedList<IRestDocGeneratorExtension>();
		
		
		@Override
		public void newResource(final RestResource restResource) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.newResource(restResource);
			}
		}
		
		@Override
		public void queryParam(final String name, final ParamDefinition definition, final Type paramType, final AnnotationMap map) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.queryParam(name, definition, paramType, map);
			}
		}
		
		@Override
		public void pathParam(final String name, final ParamDefinition definition, final Type paramType, final AnnotationMap map) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.pathParam(name, definition, paramType, map);
			}
		}
		
		@Override
		public void headerParam(final String name, final HeaderDefinition definition, final Type paramType, final AnnotationMap map) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.headerParam(name, definition, paramType, map);
			}
		}
		
		@Override
		public void newMethod(final RestResource restResource, final MethodDefinition def, final Method method) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.newMethod(restResource, def, method);
			}
		}
		
		@Override
		public void newSchema(final String schemaURI, final Schema s, final Class<?> schemaClass) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.newSchema(schemaURI, s, schemaClass);
			}
		}
		
		@Override
		public void renderDoc(final String path, final RestDoc doc) {
			for (final IRestDocGeneratorExtension e : this.exts) {
				e.renderDoc(path, doc);
			}
		}
		
	}
	
}
