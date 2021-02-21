/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.reactive.result.condition;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import org.springframework.mock.http.server.reactive.test.MockServerHttpRequest;
import org.springframework.mock.web.test.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.mock.http.server.reactive.test.MockServerHttpRequest.get;

/**
 * Unit tests for {@link ProducesRequestCondition}.
 *
 * @author Rossen Stoyanchev
 */
public class ProducesRequestConditionTests {



	@Test
	public void compareToMultipleExpressionsAndMultipeAcceptHeaderValues() throws Exception {
		ProducesRequestCondition condition1 = new ProducesRequestCondition("text/*", "text/plain");
		ProducesRequestCondition condition2 = new ProducesRequestCondition("application/*", "application/xml");

		ServerWebExchange exchange = MockServerWebExchange.from(
				get("/").header("Accept", "text/plain", "application/xml"));

		int result = condition1.compareTo(condition2, exchange);
		assertTrue("Invalid comparison result: " + result, result < 0);

		result = condition2.compareTo(condition1, exchange);
		assertTrue("Invalid comparison result: " + result, result > 0);

		exchange = MockServerWebExchange.from(
				get("/").header("Accept", "application/xml", "text/plain"));

		result = condition1.compareTo(condition2, exchange);
		assertTrue("Invalid comparison result: " + result, result > 0);

		result = condition2.compareTo(condition1, exchange);
		assertTrue("Invalid comparison result: " + result, result < 0);
	}


}
