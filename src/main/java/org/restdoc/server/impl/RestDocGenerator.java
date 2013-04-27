package org.restdoc.server.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.schema.JsonSchema;
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
import org.restdoc.server.impl.annotations.RestDocAccept;
import org.restdoc.server.impl.annotations.RestDocHeader;
import org.restdoc.server.impl.annotations.RestDocParam;
import org.restdoc.server.impl.annotations.RestDocResponse;
import org.restdoc.server.impl.annotations.RestDocReturnCode;
import org.restdoc.server.impl.annotations.RestDocReturnCodes;
import org.restdoc.server.impl.annotations.RestDocSchema;
import org.restdoc.server.impl.annotations.RestDocType;
import org.restdoc.server.impl.annotations.RestDocValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Use this class to generate RestDoc
 * 
 * @author thoeger
 * 
 */
public class RestDocGenerator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final HashMap<String, RestResource> resources = Maps.newHashMap();
	
	private Map<String, HeaderDefinition> requestHeaderMap;
	
	private Map<String, HeaderDefinition> responseHeaderMap;
	
	private HashMap<String, Schema> schemaMap;
	
	private ObjectMapper mapper;
	
	
	/**
	 * initialize the RestDoc Generator
	 * 
	 * @param classes the array of JAX-RS classes
	 * @param globalHeader the global headers
	 * @param baseURI an optional base uri like "/api"
	 */
	public void init(Class<?>[] classes, GlobalHeader globalHeader, String baseURI) {
		this.logger.info("Starting generation of RestDoc");
		this.logger.info("Searching for RestDoc API classes");
		
		this.mapper = RestDocParser.createMapper();
		
		this.requestHeaderMap = globalHeader.getRequestHeader();
		this.responseHeaderMap = globalHeader.getResponseHeader();
		
		// Avoid NPE if user returned null
		if (this.requestHeaderMap == null) {
			this.requestHeaderMap = Maps.newHashMap();
		}
		if (this.responseHeaderMap == null) {
			this.responseHeaderMap = Maps.newHashMap();
		}
		if (this.schemaMap == null) {
			this.schemaMap = Maps.newHashMap();
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
	
	private void addResourcesOfClass(Class<?> apiClass, String baseURI) {
		this.logger.info("Scanning class: {}", apiClass.getCanonicalName());
		
		String basepath = baseURI;
		if (apiClass.isAnnotationPresent(Path.class)) {
			final Path path = apiClass.getAnnotation(Path.class);
			basepath += path.value();
		}
		
		// find methods
		final Method[] methods = apiClass.getMethods();
		for (final Method method : methods) {
			if (method.isAnnotationPresent(org.restdoc.server.impl.annotations.RestDoc.class)) {
				this.logger.info("Generating RestDoc of method: " + method.toString());
				this.addResourceMethod(basepath, method);
			}
		}
	}
	
	private void addResourceMethod(String basepath, Method method) {
		// get needed annotations from method
		final org.restdoc.server.impl.annotations.RestDoc docAnnotation = method.getAnnotation(org.restdoc.server.impl.annotations.RestDoc.class);
		final Path pathAnnotation = method.getAnnotation(Path.class);
		
		// get parameter
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		
		// values from parameters
		final List<String> queryParams = Lists.newArrayList();
		final HashMap<String, HeaderDefinition> methodRequestHeader = Maps.newHashMap();
		final HashMap<String, ParamDefinition> methodParams = Maps.newHashMap();
		for (int i = 0; i < parameterTypes.length; i++) {
			// final Class<?> paramType = parameterTypes[i];
			final Annotation[] paramAnnotations = parameterAnnotations[i];
			final ParamDefinition definition = new ParamDefinition();
			final HeaderDefinition headerDefinition = new HeaderDefinition();
			
			for (final Annotation annotation : paramAnnotations) {
				if (annotation instanceof QueryParam) {
					final QueryParam queryParam = (QueryParam) annotation;
					final String name = queryParam.value();
					queryParams.add(name);
					methodParams.put(name, definition);
				} else if (annotation instanceof PathParam) {
					final PathParam pathParam = (PathParam) annotation;
					final String name = pathParam.value();
					methodParams.put(name, definition);
				} else if (annotation instanceof HeaderParam) {
					final HeaderParam headerParam = (HeaderParam) annotation;
					final String name = headerParam.value();
					if (!this.requestHeaderMap.containsKey(name)) {
						methodRequestHeader.put(name, headerDefinition);
					}
				} else if (annotation instanceof RestDocParam) {
					this.parseRestDocParameter(definition, (RestDocParam) annotation);
				} else if (annotation instanceof RestDocHeader) {
					final RestDocHeader docHeader = (RestDocHeader) annotation;
					headerDefinition.setDescription(docHeader.description());
					headerDefinition.setRequired(docHeader.required());
				}
			}
		}
		
		String path = basepath;
		if (pathAnnotation != null) {
			path += pathAnnotation.value();
		}
		for (final String string : queryParams) {
			path += "{?" + string + "}";
		}
		
		final String id = docAnnotation.id();
		final String resourceDescription = docAnnotation.resourceDescription();
		
		final String methodDescription = docAnnotation.methodDescription();
		final String methodType = this.getHTTPVerb(method);
		
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
		}
		
		if (restResource.getMethods().containsKey(methodType)) {
			throw new RuntimeException("Duplicate method detected for resource: " + path + " -> " + methodType);
		}
		
		final MethodDefinition def = new MethodDefinition();
		def.setDescription(methodDescription);
		def.getHeaders().putAll(methodRequestHeader);
		def.getAccepts().addAll(this.getAccepts(method));
		def.getStatusCodes().putAll(this.getStatusCodes(method));
		def.setResponse(this.getMethodResponse(method));
		
		restResource.getMethods().put(methodType, def);
	}
	
	private ResponseDefinition getMethodResponse(Method method) {
		final ResponseDefinition def = new ResponseDefinition();
		if (method.isAnnotationPresent(RestDocResponse.class)) {
			final RestDocResponse docResponse = method.getAnnotation(RestDocResponse.class);
			final RestDocType[] types = docResponse.types();
			for (final RestDocType restDocType : types) {
				if (!restDocType.schemaClass().equals(Object.class)) {
					final String schema = this.getSchemaFromClass(restDocType.schemaClass());
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
		return def;
	}
	
	private String getSchemaFromClass(Class<?> schemaClass) {
		if (schemaClass.isAnnotationPresent(RestDocSchema.class)) {
			final RestDocSchema docSchema = schemaClass.getAnnotation(RestDocSchema.class);
			final String schemaURI = docSchema.value();
			if (!this.schemaMap.containsKey(schemaURI)) {
				try {
					final JsonSchema schema = this.mapper.generateJsonSchema(schemaClass);
					final Schema s = new Schema();
					s.setSchema(schema);
					this.schemaMap.put(schemaURI, s);
				} catch (final JsonMappingException e) {
					this.logger.error("Error creating schema for URI: " + schemaURI, e);
				}
			}
			return schemaURI;
		}
		this.logger.error("SchemaClass {} is not annotated with RestDocSchema.", schemaClass.getCanonicalName());
		return "";
	}
	
	private Map<String, String> getStatusCodes(Method method) {
		final Map<String, String> codeMap = Maps.newHashMap();
		if (method.isAnnotationPresent(RestDocReturnCodes.class)) {
			final RestDocReturnCode[] returnCodes = method.getAnnotation(RestDocReturnCodes.class).value();
			for (final RestDocReturnCode rdrc : returnCodes) {
				codeMap.put(rdrc.code(), rdrc.description());
			}
		}
		return codeMap;
	}
	
	private Collection<Representation> getAccepts(Method method) {
		final Collection<Representation> list = Lists.newArrayList();
		if (method.isAnnotationPresent(RestDocAccept.class)) {
			final RestDocAccept docAccept = method.getAnnotation(RestDocAccept.class);
			final RestDocType[] types = docAccept.value();
			for (final RestDocType restDocType : types) {
				if (!restDocType.schemaClass().equals(Object.class)) {
					final String schema = this.getSchemaFromClass(restDocType.schemaClass());
					list.add(new Representation(restDocType.type(), schema));
				} else {
					list.add(new Representation(restDocType.type(), restDocType.schema()));
				}
			}
		}
		return list;
	}
	
	private void parseRestDocParameter(final ParamDefinition definition, final RestDocParam docParam) {
		definition.setDescription(docParam.description());
		final RestDocValidation[] restDocValidations = docParam.validations();
		for (final RestDocValidation validation : restDocValidations) {
			final ParamValidation v = new ParamValidation();
			v.setType(validation.type());
			v.setPattern(validation.pattern());
			definition.getValidations().add(v);
		}
	}
	
	private String getHTTPVerb(Method method) {
		final Annotation[] annotations = method.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
				final HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
				return httpMethod.value();
			}
		}
		throw new RuntimeException("No suitable method found for method: " + method.toString());
	}
	
	/**
	 * @param path the basepath to start
	 * @return the {@link RestDoc} as string
	 */
	public String getRestDocStringForPath(String path) {
		final RestDoc doc = this.getDoc(path);
		return RestDocParser.writeRestDoc(doc);
	}
	
	private RestDoc getDoc(String path) {
		final RestDoc doc = new RestDoc();
		// populate schemas
		doc.setSchemas(this.schemaMap);
		
		// populate header section
		doc.getHeaders().getRequestHeader().putAll(this.requestHeaderMap);
		doc.getHeaders().getResponseHeader().putAll(this.responseHeaderMap);
		
		// populate resource section
		final Set<Entry<String, RestResource>> entrySet = this.resources.entrySet();
		for (final Entry<String, RestResource> entry : entrySet) {
			if (entry.getKey().startsWith(path)) {
				doc.getResources().add(entry.getValue());
			}
		}
		
		return doc;
	}
	
}
