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

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;

public class JU_Log4JLogTargetTest {

    @Mock
    Level level;
     
    @Mock
    Logger log;

    @Before
    public void setup() {
        initMocks(this);
    }
    
    @Test
    public void testLoggable() {
        Log4JLogTarget logObj = null;
        try {
            logObj = new Log4JLogTarget( "testLogger", Level.DEBUG);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        boolean retVal = logObj.isLoggable();
        assertTrue(retVal);
    }
    
    @Test
    public void testLog() {
        Log4JLogTarget logObj = null;
        try {
            logObj = new Log4JLogTarget( null, Level.DEBUG);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logObj.log(new Object[] {"test"});
    }
    
    @Test
    public void testLogThrowable() {
        Log4JLogTarget logObj = null;
        try {
            logObj = new Log4JLogTarget( null, Level.DEBUG);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logObj.log(new Throwable("test exception"), new Object[] {"test","test2","",null});
    }
    
    @Test
    public void testPrintf() {
        Log4JLogTarget logObj = null;
        try {
            logObj = new Log4JLogTarget( "", level);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logObj.printf("test", new Object[] {"test","test2",""});

    }
    
    @Test
    public void testSetEnv() {
        try {
            Log4JLogTarget.setLog4JEnv("test", Mockito.mock(BasicEnv.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}