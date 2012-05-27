package org.restdoc.server.impl;

import java.util.HashMap;

import org.restdoc.api.HeaderDefinition;

import com.google.common.collect.Maps;

/**
 * @author thoeger
 * 
 */
public class MyRestFilter extends AbstractRestDocFilter {

	@Override
	protected Class<?>[] getRESTClasses() {
		return new Class[] { MyRSBean.class, MyCrudBean.class };
	}

	@Override
	protected HashMap<String, HeaderDefinition> getGlobalRequestHeaders() {
		final HashMap<String, HeaderDefinition> header = Maps.newHashMap();
		final HeaderDefinition hd = new HeaderDefinition();
		hd.setDescription("The Auth Key. See http://www.foo.bar/auth");
		hd.setRequired(false);
		header.put("X-Auth", hd);
		return header;
	}

	@Override
	protected HashMap<String, HeaderDefinition> getGlobalResponseHeaders() {
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final MyRestFilter filter = new MyRestFilter();
		filter.init();

		final String doc = filter.getRestDocStringForPath("");
		System.out.println(doc);

	}
}
