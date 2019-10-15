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

package org.onap.aaf.auth.oauth.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.ws.handler.MessageContext.Scope;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_JSONPermLoaderFactoryTest {
    @Mock
    private AAFCon<?> aafcon;
    @Mock
    private AuthzTrans trans;
    @Mock
    private TimeTaken tt;
    @Mock
    Rcli c;
    @Mock
    private Future fs;
    @Mock
    private Question question;
    @Mock
    private Result<NsSplit> rdns;
    private NsSplit nss;

    private Access access;

    @Before
    public void setup() throws CadiException {
        access = new AuthzEnv();
        Define.set(access);
        initMocks(this);
        nss = new NsSplit("APPLICATION", "APPLICATION");
    }

    @Test
    public void testRemoteWithTimeOut() throws APIException, CadiException {
        when(trans.start("Call AAF Service", Env.REMOTE)).thenReturn(tt);
        when(aafcon.clientAs(Config.AAF_DEFAULT_API_VERSION, trans.getUserPrincipal())).thenReturn(c);
        when(c.read("/authz/perms/user/null?scopes=APPLICATION:HANDLER",
                "application/Perms+json;charset=utf-8;version=2.0")).thenReturn(fs);
        when(fs.get(0)).thenReturn(true);

        Set<String> scopes = new HashSet<String>();
        scopes.add(Scope.APPLICATION.toString());
        scopes.add(Scope.HANDLER.toString());

        JSONPermLoader factory = JSONPermLoaderFactory.remote(aafcon, 0);

        Result<String> loadJSONPerms = factory.loadJSONPerms(trans, null, scopes);

        assertEquals(0, loadJSONPerms.status);

        verify(tt, only()).done();
    }

    @Test
    public void testRemoteWith404() throws APIException, CadiException {
        when(trans.start("Call AAF Service", Env.REMOTE)).thenReturn(tt);
        when(aafcon.clientAs(Config.AAF_DEFAULT_API_VERSION, trans.getUserPrincipal())).thenReturn(c);
        when(c.read("/authz/perms/user/null?scopes=APPLICATION:HANDLER",
                "application/Perms+json;charset=utf-8;version=2.0")).thenReturn(fs);
        when(fs.get(0)).thenReturn(false);
        when(fs.code()).thenReturn(404);

        Set<String> scopes = new HashSet<String>();
        scopes.add(Scope.APPLICATION.toString());
        scopes.add(Scope.HANDLER.toString());

        JSONPermLoader factory = JSONPermLoaderFactory.remote(aafcon, 0);

        Result<String> loadJSONPerms = factory.loadJSONPerms(trans, null, scopes);

        assertEquals(Result.ERR_NotFound, loadJSONPerms.status);

        verify(tt, only()).done();
    }

    @Test
    public void testRemote() throws APIException, CadiException {
        when(trans.start("Call AAF Service", Env.REMOTE)).thenReturn(tt);
        when(aafcon.clientAs(Config.AAF_DEFAULT_API_VERSION, trans.getUserPrincipal())).thenReturn(c);
        when(c.read("/authz/perms/user/null?scopes=APPLICATION:HANDLER",
                "application/Perms+json;charset=utf-8;version=2.0")).thenReturn(fs);
        when(fs.get(0)).thenReturn(false);

        Set<String> scopes = new HashSet<String>();
        scopes.add(Scope.APPLICATION.toString());
        scopes.add(Scope.HANDLER.toString());

        JSONPermLoader factory = JSONPermLoaderFactory.remote(aafcon, 0);

        Result<String> loadJSONPerms = factory.loadJSONPerms(trans, null, scopes);

        assertEquals(Result.ERR_Backend, loadJSONPerms.status);

        verify(tt, only()).done();
    }

    @Test
    public void testDirectWhenPdNotOk() throws APIException, CadiException {

        Result<List<PermDAO.Data>> pd = Result.create(null, Result.ERR_Backend, "details", "vars");

        when(question.getPermsByUser(trans, "user", false)).thenReturn(pd);
        when(trans.start("Cached DB Perm lookup", Env.SUB)).thenReturn(tt);

        Set<String> scopes = new HashSet<String>();
        scopes.add(Scope.APPLICATION.toString());
        scopes.add(Scope.HANDLER.toString());

        JSONPermLoader factory = JSONPermLoaderFactory.direct(question);

        Result<String> loadJSONPerms = factory.loadJSONPerms(trans, "user", scopes);

        assertEquals(Result.ERR_Backend, loadJSONPerms.status);

        verify(tt, only()).done();
    }

    @Test
    public void testDirectWhenPdOk() throws APIException, CadiException {

        when(trans.start("Cached DB Perm lookup", Env.SUB)).thenReturn(tt);
        when(question.deriveNsSplit(trans, "name")).thenReturn(rdns);
        when(rdns.isOKhasData()).thenReturn(false);

        List<PermDAO.Data> list = new ArrayList<PermDAO.Data>();
        list.add(new PermDAO.Data(nss, "instance", "action"));
        list.add(new PermDAO.Data(nss, "instance", "action"));

        Result<List<PermDAO.Data>> pd = Result.create(list, Result.OK, "details", "vars");

        when(question.getPermsByUser(trans, "user", false)).thenReturn(pd);

        Set<String> scopes = new HashSet<String>();
        scopes.add(Scope.APPLICATION.toString());
        scopes.add(Scope.HANDLER.toString());

        JSONPermLoader factory = JSONPermLoaderFactory.direct(question);

        Result<String> loadJSONPerms = factory.loadJSONPerms(trans, "user", scopes);

        assertEquals(Result.OK, loadJSONPerms.status);
        assertEquals("Success", loadJSONPerms.details);
        assertEquals(
                "{\"perm\":[{\"ns\":\"APPLICATION\",\"type\":\"APPLICATION\",\"instance\":\"instance\",\"action\":\"action\"},{\"ns\":\"APPLICATION\",\"type\":\"APPLICATION\",\"instance\":\"instance\",\"action\":\"action\"}]}",
                loadJSONPerms.value);

        verify(tt, only()).done();
    }

}
