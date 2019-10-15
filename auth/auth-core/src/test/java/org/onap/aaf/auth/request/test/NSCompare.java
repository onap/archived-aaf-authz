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

package org.onap.aaf.auth.request.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.NsRequest;

public class NSCompare extends RosettaCompare<NsRequest>  {
    public NSCompare() {
        super(NsRequest.class);
    }

    public static NsRequest create() {
        NsRequest nsr = new NsRequest();
        String in = instance();
        nsr.setName("org.osaaf.ns"+in);
        nsr.setDescription("Hello World"+in);
        nsr.getAdmin().add("Fred"+in);
        nsr.getAdmin().add("Barney"+in);
        nsr.getResponsible().add("Wilma"+in);
        nsr.getResponsible().add("Betty"+in);
        nsr.setType("Hello"+in);
        GregorianCalendar gc = new GregorianCalendar();
        nsr.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        nsr.setEnd(Chrono.timeStamp(gc));
        return nsr;
    }

    @Override
    public void compare(NsRequest t1, NsRequest t2) {
        assertEquals(t1.getName(),t2.getName());
        assertEquals(t1.getDescription(),t2.getDescription());
        for (String s : t1.getAdmin()) {
            assertTrue(t2.getAdmin().contains(s));
        }
        for (String s : t2.getAdmin()) {
            assertTrue(t1.getAdmin().contains(s));
        }
        assertEquals(t1.getType(),t2.getType());
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public NsRequest newOne() {
        return create();
    }
}