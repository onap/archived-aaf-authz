/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.dao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.auth.dao.DAOException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

public class JU_DAOException {
    DAOException daoException;

    String message = "message";
    Throwable cause;
    @Before
    public void setUp(){
        daoException = new DAOException();
    }

    @Test
    public void testNoArgConstructor(){
        assertNull(daoException.getMessage());
    }

    @Test
    public void testOneArgConstructorMsg(){
        daoException = new DAOException("test message"); 
        assertTrue("test message".equalsIgnoreCase(daoException.getMessage()));
    }

    @Test
    public void testOneArgConstructorThrowable(){
        daoException = new DAOException(new Throwable()); 
        assertTrue("java.lang.Throwable".equalsIgnoreCase(daoException.getMessage()));
    }

    @Test
    public void testTwoArgConstructor(){
        daoException = new DAOException("test message", new Throwable()); 
        assertTrue("test message".equalsIgnoreCase(daoException.getMessage()));
    }
}
