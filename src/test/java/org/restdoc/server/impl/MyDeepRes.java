package org.restdoc.server.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.restdoc.annotations.RestDoc;
import org.restdoc.annotations.RestDocIgnore;
import org.restdoc.annotations.RestDocReturnCode;
import org.restdoc.annotations.RestDocReturnCodes;

@Produces("text/plain")
@Consumes("text/plain")
@RestDoc(resourceDescription = "foo Res")
public class MyDeepRes {
	
	/**
	 * @param id
	 * @return the message
	 */
	@GET
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	public String getMessageList() {
		return "Hello deep";
	}
	
	/**
	 * @param id
	 * @return the message
	 */
	@PUT
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	public Response putStuff() {
		return Response.ok().build();
	}
	
	/**
	 * @param id
	 * @return the message
	 */
	@POST
	@RestDocIgnore
	@RestDocReturnCodes({@RestDocReturnCode(code = "200", description = "All went well"), @RestDocReturnCode(code = "403", description = "Access not allowed")})
	public String postStuff() {
		return "Hello deep";
	}
}
