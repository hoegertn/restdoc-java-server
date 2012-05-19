package org.restdoc.server.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.schema.JsonSchema;

/**
 * @author thoeger
 * 
 */
public class SchemaTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonSchema schema = mapper.generateJsonSchema(Msg.class);

			System.out.println(mapper.writeValueAsString(schema));
		} catch (final JsonMappingException e) {
			e.printStackTrace();
		} catch (final JsonGenerationException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
