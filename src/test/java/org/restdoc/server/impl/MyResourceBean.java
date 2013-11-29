package org.restdoc.server.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.restdoc.annotations.RestDoc;
import org.restdoc.annotations.RestDocParam;

@Path("/deep")
public class MyResourceBean {
	
	@Path("/res")
	public MyDeepRes deepResource() {
		return new MyDeepRes();
	}
	
	@GET
	@RestDoc(resourceDescription = "blubb")
	public void get(@QueryParam("color") @RestDocParam(description = "my special color") EColor color) {
		System.out.println(color);
	}
}
