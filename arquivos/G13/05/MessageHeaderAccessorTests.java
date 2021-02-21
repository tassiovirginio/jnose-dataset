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

package org.springframework.messaging.support;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.SerializationTestUtils;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test fixture for {@link MessageHeaderAccessor}.
 *
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @author Juergen Hoeller
 */
public class MessageHeaderAccessorTests {


	@Test
	public void newEmptyHeaders() {
		MessageHeaderAccessor accessor = new MessageHeaderAccessor();
		assertEquals(0, accessor.toMap().size());
	}

	@Test
	public void existingHeadersModification() throws InterruptedException {
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("bar", "baz");
		GenericMessage<String> message = new GenericMessage<>("payload", map);

		Thread.sleep(50);

		MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
		accessor.setHeader("foo", "BAR");
		MessageHeaders actual = accessor.getMessageHeaders();

		assertEquals(3, actual.size());
		assertNotEquals(message.getHeaders().getId(), actual.getId());
		assertEquals("BAR", actual.get("foo"));
		assertEquals("baz", actual.get("bar"));
	}

	

}
