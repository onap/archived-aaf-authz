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

package org.onap.aaf.misc.env.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_AbsTrans {

    @Mock
    Env delegate;
    
    @Mock
    BasicEnv delegate1;
    
    @Mock
    LogTarget lt;
    
    @Before
    public void setUp() {
        initMocks(this);
    }
    
    class AbsTransImpl extends AbsTrans{

        public AbsTransImpl(Env delegate) {
            super(delegate);
            // TODO Auto-generated constructor stub
        }
        
        public AbsTransImpl(BasicEnv delegate) {
            super(delegate);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Slot slot(String name) {
            // TODO Auto-generated method stub
            return new Slot(-1, "test");
        }

        @Override
        public <T> T get(StaticSlot slot, T dflt) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String setProperty(String tag, String value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getProperty(String tag) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getProperty(String tag, String deflt) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Decryptor decryptor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Encryptor encryptor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected TimeTaken newTimeTaken(String name, int flag, Object ... values) {
            // TODO Auto-generated method stub
            return new TimeTaken("nameTest", Env.XML) {
                
                @Override
                public void output(StringBuilder sb) {
                    // TODO Auto-generated method stub
                    
                }
            };
        }
        
    }
    
    @Test
    public void testFatal() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).fatal();
        LogTarget lt = absTransObj.fatal();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testError() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).error();
        LogTarget lt = absTransObj.error();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testAudit() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).audit();
        LogTarget lt = absTransObj.audit();
        assertTrue(lt instanceof LogTarget);
    }
   
    @Test
    public void testInit() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).init();
        LogTarget lt = absTransObj.init();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testWarn() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).warn();
        LogTarget lt = absTransObj.warn();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testInfo() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        LogTarget lt = absTransObj.info();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testDebug() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).debug();
        LogTarget lt = absTransObj.debug();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testTrace() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).trace();
        LogTarget lt = absTransObj.trace();
        assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testStart() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        TimeTaken lt = absTransObj.start("test",1);
        assertEquals("nameTest", lt.name);
    }
    
    @Test
    public void testCheckpint() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        absTransObj.checkpoint("test");
        assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testCheckpintAddFlag() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        absTransObj.checkpoint("test",1);
        assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testAuditTrailWithEmptyTrail() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        Mockito.doReturn(true).when(lt).isLoggable();
        absTransObj.auditTrail(1, new StringBuilder(), 1);
        //assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testAuditTrail() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        Mockito.doReturn(true).when(lt).isLoggable();
        TimeTaken tt=absTransObj.newTimeTaken("test", 1);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.auditTrail(1, new StringBuilder(), 2);
        //assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testAuditTrailLoggableFalse() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        Mockito.doReturn(false).when(lt).isLoggable();
        TimeTaken tt=absTransObj.newTimeTaken("test", 1);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.auditTrail(1, new StringBuilder(), 1);
        //assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testAuditTrailNullSB() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        Mockito.doReturn(true).when(lt).isLoggable();
        TimeTaken tt=absTransObj.newTimeTaken("test", 1);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.auditTrail(1, null, 1);
        //assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testAuditTrailEmpptyFlag() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate);
        Mockito.doReturn(lt).when(delegate).info();
        Mockito.doReturn(true).when(lt).isLoggable();
        TimeTaken tt=absTransObj.newTimeTaken("test", 1);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.trail.add(tt);
        absTransObj.auditTrail(1, null, new int[] {});
        //assertEquals("nameTest", ((TimeTaken)absTransObj.trail.get(0)).name);
    }
    
    @Test
    public void testPut() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate1);
        try {
            absTransObj.put(absTransObj.slot("test"), "test");
        } catch(Exception e){
            assertTrue(e instanceof NullPointerException);
        }
    }
    
    @Test
    public void testGet() {
        AbsTransImpl absTransObj = new AbsTransImpl(delegate1);
        try {
            absTransObj.get(absTransObj.slot("test"), "test");
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(e instanceof NullPointerException);
        }
    }
}