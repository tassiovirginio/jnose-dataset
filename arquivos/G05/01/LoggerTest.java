/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.agent.jul;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.immutables.value.Value;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class LoggerTest {

    
    @Test
    public void testGetLogger() {
        Logger logger = Logger.getLogger("abc");
        assertThat(logger.getSlf4jLogger().getName()).isEqualTo("abc");
    }


    @Test
    public void testResourceBundle() {
        // given
        org.slf4j.Logger slf4jLogger = mock(org.slf4j.Logger.class);

        // when
        Logger logger = new Logger(slf4jLogger);

        // then
        assertThat(logger.getResourceBundle()).isNull();
        assertThat(logger.getResourceBundleName()).isNull();
        verifyNoMoreInteractions(slf4jLogger);
    }

}
