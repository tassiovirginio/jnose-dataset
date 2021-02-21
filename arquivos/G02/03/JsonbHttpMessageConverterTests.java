/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.converter.json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.Assert.*;

/**
 * Integration tests for the JSON Binding API, running against Apache Johnzon.
 *
 * @author Juergen Hoeller
 * @since 5.0
 */
public class JsonbHttpMessageConverterTests {

	private final JsonbHttpMessageConverter converter = new JsonbHttpMessageConverter();


	@Test
	public void canRead() {
		assertTrue(this.converter.canRead(MyBean.class, new MediaType("application", "json")));
		assertTrue(this.converter.canRead(Map.class, new MediaType("application", "json")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void readUntyped() throws IOException {
		String body = "{\"bytes\":[1,2],\"array\":[\"Foo\",\"Bar\"]," +
				"\"number\":42,\"string\":\"Foo\",\"bool\":true,\"fraction\":42.0}";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "json"));
		HashMap<String, Object> result = (HashMap<String, Object>) this.converter.read(HashMap.class, inputMessage);
		assertEquals("Foo", result.get("string"));
		Number n = (Number) result.get("number");
		assertEquals(42, n.longValue());
		n = (Number) result.get("fraction");
		assertEquals(42D, n.doubleValue(), 0D);
		List<String> array = new ArrayList<>();
		array.add("Foo");
		array.add("Bar");
		assertEquals(array, result.get("array"));
		assertEquals(Boolean.TRUE, result.get("bool"));
		byte[] bytes = new byte[2];
		List<Number> resultBytes = (ArrayList<Number>)result.get("bytes");
		for (int i = 0; i < 2; i++) {
			bytes[i] = resultBytes.get(i).byteValue();
		}
		assertArrayEquals(new byte[] {0x1, 0x2}, bytes);
	}

}
