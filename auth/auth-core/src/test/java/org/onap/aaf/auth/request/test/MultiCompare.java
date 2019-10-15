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

import aaf.v2_0.MultiRequest;

public class MultiCompare extends RosettaCompare<MultiRequest>  {
    public MultiCompare() {
        super(MultiRequest.class);
    }

    @Override
    public MultiRequest newOne() {
        MultiRequest multi = new MultiRequest();
        multi.setNsRequest(NSCompare.create());
        multi.getNsAttribRequest().add(NSAttribCompare.create());
        multi.getNsAttribRequest().add(NSAttribCompare.create());
        multi.getRoleRequest().add(RoleCompare.create());
        multi.getRoleRequest().add(RoleCompare.create());
        multi.getPermRequest().add(PermCompare.create());
        multi.getPermRequest().add(PermCompare.create());
        multi.getCredRequest().add(CredCompare.create());
        multi.getCredRequest().add(CredCompare.create());
        multi.getUserRoleRequest().add(UserRoleCompare.create());
        multi.getUserRoleRequest().add(UserRoleCompare.create());
        multi.getRolePermRequest().add(RolePermCompare.create());
        multi.getRolePermRequest().add(RolePermCompare.create());
    
    
        GregorianCalendar gc = new GregorianCalendar();
        multi.setStart(Chrono.timeStamp(gc));
        gc.add(GregorianCalendar.MONTH, 1);
        multi.setEnd(Chrono.timeStamp(gc));
        return multi;
    }

    public void compare(MultiRequest t1, MultiRequest t2) {
        new NSCompare().compare(t1.getNsRequest(), t2.getNsRequest());
        // Will have to find by key for others.
    
        assertEquals(t1.getStart(),t2.getStart());
        assertEquals(t1.getEnd(),t2.getEnd());
    }
}