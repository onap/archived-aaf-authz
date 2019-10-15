/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;


@RunWith(MockitoJUnitRunner.class) 
public class JU_DirectAAFUserPass {

    @Mock
    Question question;

    @Mock
    AuthzEnv env;

    @Mock
    AuthzTrans trans;

    @Mock
    HttpServletRequest request;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(env.warn()).thenReturn(new LogTarget() {
        
            @Override
            public void printf(String fmt, Object... vars) {}
        
            @Override
            public void log(Throwable e, Object... msgs) {
                e.getMessage();
                e.printStackTrace();
                msgs.toString();
            
            }
        
            @Override
            public void log(Object... msgs) {
            }
        
            @Override
            public boolean isLoggable() {
            
                return true;
            }
        });
        when(env.error()).thenReturn(new LogTarget() {
        
            @Override
            public void printf(String fmt, Object... vars) {}
        
            @Override
            public void log(Throwable e, Object... msgs) {
                e.getMessage();
                e.printStackTrace();
                msgs.toString();
            
            }
        
            @Override
            public void log(Object... msgs) {
            }
        
            @Override
            public boolean isLoggable() {
            
                return true;
            }
        });
    }

    @Test
    public void testUserPass() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,0,"",new String[0]);
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doReturn(retVal1).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, null);
    
        assertFalse(retVal);
    }

    @Test
    public void testUserPassStateisRequest() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,1,"",new String[0]);
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doReturn(retVal1).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, request);
    
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testUserPassStateNotNull() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,1,"",new String[0]);
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doReturn(retVal1).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, "test");
    
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testUserPassTransChk() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,1,"",new String[0]);
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doReturn(retVal1).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, trans);
    
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testUserPassTransIpNotNull() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,1,"",new String[0]);
        Mockito.doReturn("test").when(trans).ip();
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doReturn(retVal1).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, trans);
    
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testUserExceptionChk() {
    
        DirectAAFUserPass aafLocatorObj=null;
        aafLocatorObj = new DirectAAFUserPass(env, question);
        Result<Date> retVal1 = new Result<Date>(null,1,"",new String[0]);
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        try {
            Mockito.doThrow(DAOException.class).when(question).doesUserCredMatch(trans, null, null);
        } catch (DAOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }
        boolean retVal = aafLocatorObj.validate(null, null, null, trans);
    
//        System.out.println(retVal);
        assertFalse(retVal);
    }

}