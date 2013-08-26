package org.restdoc.server.impl;

import javax.ws.rs.Path;

@Path("/deep")
public class MyResourceBean {
	
	@Path("/res")
	public MyDeepRes deepResource() {
		return new MyDeepRes();
	}
	
}
