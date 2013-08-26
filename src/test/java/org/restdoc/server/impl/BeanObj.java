package org.restdoc.server.impl;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

import org.restdoc.annotations.RestDocHeader;
import org.restdoc.annotations.RestDocParam;

public class BeanObj {
	
	@QueryParam("filter")
	@RestDocParam(description = "my special filter")
	private String filter;
	
	@HeaderParam("X-MyHeader")
	@RestDocHeader(description = "my special header")
	private Long myHeader;
	
}
