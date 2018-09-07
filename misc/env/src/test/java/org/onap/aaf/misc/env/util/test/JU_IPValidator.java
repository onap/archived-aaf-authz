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

package org.onap.aaf.misc.env.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.aaf.misc.env.util.IPValidator;

public class JU_IPValidator {

    @Test
    public void test() {
        assertTrue(IPValidator.ipv4("10.10.10.10"));
        assertTrue(IPValidator.ipv4("127.0.0.0"));
        assertFalse(IPValidator.ipv4("10"));
        assertFalse(IPValidator.ipv4("10.10.10"));
        assertFalse(IPValidator.ipv4("10.10.10."));
        assertFalse(IPValidator.ipv4("10.10.10.10."));
        assertFalse(IPValidator.ipv4("10.10.10.10.10"));
        assertFalse(IPValidator.ipv4("something10.10.10.10"));
        assertTrue(IPValidator.ipv4("0.10.10.10"));
        assertTrue(IPValidator.ipv4("0.0.0.0"));
        assertTrue(IPValidator.ipv4("0.10.10.10"));
        assertFalse(IPValidator.ipv4("011.255.255.255"));
        assertFalse(IPValidator.ipv4("255.01.255.255"));
        assertFalse(IPValidator.ipv4("255.255.255.256"));
        assertFalse(IPValidator.ipv4("255.299.255.255"));

        assertTrue(IPValidator.ipv6("0000:0000:0000:0000:0000:0000:0000:0000"));
        assertTrue(IPValidator.ipv6("0:0:0:0:0:0:0:0"));
        assertTrue(IPValidator.ipv6("2001:08DB:0000:0000:0023:F422:FE3B:AC10"));
        assertTrue(IPValidator.ipv6("2001:8DB:0:0:23:F422:FE3B:AC10"));
        assertTrue(IPValidator.ipv6("2001:8DB::23:F422:FE3B:AC10"));
        assertTrue(IPValidator.ipv6("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertTrue(IPValidator.ipv6("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"));
        assertFalse(IPValidator.ipv6("2001:8DB::23:G422:FE3B:AC10"));
        assertFalse(IPValidator.ipv6("2001:8DB::23:G422:FE3B:AC10"));
        // more than one Double Colons
        assertFalse(IPValidator.ipv6("0000:0000:0000::0000::0000"));
        assertFalse(IPValidator.ipv6("2001:8DB::23:G422:FE3B:AC10:FFFF"));

        assertTrue(IPValidator.ip("2001:08DB:0000:0000:0023:F422:FE3B:AC10"));
        assertTrue(IPValidator.ip("192.168.7.2"));
    }

}
