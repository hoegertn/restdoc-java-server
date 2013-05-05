package org.restdoc.server.ext.oauth2;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.restdoc.api.MethodDefinition;
import org.restdoc.api.RestDoc;
import org.restdoc.api.RestResource;
import org.restdoc.server.impl.RestDocGeneratorExtensionAdapter;

public class OAuth2Extension extends RestDocGeneratorExtensionAdapter {
	
	private String clientaccess;
	private String[] grants;
	private String tokenEndpoint;
	private String authEndpoint;
	
	
	public OAuth2Extension() {
		//
	}
	
	/**
	 * @param tokenEndpoint
	 * @param authEndpoint
	 * @param grants
	 */
	public OAuth2Extension(final String tokenEndpoint, final String authEndpoint, final String... grants) {
		this.tokenEndpoint = tokenEndpoint;
		this.authEndpoint = authEndpoint;
		this.grants = grants;
	}
	
	public String getClientaccess() {
		return this.clientaccess;
	}
	
	public void setClientaccess(final String clientaccess) {
		this.clientaccess = clientaccess;
	}
	
	public String[] getGrants() {
		return this.grants;
	}
	
	public void setGrants(final String[] grants) {
		this.grants = grants;
	}
	
	public String getTokenEndpoint() {
		return this.tokenEndpoint;
	}
	
	public void setTokenEndpoint(final String tokenEndpoint) {
		this.tokenEndpoint = tokenEndpoint;
	}
	
	public String getAuthEndpoint() {
		return this.authEndpoint;
	}
	
	public void setAuthEndpoint(final String authEndpoint) {
		this.authEndpoint = authEndpoint;
	}
	
	@Override
	public void newMethod(final RestResource restResource, final MethodDefinition def, final Method method) {
		if (method.isAnnotationPresent(Scopes.class)) {
			final Scopes scopes = method.getAnnotation(Scopes.class);
			def.setAdditionalField("scopes", scopes.value());
		}
	}
	
	@Override
	public void renderDoc(final String path, final RestDoc doc) {
		final HashMap<String, Object> endpoints = new HashMap<String, Object>();
		endpoints.put("token", this.tokenEndpoint);
		endpoints.put("authorize", this.authEndpoint);
		
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("endpoints", endpoints);
		map.put("grants", this.grants);
		if ((this.clientaccess != null) && !this.clientaccess.isEmpty()) {
			map.put("clientaccess", this.clientaccess);
		}
		doc.setAdditionalField("oauth2", map);
	}
	
}
