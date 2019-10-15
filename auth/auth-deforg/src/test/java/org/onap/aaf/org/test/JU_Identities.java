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

package org.onap.aaf.org.test;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.org.Identities;
import org.onap.aaf.org.Identities.Data;

/**
 *
 */
public class JU_Identities {
//
//    private static final String DATA_IDENTITIES = "/opt/app/onap/data/identities.dat";
//    private static File fids;
//    private static Identities ids;
//    private static AuthzEnv env;
//
//    /**
//     * @throws java.lang.Exception
//     */
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception {
//        env = new AuthzEnv();
//        AuthzTrans trans = env.newTransNoAvg();
//        // Note: utilize TimeTaken, from trans.start if you want to time.
//        fids = new File(DATA_IDENTITIES);
//        if (fids.exists()) {
//            ids = new Identities(fids);
//            ids.open(trans, 5000);
//        } else {
//        
//            throw new Exception("Data File for Tests, \"" + DATA_IDENTITIES 
//                    + "\" must exist before test can run. (Current dir is " + System.getProperty("user.dir") + ")");
//        }
//    }
//
//    /**
//     * @throws java.lang.Exception
//     */
//    @AfterClass
//    public static void tearDownAfterClass() throws Exception {
//        AuthzTrans trans = env.newTransNoAvg();
//        if (ids!=null) {
//            ids.close(trans);
//        }
//    }
//
//    /**
//     * @throws java.lang.Exception
//     */
//    @Before
//    public void setUp() throws Exception {
//    }
//
//    /**
//     * @throws java.lang.Exception
//     */
//    @After
//    public void tearDown() throws Exception {
//    }
// 
//    @Test
//    public void test() throws IOException {
//        Reuse reuse = ids.reuse(); // this object can be reused within the same thread.
//        Data id = ids.find("osaaf",reuse);
//        Assert.assertNotNull(id);
//        System.out.println(id);
//
//        id = ids.find("mmanager",reuse);
//        Assert.assertNotNull(id);
//        System.out.println(id);
//
//        //TODO Fill out JUnit with Tests of all Methods in "Data id"
//    }

}
