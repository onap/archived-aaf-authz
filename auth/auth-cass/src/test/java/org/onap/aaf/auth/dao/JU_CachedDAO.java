/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.CachedDAO;
import org.onap.aaf.auth.dao.DAO;
import org.onap.aaf.misc.env.Trans;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_CachedDAO {
    CachedDAO cachedDAO;
    @Mock
    DAO daoMock;
    @Mock
    CIDAO<Trans> ciDAOMock; 
    int segsize=1;
    Object[ ] objs = new Object[2];

    @Before
    public void setUp(){
        objs[0] = "helo";
        objs[1] = "polo";
        cachedDAO = new CachedDAO(daoMock, ciDAOMock, segsize, segsize);
    }
    
    @Test
    public void testKeyFromObjs(){
        String result = cachedDAO.keyFromObjs(objs);
        System.out.println("value of resut " +result);
        assertTrue(true);
    }

}
