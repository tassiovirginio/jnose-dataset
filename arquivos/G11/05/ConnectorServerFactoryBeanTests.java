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

package org.springframework.jmx.support;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Test;

import org.springframework.jmx.AbstractMBeanServerTests;
import org.springframework.tests.Assume;
import org.springframework.tests.TestGroup;

import static org.junit.Assert.*;

/**
 * To run the tests in the class, set the following Java system property:
 * {@code -DtestGroups=jmxmp}.
 *
 * @author Rob Harrop
 * @author Chris Beams
 * @author Sam Brannen
 */
public class ConnectorServerFactoryBeanTests extends AbstractMBeanServerTests {

	@Test
	public void startupWithSuppliedServer() throws Exception {
		//Added a brief snooze here - seems to fix occasional
		//java.net.BindException: Address already in use errors
		Thread.sleep(1);

		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.setServer(getServer());
		bean.afterPropertiesSet();

		try {
			checkServerConnection(getServer());
		}
		finally {
			bean.destroy();
		}
	}


	@Test
	public void noRegisterWithMBeanServer() throws Exception {
		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.afterPropertiesSet();

		try {
			// Try to get the connector bean.
			getServer().getObjectInstance(ObjectName.getInstance(OBJECT_NAME));
			fail("Instance should not be found");
		}
		catch (InstanceNotFoundException ex) {
			// expected
		}
		finally {
			bean.destroy();
		}
	}


}
