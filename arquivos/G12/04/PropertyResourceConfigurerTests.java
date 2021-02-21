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

package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.junit.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.tests.sample.beans.IndexedTestBean;
import org.springframework.tests.sample.beans.TestBean;

import static org.junit.Assert.*;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.*;
import static org.springframework.tests.TestResourceUtils.*;

/**
 * Unit tests for various {@link PropertyResourceConfigurer} implementations including:
 * {@link PropertyPlaceholderConfigurer}, {@link PropertyOverrideConfigurer} and
 * {@link PreferencesPlaceholderConfigurer}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @since 02.10.2003
 * @see PropertyPlaceholderConfigurerTests
 */
public class PropertyResourceConfigurerTests {

	@Test
	public void testPropertyOverrideConfigurerWithHeldProperties() {
		BeanDefinition def = BeanDefinitionBuilder.genericBeanDefinition(PropertiesHolder.class).getBeanDefinition();
		factory.registerBeanDefinition("tb", def);

		PropertyOverrideConfigurer poc;
		poc = new PropertyOverrideConfigurer();
		Properties props = new Properties();
		props.setProperty("tb.heldProperties[mail.smtp.auth]", "true");
		poc.setProperties(props);
		poc.postProcessBeanFactory(factory);

		PropertiesHolder tb = (PropertiesHolder) factory.getBean("tb");
		assertEquals("true", tb.getHeldProperties().getProperty("mail.smtp.auth"));
	}

	@Test
	public void testPropertyPlaceholderConfigurer() {
		doTestPropertyPlaceholderConfigurer(false);
	}

}
