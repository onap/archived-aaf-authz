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

import static junit.framework.Assert.*;

import java.util.GregorianCalendar;

import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.PermRequest;

public class PermCompare extends RosettaCompare<PermRequest>  {
    public PermCompare() {
        super(PermRequest.class);
    }

    public static PermRequest create() {
        PermRequest pr = new PermRequest();
        String in = instance();
        pr.setType("org.osaaf.ns.perm"+in);
        pr.setInstance("instance"+in);
        pr.setAction("read");
        pr.setDescription("Hello World, Perm"+in);
        GregorianCalendar gc = new GregorianCalendar();
        pr.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        pr.setEnd(Chrono.timeStamp(gc));
        return pr;
    }

    @Override
    public void compare(PermRequest t1, PermRequest t2) {
        assertEquals(t1.getType(),t2.getType());
        assertEquals(t1.getInstance(),t2.getInstance());
        assertEquals(t1.getAction(),t2.getAction());
        assertEquals(t1.getDescription(),t2.getDescription());
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public PermRequest newOne() {
        return create();
    }
}