/*
 * Copyright 1999-2011 Alibaba Group.
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
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.model.Person;
import com.alibaba.dubbo.common.model.SerializablePerson;
import com.alibaba.dubbo.common.model.person.BigPerson;
import com.alibaba.dubbo.common.model.person.FullAddress;
import com.alibaba.dubbo.common.model.person.PersonInfo;
import com.alibaba.dubbo.common.model.person.PersonStatus;
import com.alibaba.dubbo.common.model.person.Phone;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author ding.lid
 */
public class PojoUtilsTest {

    @Test
    public void test_Loop_pojo() throws Exception {
        Parent p = new Parent();
        p.setAge(10);
        p.setName("jerry");

        Child c = new Child();
        c.setToy("haha");

        p.setChild(c);
        c.setParent(p);

        Object generalize = PojoUtils.generalize(p);
        Parent parent = (Parent) PojoUtils.realize(generalize, Parent.class);

        assertEquals(10, parent.getAge());
        assertEquals("jerry", parent.getName());

        assertEquals("haha", parent.getChild().getToy());
        assertSame(parent, parent.getChild().getParent());
    }

    @Test
    public void test_Loop_Map() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("k", "v");
        map.put("m", map);
        assertSame(map, map.get("m"));
        System.out.println(map);
        Object generalize = PojoUtils.generalize(map);
        System.out.println(generalize);
        @SuppressWarnings("unchecked")
        Map<String, Object> ret = (Map<String, Object>) PojoUtils.realize(generalize, Map.class);
        System.out.println(ret);

        assertEquals("v", ret.get("k"));
        assertSame(ret, ret.get("m"));
    }

}