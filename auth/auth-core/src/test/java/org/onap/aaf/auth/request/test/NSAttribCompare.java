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

import aaf.v2_0.NsAttribRequest;
import aaf.v2_0.NsAttribRequest.Attrib;

public class NSAttribCompare extends RosettaCompare<NsAttribRequest>  {
    public NSAttribCompare() {
        super(NsAttribRequest.class);
    }

    public static NsAttribRequest create() {
        NsAttribRequest nar = new NsAttribRequest();
        String in = instance();

        nar.setNs("org.osaaf.ns"+in);
        Attrib attrib = new Attrib();
        attrib.setKey("swm");
        attrib.setValue("v"+instance());
        nar.getAttrib().add(attrib);
        attrib = new Attrib();
        attrib.setKey("scamp");
        attrib.setValue("v"+instance());
        nar.getAttrib().add(attrib);
        GregorianCalendar gc = new GregorianCalendar();
        nar.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        nar.setEnd(Chrono.timeStamp(gc));
        return nar;
    }

    @Override
    public void compare(NsAttribRequest t1, NsAttribRequest t2) {
        assertEquals(t1.getNs(),t2.getNs());
        for (Attrib a1 : t1.getAttrib()) {
            boolean ok = false;
            for (Attrib a2 : t2.getAttrib()) {
                if (a1.getKey().equals(a2.getKey()) &&
                    a1.getValue().equals(a2.getValue())) {
                    ok = true;
                    break;
                }
            }
            assertTrue("a2 Attribs in a1",ok);
        }
        for (Attrib a2 : t2.getAttrib()) {
            boolean ok = false;
            for (Attrib a1 : t1.getAttrib()) {
                if (a1.getKey().equals(a2.getKey()) &&
                    a1.getValue().equals(a2.getValue())) {
                    ok = true;
                    break;
                }
            }
            assertTrue("a2 Attribs in a1",ok);
        }
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public NsAttribRequest newOne() {
        return create();
    }
}