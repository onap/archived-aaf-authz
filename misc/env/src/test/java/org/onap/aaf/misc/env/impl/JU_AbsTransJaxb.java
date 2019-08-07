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

import static org.mockito.MockitoAnnotations.initMocks;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.DataFactory;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_AbsTransJaxb {

    @Mock
    EnvJAXB delegate;
    
    @Mock
    LogTarget lt;
    
    @Before
    public void setUp() {
        initMocks(this);
    }
    
    class AbsTransJAXBImpl extends AbsTransJAXB{

        public AbsTransJAXBImpl(EnvJAXB env) {
            super(env);
            // TODO Auto-generated constructor stub
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
        public Slot slot(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T get(StaticSlot slot, T dflt) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected TimeTaken newTimeTaken(String name, int flag, Object ... values) {
            // TODO Auto-generated method stub
            return null;
        }

        
    }
    
    
    @Test
    public void testNewDataFactory() {
        AbsTransJAXB absTransObj = new AbsTransJAXBImpl(delegate);
        DataFactory<Object> lt = null;

        try {
            Mockito.doReturn(lt).when(delegate).newDataFactory(new Class[] {AbsTransJAXB.class});
            lt = absTransObj.newDataFactory(new Class[] {AbsTransJAXB.class});
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testNewDataFactorySchema() {
        AbsTransJAXB absTransObj = new AbsTransJAXBImpl(delegate);
        DataFactory<Object> lt = null;
        Schema schema = Mockito.mock(Schema.class);
        try {
            Mockito.doReturn(lt).when(delegate).newDataFactory(schema, new Class[] {AbsTransJAXB.class});
            lt = absTransObj.newDataFactory(schema, new Class[] {AbsTransJAXB.class});
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testNewDataFactoryQname() {
        AbsTransJAXB absTransObj = new AbsTransJAXBImpl(delegate);
        DataFactory<Object> lt = null;
        QName schema = Mockito.mock(QName.class);
        try {
            Mockito.doReturn(lt).when(delegate).newDataFactory(schema, new Class[] {AbsTransJAXB.class});
            lt = absTransObj.newDataFactory(schema, new Class[] {AbsTransJAXB.class});
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //assertTrue(lt instanceof LogTarget);
    }
    
    @Test
    public void testNewDataFactorySchemaQname() {
        AbsTransJAXB absTransObj = new AbsTransJAXBImpl(delegate);
        DataFactory<Object> lt = null;
        QName qname = Mockito.mock(QName.class);
        Schema schema = Mockito.mock(Schema.class);
        try {
            Mockito.doReturn(lt).when(delegate).newDataFactory(schema, qname,new Class[] {AbsTransJAXB.class});
            lt = absTransObj.newDataFactory(schema, qname,new Class[] {AbsTransJAXB.class});
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //assertTrue(lt instanceof LogTarget);
    }
    
    
}