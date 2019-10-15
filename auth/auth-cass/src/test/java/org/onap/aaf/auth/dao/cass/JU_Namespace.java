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

package org.onap.aaf.auth.dao.cass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.misc.env.APIException;

public class JU_Namespace {

    Namespace namespace;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
    }

    @Test
    public void testInit() {
        new Namespace();
        NsDAO.Data data = new NsDAO.Data();
        data.name = "name";
        namespace = new Namespace(data);
        assertTrue(namespace.name.equals("name"));
        data.attrib = new HashMap<>();
        namespace = new Namespace(data);
        data.attrib.put("test", "test");
        namespace = new Namespace(data);
    }



    @Test
    public void testSecondConstructor() {

        NsDAO.Data data = new NsDAO.Data();
        data.name = "name";
        List<String> owner = new ArrayList<>();
        List<String> admin = new ArrayList<>();;
        namespace = new Namespace(data,owner, admin);
        assertTrue(namespace.name.equals("name"));
        data.attrib = new HashMap<>();
        namespace = new Namespace(data,owner, admin);
        data.attrib.put("test", "test");
        namespace = new Namespace(data ,owner, admin);

        NsDAO.Data retData = namespace.data();
        assertTrue(retData.name.equals("name"));

    }
    @Test
    public void testBytify() {
        testSecondConstructor();
        try {
            ByteBuffer retVal = namespace.bytify();
            namespace.reconstitute(retVal);
            namespace.hashCode();
            namespace.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testEquals() {
        testSecondConstructor();
        NsDAO.Data data = new NsDAO.Data();
        data.name = "name";
        Namespace nameObj = null;
        assertFalse(namespace.equals(nameObj));
        assertFalse(namespace.equals(data));
        nameObj = new Namespace(data);
        assertTrue(namespace.equals(nameObj));
    }

}