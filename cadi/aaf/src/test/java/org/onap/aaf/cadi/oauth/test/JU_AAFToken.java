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

package org.onap.aaf.cadi.oauth.test;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.UUID;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.oauth.AAFToken;

public class JU_AAFToken {

    @Test
    public void testMax() throws CadiException {
        UUID uuid = new UUID(Long.MAX_VALUE,Long.MAX_VALUE);
        String token = AAFToken.toToken(uuid);
        UUID uuid2 = AAFToken.fromToken(token);
        assertEquals(uuid, uuid2);
    }

    @Test
    public void testMin() throws CadiException {
        UUID uuid = new UUID(Long.MIN_VALUE,Long.MIN_VALUE);
        String token = AAFToken.toToken(uuid);
        UUID uuid2 = AAFToken.fromToken(token);
        assertEquals(uuid, uuid2);
    }

    @Test
    public void testRandom() throws CadiException {
        for (int i=0;i<100;++i) {
            UUID uuid = UUID.randomUUID();
            String token = AAFToken.toToken(uuid);
            UUID uuid2 = AAFToken.fromToken(token);
            assertEquals(uuid, uuid2);
        }
    }

    @Test
    public void nullTest() {
        // Invalid characters
        assertNull(AAFToken.fromToken("~~invalid characters~~"));
    
        // Invalid CADI tokens
        assertNull(AAFToken.fromToken("ABCDEF"));
        assertNull(AAFToken.fromToken("12345678901234567890123456789012345678"));
    }

}
