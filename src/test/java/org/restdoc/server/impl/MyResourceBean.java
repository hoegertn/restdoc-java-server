package org.restdoc.server.impl;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/deep")
public class MyResourceBean {
	
	@Path("/res")
	public MyDeepRes deepResource() {
		return new MyDeepRes();
	}
	
	@GET
	public void get(@BeanParam BeanObj o) {
		System.out.println(o);
	}
}
