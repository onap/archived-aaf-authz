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

package org.onap.aaf.auth.log4j.test;

import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.log4j.Log4JAccessAppender;
import org.onap.aaf.cadi.Access;

public class JU_Log4jAccessAppender {

    @Mock
    Access access;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testRequiresLayout() {
        Log4JAccessAppender log4jObj = new Log4JAccessAppender(access);
        boolean retObj = log4jObj.requiresLayout();
        assertFalse(retObj);
    }

    @Test
    public void testClose() {
        Log4JAccessAppender log4jObj = new Log4JAccessAppender(access);
        log4jObj.close();

    }

    @Test
    public void testAppend() {
        Log4jAccessAppenderImpl log4jObj = new Log4jAccessAppenderImpl(access);
        LoggingEvent event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.FATAL,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
        Mockito.doReturn(true).when(access).willLog(Access.Level.ERROR);
        event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.ERROR,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
        event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.ALL,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
    }

    @Test
    public void testAppendWARN() {
        Log4jAccessAppenderImpl log4jObj = new Log4jAccessAppenderImpl(access);
        Mockito.doReturn(false).when(access).willLog(Access.Level.WARN);
        LoggingEvent event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.WARN,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
    }

    @Test
    public void testAppendINFO() {
        Log4jAccessAppenderImpl log4jObj = new Log4jAccessAppenderImpl(access);
        Mockito.doReturn(true).when(access).willLog(Access.Level.INFO);
        LoggingEvent event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.INFO,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
    }

    @Test
    public void testAppendWTrace() {
        Log4jAccessAppenderImpl log4jObj = new Log4jAccessAppenderImpl(access);
        Mockito.doReturn(false).when(access).willLog(Access.Level.TRACE);
        LoggingEvent event=new LoggingEvent("com.chililog.server.engine",Logger.getLogger(Log4JAccessAppender.class),(new Date()).getTime(),Level.TRACE,"test",Thread.currentThread().getName(),null,null,null,null);
        log4jObj.append(event);
    }

    class Log4jAccessAppenderImpl extends Log4JAccessAppender{

        public Log4jAccessAppenderImpl(Access access) {
            super(access);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void append(LoggingEvent event) {
            super.append(event);
        }

    }
}
