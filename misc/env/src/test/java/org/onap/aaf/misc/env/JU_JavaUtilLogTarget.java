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

import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.impl.JavaUtilLogTarget;

public class JU_JavaUtilLogTarget {

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
        JavaUtilLogTarget logObj = new JavaUtilLogTarget( log, level);
        boolean retVal = logObj.isLoggable();
        
        assertFalse(retVal);
    }

    @Test
    public void testLog() {
        JavaUtilLogTarget logObj = new JavaUtilLogTarget( log, level);
        Mockito.doReturn(false).when(log).isLoggable(level);
        logObj.log(new Object[] {"test","test2",""});
        Mockito.doReturn(true).when(log).isLoggable(level);
        logObj.log(new Object[] {"test","test2",""});
        
    }
    
    @Test
    public void testLogThrowable() {
        JavaUtilLogTarget logObj = new JavaUtilLogTarget( log, level);
        
        Mockito.doReturn(true).when(log).isLoggable(level);
        logObj.log(new Throwable("test exception"), new Object[] {"test","test2",""});
        logObj.log(new Throwable(), new Object[] {"test","test2",""});
    }
    
    @Test
    public void testPrintf() {
        JavaUtilLogTarget logObj = new JavaUtilLogTarget( log, level);
        
        Mockito.doReturn(true).when(log).isLoggable(level);
        logObj.printf("test", new Object[] {"test","test2",""});

        Mockito.doReturn(false).when(log).isLoggable(level);
        logObj.printf("test", new Object[] {"test","test2",""});
    }
}
