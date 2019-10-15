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

package org.onap.aaf.auth.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;
import org.onap.aaf.cadi.util.MaskFormatException;
import org.onap.aaf.cadi.util.NetMask;

import junit.framework.Assert;

public class JU_Mask {

    @Test
    public void test() throws Exception {
//        InetAddress ia = InetAddress.getLocalHost();
        InetAddress ia = InetAddress.getByName("192.168.0.0");
        NetMask mask = new NetMask(ia.getAddress());
        assertTrue(mask.isInNet(ia.getAddress()));
    
        mask = new NetMask("192.168.1/24");
        assertTrue(mask.isInNet("192.168.1.20"));
        assertTrue(mask.isInNet("192.168.1.255"));
        assertFalse(mask.isInNet("192.168.2.20"));
    
        mask = new NetMask("192.168.1/31");
        assertFalse(mask.isInNet("192.168.2.20"));
        assertFalse(mask.isInNet("192.168.1.20"));
        assertTrue(mask.isInNet("192.168.1.1"));
        assertFalse(mask.isInNet("192.168.1.2"));

        mask = new NetMask("192/8");
        assertTrue(mask.isInNet("192.168.1.1"));
        assertTrue(mask.isInNet("192.1.1.1"));
        assertFalse(mask.isInNet("193.168.1.1"));
    
        mask = new NetMask("/0");
        assertTrue(mask.isInNet("193.168.1.1"));
    
        String msg = "Should throw " + MaskFormatException.class.getSimpleName();
        try {
            mask = new NetMask("256.256.256.256");
            Assert.assertTrue(msg,false);
        } catch (MaskFormatException e) {
            Assert.assertTrue(msg,true);
        }
    }

}
