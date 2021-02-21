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

package org.springframework.scripting.groovy;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObject;
import org.junit.Test;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.dynamic.Refreshable;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scripting.Calculator;
import org.springframework.scripting.CallCounter;
import org.springframework.scripting.ConfigurableMessenger;
import org.springframework.scripting.ContextScriptBean;
import org.springframework.scripting.Messenger;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.tests.sample.beans.TestBean;
import org.springframework.util.ObjectUtils;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

/**
 * @author Rob Harrop
 * @author Rick Evans
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Chris Beams
 */
@SuppressWarnings("resource")
public class GroovyScriptFactoryTests {

	@Test
	public void testStaticScript() throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("groovyContext.xml", getClass());

		assertTrue(Arrays.asList(ctx.getBeanNamesForType(Calculator.class)).contains("calculator"));
		assertTrue(Arrays.asList(ctx.getBeanNamesForType(Messenger.class)).contains("messenger"));

		Calculator calc = (Calculator) ctx.getBean("calculator");
		Messenger messenger = (Messenger) ctx.getBean("messenger");

		assertFalse("Shouldn't get proxy when refresh is disabled", AopUtils.isAopProxy(calc));
		assertFalse("Shouldn't get proxy when refresh is disabled", AopUtils.isAopProxy(messenger));

		assertFalse("Scripted object should not be instance of Refreshable", calc instanceof Refreshable);
		assertFalse("Scripted object should not be instance of Refreshable", messenger instanceof Refreshable);

		assertEquals(calc, calc);
		assertEquals(messenger, messenger);
		assertTrue(!messenger.equals(calc));
		assertTrue(messenger.hashCode() != calc.hashCode());
		assertTrue(!messenger.toString().equals(calc.toString()));

		String desiredMessage = "Hello World!";
		assertEquals("Message is incorrect", desiredMessage, messenger.getMessage());

		assertTrue(ctx.getBeansOfType(Calculator.class).values().contains(calc));
		assertTrue(ctx.getBeansOfType(Messenger.class).values().contains(messenger));
	}

}
