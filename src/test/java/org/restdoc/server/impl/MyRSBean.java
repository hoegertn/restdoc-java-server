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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.restdoc.annotations.RestDoc;
import org.restdoc.annotations.RestDocHeader;
import org.restdoc.annotations.RestDocParam;
import org.restdoc.annotations.RestDocResponse;
import org.restdoc.annotations.RestDocReturnCode;
import org.restdoc.annotations.RestDocReturnCodes;
import org.restdoc.server.ext.oauth2.Scopes;

import com.google.common.collect.Maps;

/**
 * 
 */
@Path("/api")
public class MyRSBean {
	
	private final HashMap<String, String> messages = Maps.newHashMap();
	
	
	/**
	 * @param id
	 * @return the message
	 */
	@GET
	@RestDoc(id = "messageList", resourceDescription = "A list of messages", methodDescription = "List all messages")
	@RestDocResponse(headers = {@RestDocHeader(name = "X-Call", description = "Remaining calls", required = false)})
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	@Produces("text/plain")
	@Consumes("text/plain")
	public String getMessageList() {
		final StringBuilder sb = new StringBuilder();
		final Set<String> keySet = this.messages.keySet();
		for (final String key : keySet) {
			sb.append(key);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * @param id
	 * @return the message
	 */
	@GET
	@Path(".json")
	@RestDoc(resourceDescription = "A list of messages", methodDescription = "List all messages")
	@RestDocResponse(headers = {@RestDocHeader(name = "X-Call", description = "Remaining calls", required = false)})
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	@Produces("application/json")
	public List<Msg> getMessageListJson() {
		List<Msg> l = new ArrayList<Msg>();
		final Set<String> keySet = this.messages.keySet();
		for (final String key : keySet) {
			Msg msg = new Msg();
			msg.setId(key);
			msg.setContent(this.messages.get(key));
			l.add(msg);
		}
		return l;
	}
	
	/**
	 * @param id
	 * @return the message
	 */
	@Path("/{id}")
	@GET
	@RestDoc(id = "message", resourceDescription = "A single message", methodDescription = "Read the message content")
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed"), @RestDocReturnCode(code = "404", description = "Message not found")})
	@Produces("text/plain")
	public String getMessage(@PathParam("id") @RestDocParam(description = "The message id") final String id) {
		if (this.messages.containsKey(id)) {
			return this.messages.get(id);
		}
		throw new WebApplicationException(Status.NOT_FOUND);
	}
	
	/**
	 * @param id
	 * @return the message
	 */
	@Path("/{id}")
	@GET
	@RestDoc(id = "message", resourceDescription = "A single message", methodDescription = "Read the message content")
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed"), @RestDocReturnCode(code = "404", description = "Message not found")})
	@Produces("application/json")
	public String getMessageJSON(@PathParam("id") @RestDocParam(description = "The message id") final String id) {
		if (this.messages.containsKey(id)) {
			return this.messages.get(id);
		}
		throw new WebApplicationException(Status.NOT_FOUND);
	}
	
	/**
	 * @param id
	 * @param content
	 * @return the created message
	 */
	@Path("/{id}")
	@PUT
	@RestDoc(methodDescription = "Update the message content")
	@RestDocReturnCodes({@RestDocReturnCode(code = "403", description = "")})
	@Consumes("text/plain")
	@Scopes("write")
	public void setMessage(@PathParam("id") final String id, final String content) {
		this.messages.put(id, content);
	}
	
	/**
	 * @param msg the message
	 * @param lifetime the message ttl
	 * @return the created message
	 */
	@POST
	@RestDoc(methodDescription = "Update the message content")
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	@Produces("text/plain")
	@Consumes("application/json")
	@SuppressWarnings("unused")
	public String setMessage(final Msg msg, @QueryParam("X-TTL") @RestDocParam(description = "The message lifetime") final Long lifetime) {
		this.setMessage(msg.getId(), msg.getContent());
		return msg.getContent();
	}
	
}
