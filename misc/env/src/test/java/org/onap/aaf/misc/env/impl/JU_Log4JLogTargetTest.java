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

import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.misc.env.APIException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Log4JLogTarget.class, Logger.class })
public class JU_Log4JLogTargetTest {

    @Mock
    Logger log;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Logger.class);
        when(Logger.getLogger("Info")).thenReturn(log);
        when(log.isEnabledFor(Level.DEBUG)).thenReturn(false);
    }

    @Test
    public void test() throws APIException {
        Log4JLogTarget target = new Log4JLogTarget(null, Level.INFO);
        Log4JLogTarget target1 = new Log4JLogTarget("Info", Level.DEBUG);

        assertFalse(target1.isLoggable());

    }
}