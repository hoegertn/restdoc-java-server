package org.restdoc.server.impl;

import org.restdoc.server.impl.annotations.RestDocSchema;

/**
 * @author thoeger
 * 
 */
@RestDocSchema("http://some.json/msg")
public class Msg {

	private String id;

	private String content;

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(final String content) {
		this.content = content;
	}

}
