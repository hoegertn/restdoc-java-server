package org.restdoc.server.impl.reflect;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.restdoc.api.Schema;
import org.restdoc.server.impl.Msg;
import org.restdoc.server.impl.util.SchemaResolver;

/**
 * Copyright 2013 Cinovo AG<br>
 * <br>
 * 
 * @author Thorsten Hoeger
 * 
 */
@SuppressWarnings("javadoc")
public class SchemaResolverTest {
	
	private HashMap<String, Schema> map = new HashMap<String, Schema>();
	
	
	@Test
	public void testClass() {
		Assert.assertEquals("http://some.json/msg", SchemaResolver.getSchemaFromTypeOrNull(Msg.class, this.map, null));
		Assert.assertEquals("string", SchemaResolver.getSchemaFromTypeOrNull(String.class, this.map, null));
		Assert.assertEquals("long", SchemaResolver.getSchemaFromTypeOrNull(Long.class, this.map, null));
		Assert.assertEquals("integer", SchemaResolver.getSchemaFromTypeOrNull(Integer.class, this.map, null));
		Assert.assertEquals("double", SchemaResolver.getSchemaFromTypeOrNull(Double.class, this.map, null));
		Assert.assertEquals("integer", SchemaResolver.getSchemaFromTypeOrNull(int.class, this.map, null));
		Assert.assertEquals("boolean", SchemaResolver.getSchemaFromTypeOrNull(Boolean.class, this.map, null));
		Assert.assertEquals("double", SchemaResolver.getSchemaFromTypeOrNull(BigDecimal.class, this.map, null));
	}
	
	@Test
	public void testReturn() {
		Assert.assertEquals("http://some.json/msg", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rMsg"), this.map, null));
		Assert.assertEquals("string", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rString"), this.map, null));
		Assert.assertEquals("long", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rLong"), this.map, null));
		Assert.assertEquals("boolean", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rLBoolean"), this.map, null));
	}
	
	@Test
	public void testCollection() {
		Assert.assertEquals("http://some.json/msg[]", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rListMsg"), this.map, null));
		Assert.assertEquals("http://some.json/msg[]", SchemaResolver.getSchemaFromTypeOrNull(SchemaResolverTest.ret("rAMsg"), this.map, null));
	}
	
	private static Type ret(String methodName) {
		try {
			Type t = ReflectClass.class.getMethod(methodName).getGenericReturnType();
			System.out.println(t + " - " + t.getClass());
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
