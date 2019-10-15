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

package org.onap.aaf.auth.batch.approvalsets;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.org.DefaultOrgIdentity;

import com.datastax.driver.core.Cluster;

public class JU_URApprovalSet {

    @Mock
    AuthzTrans trans;
    @Mock
    Cluster cluster;
    @Mock
    PropAccess access;

    @Mock
    DataView dv;

    URApprovalSet approvalObj;

    @Mock
    Loader<UserRoleDAO.Data> lurdd;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testConstructor() {
        GregorianCalendar start = new GregorianCalendar();
        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
        urdd.rname = "owner";
        urdd.expires = new Date();
        Organization orgObj = Mockito.mock(Organization.class);
        try {

            Mockito.doReturn(urdd).when(lurdd).load();
            Mockito.doReturn(orgObj).when(trans).org();
            Mockito.doReturn(Mockito.mock(GregorianCalendar.class)).when(orgObj)
                    .expiration(null, Organization.Expiration.UserInRole);

            Result<RoleDAO.Data> rsRoleData = new Result<RoleDAO.Data>(null, 0,
                    "test", new Object[0]);
            Mockito.doReturn(rsRoleData).when(dv).roleByName(trans, urdd.role);
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            assertTrue(e.getMessage().contains("Error - test"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            urdd.rname = "admin";
            RoleDAO.Data dataObj = new RoleDAO.Data();
            Result<RoleDAO.Data> rsRoleData = new Result<RoleDAO.Data>(dataObj,
                    0, "test", new Object[0]);
            Mockito.doReturn(rsRoleData).when(dv).roleByName(trans, urdd.role);

            Result<NsDAO.Data> rsNsData = new Result<NsDAO.Data>(null, 0,
                    "test", new Object[0]);
            Mockito.doReturn(rsNsData).when(dv).ns(trans, urdd.ns);
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            assertTrue(e.getMessage().contains("Error - test"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            urdd.rname = "rname";
            NsDAO.Data dataObj = new NsDAO.Data();

            Result<NsDAO.Data> rsNsData = new Result<NsDAO.Data>(dataObj, 0,
                    "test", new Object[0]);
            Mockito.doReturn(rsNsData).when(dv).ns(trans, urdd.ns);

            Result<List<Data>> rsData = new Result<List<Data>>(null, 1, "test",
                    new Object[0]);
            Mockito.doReturn(rsData).when(dv).ursByRole(trans, urdd.role);
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            assertTrue(e.getMessage()
                    .contains("User 'null' in Role 'null' does not exist"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            List<Data> dataAL = new ArrayList<>();
            Data data = new Data();
            data.user = "user";
            urdd.user = "test";
            dataAL.add(data);
            Result<List<Data>> rsData = new Result<List<Data>>(dataAL, 0,
                    "test", new Object[0]);
            Mockito.doReturn(rsData).when(dv).ursByRole(trans, urdd.role);
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            assertTrue(e.getMessage()
                    .contains("User 'test' in Role 'null' does not exist"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            List<Data> dataAL = new ArrayList<>();
            Data data = new Data();
            data.user = "user";
            urdd.user = "user";
            dataAL.add(data);
            Result<List<Data>> rsData = new Result<List<Data>>(dataAL, 0,
                    "test", new Object[0]);
            Mockito.doReturn(rsData).when(dv).ursByRole(trans, urdd.role);

            Result<List<UserRoleDAO.Data>> rsURData = new Result<List<UserRoleDAO.Data>>(
                    null, 1, "test", new Object[0]);
            Mockito.doReturn(rsURData).when(dv).ursByRole(trans,
                    urdd.ns + ".owner");

            approvalObj = new URApprovalSet(trans, start, dv, lurdd);

            List<UserRoleDAO.Data> urDataAL = new ArrayList<>();
            UserRoleDAO.Data urData = new UserRoleDAO.Data();
            urData.user = "user";
            urDataAL.add(urData);
            rsURData = new Result<List<UserRoleDAO.Data>>(urDataAL, 0, "test",
                    new Object[0]);
            Mockito.doReturn(rsURData).when(dv).ursByRole(trans,
                    urdd.ns + ".owner");
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);

            List<Identity> identityAL = new ArrayList<Identity>();
            Identity idenObj = Mockito.mock(DefaultOrgIdentity.class);
            identityAL.add(idenObj);

            try {
                Mockito.doReturn(identityAL).when(orgObj).getApprovers(trans,
                        urdd.user);
            } catch (OrganizationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);

            urDataAL = new ArrayList<>();
            urData = new UserRoleDAO.Data();
            urData.user = "user1";
            urDataAL.add(urData);
            rsURData = new Result<List<UserRoleDAO.Data>>(urDataAL, 0, "test",
                    new Object[0]);
            Mockito.doReturn(rsURData).when(dv).ursByRole(trans,
                    urdd.ns + ".owner");
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            assertTrue(e.getMessage()
                    .contains("User 'null' in Role 'null' does not exist"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            try {
                Mockito.doThrow(new OrganizationException()).when(orgObj)
                        .getApprovers(trans, urdd.user);
            } catch (OrganizationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            approvalObj = new URApprovalSet(trans, start, dv, lurdd);
        } catch (CadiException e) {
            e.printStackTrace();
            assertTrue(e.getMessage()
                    .contains("User 'null' in Role 'null' does not exist"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
