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

package org.onap.aaf.auth.dao.aaf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans.Metric;

import com.datastax.driver.core.Cluster;

import junit.framework.Assert;

/**
 * Do Setup of Cassandra for Cassandra JUnit Testing
 *
 *
 */
public class AbsJUCass {
    protected static final String AUTHZ = "authz";
    protected static Cluster cluster;
    protected static AuthzEnv env;
    protected static int iterations = 0;
    protected static float totals=0.0f;
    protected static float remote = 0.0f;
    protected static float json = 0.0f;
    protected static AuthzTrans trans;
    protected static boolean details = true;

    @BeforeClass
    public static void startup() throws APIException, IOException {
        synchronized(AUTHZ) {
            if (env==null) {
                final String resource = "cadi.properties";
                File f = new File("etc" + resource);
                InputStream is=null;
                Properties props = new Properties();
                try {
                    if (f.exists()) {
                        is = new FileInputStream(f);
                    } else {
                        URL rsrc = ClassLoader.getSystemResource(resource);
                        is = rsrc.openStream();
                    }
                    props.load(is);
                } finally {
                    if (is==null) {
                        env= new AuthzEnv();
                        Assert.fail(resource + " must exist in etc dir, or in Classpath");
                    }
                    is.close();
                }
                env = new AuthzEnv(props);
            }
        }
        cluster = CassAccess.cluster(env,"LOCAL");

        env.info().log("Connecting to Cluster");
        try {
            cluster.connect(AUTHZ);
        } catch (Exception e) {
            cluster=null;
            env.error().log(e);
            Assert.fail("Not able to connect to DB: " + e.getLocalizedMessage());
        }
        env.info().log("Connected");

        // Load special data here


        iterations = 0;

    }

    @AfterClass
    public static void shutdown() {
        if (cluster!=null) {
            cluster.close();
            cluster = null;
        }
    }

    @Before
    public void newTrans() {
        trans = env.newTrans();

        trans.setProperty(CassDAOImpl.USER_NAME, System.getProperty("user.name"));
    }

    @After
    public void auditTrail() {
        if (totals==0) { // "updateTotals()" was not called... just do one Trans
            StringBuilder sb = new StringBuilder();
            Metric metric = trans.auditTrail(4, sb, Env.JSON, Env.REMOTE);
            if (details) {
                env.info().log(
                sb,
                "Total time:",
                totals += metric.total,
                "JSON time: ",
                metric.buckets[0],
                "REMOTE time: ",
                metric.buckets[1]
                );
            } else {
                totals += metric.total;
            }
        }
    }

    protected void updateTotals() {
        Metric metric = trans.auditTrail(0, null, Env.JSON, Env.REMOTE);
        totals+=metric.total;
        json  +=metric.buckets[0];
        remote+=metric.buckets[1];
    }


    @AfterClass
    public static void print() {
        float transTime;
        if (iterations==0) {
            transTime=totals;
        } else {
            transTime=totals/iterations;
        }
        env.info().log(
        "Total time:",
        totals,
        "JSON time:",
        json,
        "REMOTE time:",
        remote,
        "Iterations:",
        iterations,
        "Transaction time:",
        transTime
        );
    }

    /**
     * Take a User/Pass and turn into an MD5 Hashed BasicAuth
     *
     * @param user
     * @param pass
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    //TODO: Gabe [JUnit] Issue
    public static byte[] userPassToBytes(String user, String pass)
            throws IOException, NoSuchAlgorithmException {
        // Take the form of BasicAuth, so as to allow any character in Password
        // (this is an issue in 1.0)
        // Also, it makes it quicker to evaluate Basic Auth direct questions
        String ba = Symm.base64url.encode(user + ':' + pass);
        // Take MD5 Hash, so that data in DB can't be reversed out.
        return Hash.hashMD5(ba.getBytes());
    }

}
