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

import aaf.v2_0.UserRoleRequest;

public class UserRoleCompare extends RosettaCompare<UserRoleRequest>  {
    public UserRoleCompare() {
        super(UserRoleRequest.class);
    }

    public static UserRoleRequest create() {
        UserRoleRequest urr = new UserRoleRequest();
        String in = instance();
        urr.setUser("m125"+in + "@ns.att.com");
        urr.setRole("org.osaaf.ns.role"+in);
        GregorianCalendar gc = new GregorianCalendar();
        urr.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        urr.setEnd(Chrono.timeStamp(gc));
        return urr;
    }

    @Override
    public void compare(UserRoleRequest t1, UserRoleRequest t2) {
        assertEquals(t1.getUser(),t2.getUser());
        assertEquals(t1.getRole(),t2.getRole());
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }


    @Override
    public UserRoleRequest newOne() {
        return create();
    }
}