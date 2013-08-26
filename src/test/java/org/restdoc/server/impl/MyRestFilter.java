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
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.restdoc.api.GlobalHeader;
import org.restdoc.server.ext.oauth2.OAuth2Extension;

/**
 * 
 */
public class MyRestFilter implements Filter {
	
	private RestDocGenerator generator;
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			final MyRestFilter filter = new MyRestFilter();
			filter.init(null);
			
			final String doc = filter.generator.getRestDocStringForPath("/");
			System.out.println(doc);
			
			final String doc2 = filter.generator.getRestDocStringForPath("/v1/api/{id}");
			System.out.println(doc2);
			
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.generator = new RestDocGenerator();
		
		final OAuth2Extension oauth = new OAuth2Extension("tokenURL", "authURL", "code", "password");
		oauth.setClientaccess("tweet me!");
		
		// Enable to test RestDoc extensibility
		this.generator.registerGeneratorExtension(new MyExt());
		this.generator.registerGeneratorExtension(oauth);
		
		final Class<?>[] classes = new Class[] {MyRSBean.class, MyCrudBean.class, MyResourceBean.class};
		final GlobalHeader globalHeader = new GlobalHeader();
		globalHeader.request("X-Auth", "The Auth Key. See http://www.foo.bar/auth", false);
		this.generator.init(classes, globalHeader, "/v1");
	}
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getMethod().equals("OPTIONS")) {
				final String requestURI = httpRequest.getRequestURI();
				final String docString = this.generator.getRestDocStringForPath(URLDecoder.decode(requestURI, "UTF-8"));
				response.getWriter().write(docString);
			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}
	
	@Override
	public void destroy() {
		// Auto-generated method stub
	}
}
