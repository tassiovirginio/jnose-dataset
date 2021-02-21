/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.junit.Test;

import org.springframework.aop.MethodMatcher;
import org.springframework.lang.Nullable;
import org.springframework.tests.sample.beans.IOther;
import org.springframework.tests.sample.beans.ITestBean;
import org.springframework.tests.sample.beans.TestBean;
import org.springframework.util.SerializationTestUtils;

import static org.junit.Assert.*;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class MethodMatchersTests {

	private final Method EXCEPTION_GETMESSAGE;

	private final Method ITESTBEAN_SETAGE;

	private final Method ITESTBEAN_GETAGE;

	private final Method IOTHER_ABSQUATULATE;


	public MethodMatchersTests() throws Exception {
		EXCEPTION_GETMESSAGE = Exception.class.getMethod("getMessage");
		ITESTBEAN_GETAGE = ITestBean.class.getMethod("getAge");
		ITESTBEAN_SETAGE = ITestBean.class.getMethod("setAge", int.class);
		IOTHER_ABSQUATULATE = IOther.class.getMethod("absquatulate");
	}


	@Test
	public void testDefaultMatchesAll() throws Exception {
		MethodMatcher defaultMm = MethodMatcher.TRUE;
		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertTrue(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
	}


}
