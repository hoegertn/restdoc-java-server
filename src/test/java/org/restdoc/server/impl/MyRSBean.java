package org.restdoc.server.impl;

import java.util.HashMap;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.restdoc.server.impl.annotations.RestDoc;
import org.restdoc.server.impl.annotations.RestDocAccept;
import org.restdoc.server.impl.annotations.RestDocHeader;
import org.restdoc.server.impl.annotations.RestDocParam;
import org.restdoc.server.impl.annotations.RestDocResponse;
import org.restdoc.server.impl.annotations.RestDocReturnCode;
import org.restdoc.server.impl.annotations.RestDocReturnCodes;
import org.restdoc.server.impl.annotations.RestDocType;

import com.google.common.collect.Maps;

/**
 * @author thoeger
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
	@RestDocResponse(types = { @RestDocType(type = "text/plain") }, headers = { @RestDocHeader(name = "X-Call", description = "Remaining calls", required = false) })
	@RestDocReturnCodes({ @RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed") })
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
	@Path("/{id}")
	@GET
	@RestDoc(id = "message", resourceDescription = "A single message", methodDescription = "Read the message content")
	@RestDocResponse(types = { @RestDocType(type = "text/plain") })
	@RestDocReturnCodes({ @RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed"), @RestDocReturnCode(code = "404", description = "Message not found") })
	@Produces("text/plain")
	@Consumes("text/plain")
	public String getMessage(@PathParam("id") @RestDocParam(description = "The message id") String id) {
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
	@POST
	@RestDoc(methodDescription = "Update the message content")
	@RestDocAccept({ @RestDocType(type = "text/plain") })
	@RestDocResponse(types = { @RestDocType(type = "text/plain") })
	@RestDocReturnCodes({ @RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed") })
	@Produces("text/plain")
	@Consumes("text/plain")
	public String setMessage(@PathParam("id") String id, String content) {
		this.messages.put(id, content);
		return content;
	}

	/**
	 * @param msg
	 *            the message
	 * @return the created message
	 */
	@POST
	@RestDoc(methodDescription = "Update the message content")
	@RestDocAccept({ @RestDocType(type = "application/json", schemaClass = Msg.class) })
	@RestDocResponse(types = { @RestDocType(type = "text/plain") })
	@RestDocReturnCodes({ @RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed") })
	@Produces("text/plain")
	@Consumes("application/json")
	public String setMessage(Msg msg) {
		return this.setMessage(msg.getId(), msg.getContent());
	}

}
