/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.dao.aaf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;

public class JU_Bytification {

    @Test
    public void testNS() throws IOException {
    
        // Normal
        NsDAO.Data ns = new NsDAO.Data();
        ns.name = "org.osaaf.<pass>";
        ns.type = NsType.APP.type;

        ByteBuffer bb = ns.bytify();
    
        NsDAO.Data nsr = new NsDAO.Data();
        nsr.reconstitute(bb);
        check(ns,nsr);
    
        // Empty admin
//        ns.admin(true).clear();
        bb = ns.bytify();
        nsr = new NsDAO.Data();
        nsr.reconstitute(bb);
        check(ns,nsr);
    
        // Empty responsible
//        ns.responsible(true).clear();
        bb = ns.bytify();
        nsr = new NsDAO.Data();
        nsr.reconstitute(bb);
        check(ns,nsr);

        bb = ns.bytify();
        nsr = new NsDAO.Data();
        nsr.reconstitute(bb);
        check(ns,nsr);
    }

    private void check(NsDAO.Data a, NsDAO.Data b) {
        assertEquals(a.name,b.name);
        assertEquals(a.type,b.type);
//        assertEquals(a.admin.size(),b.admin.size());
    
//        for (String s: a.admin) {
//            assertTrue(b.admin.contains(s));
//        }
//    
//        assertEquals(a.responsible.size(),b.responsible.size());
//        for (String s: a.responsible) {
//            assertTrue(b.responsible.contains(s));
//        }
    }

    @Test
    public void testRole() throws IOException {
        RoleDAO.Data rd1 = new RoleDAO.Data();
        rd1.ns = "org.osaaf.<pass>";
        rd1.name = "my.role";
        rd1.perms(true).add("org.osaaf.<pass>.my.Perm|myInstance|myAction");
        rd1.perms(true).add("org.osaaf.<pass>.my.Perm|myInstance|myAction2");

        // Normal
        ByteBuffer bb = rd1.bytify();
        RoleDAO.Data rd2 = new RoleDAO.Data();
        rd2.reconstitute(bb);
        check(rd1,rd2);
    
        // Overshoot Buffer
        StringBuilder sb = new StringBuilder(300);
        sb.append("role|instance|veryLongAction...");
        for (int i=0;i<280;++i) {
            sb.append('a');
        }
        rd1.perms(true).add(sb.toString());
        bb = rd1.bytify();
        rd2 = new RoleDAO.Data();
        rd2.reconstitute(bb);
        check(rd1,rd2);
    
        // No Perms
        rd1.perms.clear();
    
        bb = rd1.bytify();
        rd2 = new RoleDAO.Data();
        rd2.reconstitute(bb);
        check(rd1,rd2);
    
        // 1000 Perms
        for (int i=0;i<1000;++i) {
            rd1.perms(true).add("com|inst|action"+ i);
        }

        bb = rd1.bytify();
        rd2 = new RoleDAO.Data();
        rd2.reconstitute(bb);
        check(rd1,rd2);

    }

    private void check(RoleDAO.Data a, RoleDAO.Data b) {
        assertEquals(a.ns,b.ns);
        assertEquals(a.name,b.name);
    
        assertEquals(a.perms.size(),b.perms.size());
        for (String s: a.perms) {
            assertTrue(b.perms.contains(s));
        }
    }

    @Test
    public void testPerm() throws IOException {
        PermDAO.Data pd1 = new PermDAO.Data();
        pd1.ns = "org.osaaf.<pass>";
        pd1.type = "my.perm";
        pd1.instance = "instance";
        pd1.action = "read";
        pd1.roles(true).add("org.osaaf.<pass>.my.Role");
        pd1.roles(true).add("org.osaaf.<pass>.my.Role2");

        // Normal
        ByteBuffer bb = pd1.bytify();
        PermDAO.Data rd2 = new PermDAO.Data();
        rd2.reconstitute(bb);
        check(pd1,rd2);
    
        // No Perms
        pd1.roles.clear();
    
        bb = pd1.bytify();
        rd2 = new PermDAO.Data();
        rd2.reconstitute(bb);
        check(pd1,rd2);
    
        // 1000 Perms
        for (int i=0;i<1000;++i) {
            pd1.roles(true).add("org.osaaf.<pass>.my.Role"+ i);
        }

        bb = pd1.bytify();
        rd2 = new PermDAO.Data();
        rd2.reconstitute(bb);
        check(pd1,rd2);

    }

    private void check(PermDAO.Data a, PermDAO.Data b) {
        assertEquals(a.ns,b.ns);
        assertEquals(a.type,b.type);
        assertEquals(a.instance,b.instance);
        assertEquals(a.action,b.action);
    
        assertEquals(a.roles.size(),b.roles.size());
        for (String s: a.roles) {
            assertTrue(b.roles.contains(s));
        }
    }

    @Test
    public void testUserRole() throws IOException {
        UserRoleDAO.Data urd1 = new UserRoleDAO.Data();
        urd1.user = "myname@abc.att.com";
        urd1.role("org.osaaf.<pass>","my.role");
        urd1.expires = new Date();

        // Normal
        ByteBuffer bb = urd1.bytify();
        UserRoleDAO.Data urd2 = new UserRoleDAO.Data();
        urd2.reconstitute(bb);
        check(urd1,urd2);
    
        // A null
        urd1.expires = null; 
        urd1.role = null;
    
        bb = urd1.bytify();
        urd2 = new UserRoleDAO.Data();
        urd2.reconstitute(bb);
        check(urd1,urd2);
    }

    private void check(UserRoleDAO.Data a, UserRoleDAO.Data b) {
        assertEquals(a.user,b.user);
        assertEquals(a.role,b.role);
        assertEquals(a.expires,b.expires);
    }


    @Test
    public void testCred() throws IOException {
        CredDAO.Data cd = new CredDAO.Data();
        cd.id = "m55555@abc.att.com";
        cd.ns = "org.osaaf.abc";
        cd.type = 2;
        cd.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
        cd.expires = new Date();

        // Normal
        ByteBuffer bb = cd.bytify();
        CredDAO.Data cd2 = new CredDAO.Data();
        cd2.reconstitute(bb);
        check(cd,cd2);
    
        // nulls
        cd.expires = null;
        cd.cred = null;
    
        bb = cd.bytify();
        cd2 = new CredDAO.Data();
        cd2.reconstitute(bb);
        check(cd,cd2);

    }

    private void check(CredDAO.Data a, CredDAO.Data b) {
        assertEquals(a.id,b.id);
        assertEquals(a.ns,b.ns);
        assertEquals(a.type,b.type);
        if (a.cred==null) {
            assertEquals(a.cred,b.cred); 
        } else {
            int l = a.cred.limit();
            assertEquals(l,b.cred.limit());
            for (int i=0;i<l;++i) {
                assertEquals(a.cred.get(),b.cred.get());
            }
        }
    }

}
