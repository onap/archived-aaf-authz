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

package org.onap.aaf.misc.env;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.misc.env.impl.BasicTrans;

@RunWith(MockitoJUnitRunner.class)
public class JU_BasicTransTest {

    BasicTrans trans = null;

    @Mock
    private EnvJAXB env;

    @Mock
    private TimeTaken timeTaken;

    @Before
    public void setUp() throws Exception {
        trans = new BasicTrans(env);
    }

    @Test
    public void testSlot() {
        Slot slot = new Slot(1, "XML");
        when(env.slot("XML")).thenReturn(slot);

        Slot outputSlot = trans.slot("XML");
        Object[] state = new Object[2];

        slot.put(state, "JSON");

        assertEquals(slot.get(state), "JSON");
        assertEquals(slot.getKey(), outputSlot.getKey());
        assertEquals(slot.toString(), outputSlot.toString());
    }

    @Test
    public void testGetStaticSlot() {
        StaticSlot staticSlot = new StaticSlot(1, "XML");
        when(env.get(staticSlot)).thenReturn(staticSlot.toString());

        assertEquals(staticSlot.toString(), trans.get(staticSlot));
    }

    @Test
    public void testGetStaticSlotWithT() {
        StaticSlot staticSlot = new StaticSlot(1, "XML");
        when(env.get(staticSlot, "XML")).thenReturn(staticSlot.getKey());

        assertEquals(staticSlot.getKey(), trans.get(staticSlot, "XML"));
    }

    @Test
    public void testSetProperty() {
        String tag = "tag";
        String value = "value";
        String defltValue = "diffValue";
        when(env.setProperty(tag, value)).thenReturn(value);
        when(env.getProperty(tag)).thenReturn(value);
        when(env.getProperty(tag, defltValue)).thenReturn(defltValue);

        assertEquals(value, trans.setProperty(tag, value));
        assertEquals(value, trans.getProperty(tag));
        assertEquals(defltValue, trans.getProperty(tag, defltValue));
    }

    @Test
    public void testDecryptor() {
        when(env.decryptor()).thenReturn(Decryptor.NULL);

        assertEquals(Decryptor.NULL, trans.decryptor());
        assertEquals("tag", trans.decryptor().decrypt("tag"));
    }

    @Test
    public void testEncryptor() {
        when(env.encryptor()).thenReturn(Encryptor.NULL);

        assertEquals(Encryptor.NULL, trans.encryptor());
        assertEquals("tag", trans.encryptor().encrypt("tag"));
    }
}
