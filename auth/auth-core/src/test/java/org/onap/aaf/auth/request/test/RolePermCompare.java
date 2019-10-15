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

import java.util.GregorianCalendar;

import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.Pkey;
import aaf.v2_0.RolePermRequest;

public class RolePermCompare extends RosettaCompare<RolePermRequest>  {
    public RolePermCompare() {
        super(RolePermRequest.class);
    }

    public static RolePermRequest create() {
        RolePermRequest urr = new RolePermRequest();
        String in = instance();
        urr.setRole("org.osaaf.ns.role"+in);
        Pkey pkey = new Pkey();
        pkey.setType("org.osaaf.ns.myType"+in);
        pkey.setInstance("myInstance"+in);
        pkey.setAction("myAction"+in);
        urr.setPerm(pkey);
        GregorianCalendar gc = new GregorianCalendar();
        urr.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        urr.setEnd(Chrono.timeStamp(gc));
        return urr;
    }

    @Override
    public void compare(RolePermRequest t1, RolePermRequest t2) {
        assertEquals(t1.getRole(),t2.getRole());
        assertEquals(t1.getPerm().getType(),t1.getPerm().getType());
        assertEquals(t1.getPerm().getInstance(),t1.getPerm().getInstance());
        assertEquals(t1.getPerm().getAction(),t1.getPerm().getAction());
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public RolePermRequest newOne() {
        return create();
    }
}