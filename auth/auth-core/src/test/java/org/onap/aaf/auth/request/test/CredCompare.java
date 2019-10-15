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

package org.onap.aaf.auth.request.test;

import static junit.framework.Assert.*;

import java.util.GregorianCalendar;

import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.CredRequest;

public class CredCompare extends RosettaCompare<CredRequest>  {
    public CredCompare() {
        super(CredRequest.class);
    }

    public static CredRequest create() {
        CredRequest rr = new CredRequest();
        String in = instance();
        rr.setId("m888"+ in + "@ns.att.com");
        rr.setPassword("Bogus0"+in);
        rr.setType(200);
        GregorianCalendar gc = new GregorianCalendar();
        rr.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        rr.setEnd(Chrono.timeStamp(gc));
        return rr;
    }

    @Override
    public void compare(CredRequest t1, CredRequest t2) {
        assertEquals(t1.getId(),t2.getId());
        assertEquals(t1.getPassword(),t2.getPassword());
        assertEquals(t1.getType(),t2.getType());
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public CredRequest newOne() {
        return create();
    }
}