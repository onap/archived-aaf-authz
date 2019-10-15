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

package org.onap.aaf.cadi.sso.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.sso.AAFSSO;

public class JU_AAFSSO {

    private static final String resourceDirString = "src/test/resources";
    private static final String aafDir = resourceDirString + "/aaf";

    private ByteArrayInputStream inStream;

    @Before
    public void setup() {
        System.setProperty("user.home", aafDir);

        // Simulate user input
        inStream = new ByteArrayInputStream("test\npassword".getBytes());
        System.setIn(inStream);
    }

    @After
    public void tearDown() {
//        recursiveDelete(new File(aafDir));
    }

    @Test
    public void test()  {
    
    // Note  this is desctructive of personal dirs, and doesn't really test anything.  Needs redoing. 
//        AAFSSO sso;
//        String[] args;
//
//        args = new String[] {
//                "-login",
//                "-noexit",
//        };
//        try {
//            sso = new AAFSSO(args);
//    
//        assertThat(new File(aafDir).exists(), is(true));
//        assertThat(new File(aafDir + "/.aaf").exists(), is(true));
//        assertThat(new File(aafDir + "/.aaf/keyfile").exists(), is(true));
//        assertThat(new File(aafDir + "/.aaf/sso.out").exists(), is(true));
//        assertThat(sso.loginOnly(), is(true));
//
//// Not necessarily true
////        assertThat(new File(aafDir + "/.aaf/sso.props").exists(), is(true));
//    
//        sso.setLogDefault();
//        sso.setStdErrDefault();
//
//        inStream.reset();
//        args = new String[] {
//                "-logout",
//                "\\*",
//                "-noexit",
//        };
//        sso = new AAFSSO(args);
//
//        assertThat(new File(aafDir).exists(), is(true));
//        assertThat(new File(aafDir + "/.aaf").exists(), is(true));
//        assertThat(new File(aafDir + "/.aaf/keyfile").exists(), is(false));
//        assertThat(new File(aafDir + "/.aaf/sso.out").exists(), is(true));
//        assertThat(sso.loginOnly(), is(false));
//
//        PropAccess access = sso.access();
//        assertThat(sso.enc_pass(), is(access.getProperty(Config.AAF_APPPASS)));
//        assertThat(sso.user(), is(access.getProperty(Config.AAF_APPID)));
//
//        sso.addProp("key", "value");
//        assertThat(sso.err(), is(nullValue()));
//    
//        assertThat(sso.useX509(), is(false));
////
////        sso.close();
//        } catch (IOException | CadiException e) {
//            e.printStackTrace();
//        }

    }

    private void recursiveDelete(File file) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                recursiveDelete(f);
            }
            f.delete();
        }
        file.delete();
    }

}
