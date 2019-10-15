/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
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
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.misc.env.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.StoreImpl;
import org.onap.aaf.misc.env.TimeTaken;

@RunWith(MockitoJUnitRunner.class)
public class JU_StoreImplTest {

    @Mock
    QName qname;

    @Mock
    Env env;

    TimeTaken tt,ttstringify;

    LogTarget logT;

    @Before
    public void setUp() {
        initMocks(this);
        tt=Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(env).start("JAXB Marshal", Env.XML);
        Mockito.doNothing().when(tt).done();
        logT = Mockito.mock(LogTarget.class);
        Mockito.doReturn(logT).when(env).debug();
    }

    @Test
    public void testPropsFromArgs() {
        StoreImpl bdfObj = new StoreImpl();
        bdfObj = new StoreImpl("");
        bdfObj.propsFromArgs(null, new String[] {"test"});
        bdfObj.propsFromArgs("test", new String[] {"test","te=st","test=1"});

    }

    @Test
    public void testMorePropsConstructor() {
        Properties props = Mockito.mock(Properties.class);
        new StoreImpl(null,props);
        StoreImpl bdfObj = new StoreImpl("test",props);
    }

    @Test
    public void testMorePropsFileNOtExists() {
        Properties props = Mockito.mock(Properties.class);
        Mockito.doReturn("test").when(props).getProperty("test");
        StoreImpl bdfObj = new StoreImpl("test",props);
    }

    @Test
    public void testMorePropsExists() {
        Properties props = Mockito.mock(Properties.class);
        Mockito.doReturn(System.getProperty("user.dir")+"/src/test/java/org/onap/aaf/misc/env/JU_StoreImplTest.java").when(props).getProperty("test");
        StoreImpl bdfObj = new StoreImpl("test",props);
    }

    @Test
    public void testNewTransState() {
        StoreImpl bdfObj = new StoreImpl(null, new String[] {});
        bdfObj.newTransState();
    }

    @Test
    public void testSlot() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {});
        Slot slot = bdfObj.slot(null);
        assertEquals(slot.toString(),"=0");
        slot = bdfObj.slot("test");
        assertEquals(slot.toString(),"test=1");
    }

    @Test
    public void testExistingSlot() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {"test","test=1"});
        Slot retVal = bdfObj.existingSlot("test");
        assertNull(retVal);
    }

    @Test
    public void testExistingSlotNames() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {"test","test=1"});
        List<String> retVal = bdfObj.existingSlotNames();
        assertTrue(retVal.size()==0);
    }

    @Test
    public void testGet() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {"test","test=1"});
        Object retVal = bdfObj.get(new StaticSlot(1,"test"),qname);
        assertTrue(retVal instanceof QName);
    }

    @Test
    public void testGetSlot() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {"test","test=1"});
        Object retVal = bdfObj.get(new StaticSlot(1,"test"));
        assertNull(retVal);
    }

    @Test
    public void testExistingStaticSlotNames() {
        StoreImpl bdfObj = new StoreImpl("test", new String[] {"test","test=1"});
        List<String> retVal = bdfObj.existingStaticSlotNames();
        assertTrue(retVal.size()==1);
    }
}
