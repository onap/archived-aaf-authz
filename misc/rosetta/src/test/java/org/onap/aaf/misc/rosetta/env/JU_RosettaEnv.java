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

package org.onap.aaf.misc.rosetta.env;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.applet.Applet;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.jaxb.JAXBmar;
import org.onap.aaf.misc.rosetta.Saved;

public class JU_RosettaEnv {

    @Before
    public void setUp() {
        initMocks(this);
    }
    
    @Test
    public void testNewDataFactoryClass() {
        RosettaEnv rosettaObj = new RosettaEnv();
        try {
            Object retVal = rosettaObj.newDataFactory(Api.class);
            assertTrue(retVal instanceof RosettaDF);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testNewDataFactorySchema() {
        RosettaEnv rosettaObj = new RosettaEnv(Mockito.mock(Applet.class),"test");
        try {
            Object retVal = rosettaObj.newDataFactory(Mockito.mock(Schema.class),Api.class);
            assertTrue(retVal instanceof RosettaDF);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testNewDataFactoryQname() {
        RosettaEnv rosettaObj = new RosettaEnv(new String[] {"test"});
        rosettaObj = new RosettaEnv(Mockito.mock(Properties.class));
        try {
            Object retVal = rosettaObj.newDataFactory(Mockito.mock(QName.class),Api.class);
            assertTrue(retVal instanceof RosettaDF);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testNewDataFactoryQnameSchema() {
        RosettaEnv rosettaObj = new RosettaEnv("test", new String[] {"test"});
        rosettaObj = new RosettaEnv("test", Mockito.mock(Properties.class));
        try {
            Object retVal = rosettaObj.newDataFactory(Mockito.mock(Schema.class),Mockito.mock(QName.class),Api.class);
            assertTrue(retVal instanceof RosettaDF);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
