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
		Assert.assertEquals("http://some.json/msg", this.getSchemaForClass(Msg.class));
		Assert.assertEquals("string", this.getSchemaForClass(String.class));
		Assert.assertEquals("long", this.getSchemaForClass(Long.class));
		Assert.assertEquals("integer", this.getSchemaForClass(Integer.class));
		Assert.assertEquals("double", this.getSchemaForClass(Double.class));
		Assert.assertEquals("integer", this.getSchemaForClass(int.class));
		Assert.assertEquals("boolean", this.getSchemaForClass(Boolean.class));
		Assert.assertEquals("double", this.getSchemaForClass(BigDecimal.class));
	}
	
	@Test
	public void testReturn() {
		Assert.assertEquals("http://some.json/msg", this.getSchemaForMethod("rMsg"));
		Assert.assertEquals("string", this.getSchemaForMethod("rString"));
		Assert.assertEquals("long", this.getSchemaForMethod("rLong"));
		Assert.assertEquals("boolean", this.getSchemaForMethod("rLBoolean"));
		Assert.assertEquals(null, this.getSchemaForMethod("rVoid"));
	}
	
	@Test
	public void testCollection() {
		Assert.assertEquals("http://some.json/msg[]", this.getSchemaForMethod("rListMsg"));
		Assert.assertEquals("http://some.json/msg[]", this.getSchemaForMethod("rAMsg"));
	}
	
	private String getSchemaForClass(Type type) {
		return SchemaResolver.getSchemaFromTypeOrNull(type, this.map, null);
	}
	
	private String getSchemaForMethod(String methodName) {
		try {
			Type t = ReflectClass.class.getMethod(methodName).getGenericReturnType();
			return this.getSchemaForClass(t);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
}
