package org.restdoc.server.impl;

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

/**
 * @author thoeger
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
		final Class<?>[] classes = new Class[] { MyRSBean.class, MyCrudBean.class };
		final GlobalHeader globalHeader = new GlobalHeader();
		globalHeader.request("X-Auth", "The Auth Key. See http://www.foo.bar/auth", false);
		this.generator.init(classes, globalHeader, "/v1");
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
			ServletException {
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
