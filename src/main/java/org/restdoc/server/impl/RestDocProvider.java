package org.restdoc.server.impl;

/*
 * #%L Daemon with Spring and CXF %% Copyright (C) 2013 Taimos GmbH %% Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License. #L%
 */

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restdoc.api.RestDoc;

import com.google.common.base.Charsets;

/**
 * Copyright 2013 Taimos<br>
 * <br>
 * https://issues.apache.org/jira/browse/CXF-4996
 * 
 * @author hoegertn
 * 
 */
@Provider
@Produces(RestDoc.RESTDOC_MEDIATYPE)
public class RestDocProvider implements MessageBodyWriter<String> {
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (mediaType.toString().equals(RestDoc.RESTDOC_MEDIATYPE)) {
			return true;
		}
		return false;
	}
	
	@Override
	public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return t.getBytes(Charsets.UTF_8).length;
	}
	
	@Override
	public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		entityStream.write(t.getBytes(Charsets.UTF_8));
	}
	
}
