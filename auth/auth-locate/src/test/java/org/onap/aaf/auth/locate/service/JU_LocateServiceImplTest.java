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
package org.onap.aaf.auth.locate.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.mapper.Mapper;
import org.onap.aaf.misc.env.APIException;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;

public class JU_LocateServiceImplTest {

    // Extend, because I don't want a "setter" in the original.  Compromised with a protected...
    private final class LocateServiceImplExtension extends LocateServiceImpl {
        private LocateServiceImplExtension(AuthzTrans trans, AAF_Locate locate, Mapper mapper) throws APIException {
            super(trans, locate, mapper);
        }
        public void set(LocateDAO ld) {
            locateDAO=ld;
        }
    }

    @Mock
    private AuthzTrans trans;
    @Mock
    private AAF_Locate aaf_locate;
    @Mock
    private LocateDAO locateDAO;
    @Mock
    private Mapper mapper;
    @Mock
    private Result<List<Data>> result;
    @Mock
    private Result endPointResult;
    @Mock
    private MgmtEndpoints meps;
    @Mock
    private MgmtEndpoint mgmtEndPoint;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws APIException {
        LocateServiceImplExtension locateServiceImpl = new LocateServiceImplExtension(trans, aaf_locate, mapper);
        locateServiceImpl.set(locateDAO);

        assertEquals(mapper, locateServiceImpl.mapper());

        when(locateDAO.readByName(trans, "http")).thenReturn(result);
        when(mapper.endpoints(result, "1.0", "other")).thenReturn(endPointResult);

        Result output = locateServiceImpl.getEndPoints(trans, "http", "1.0", "other");

        assertEquals(endPointResult, output);

        List<MgmtEndpoint> mgmtEndPoints = new ArrayList<>();
        mgmtEndPoints.add(mgmtEndPoint);

        when(mgmtEndPoint.getName()).thenReturn("http.Endpoint1");
        when(mgmtEndPoint.getHostname()).thenReturn("HOST1");
        when(mgmtEndPoint.getPort()).thenReturn(9090);
        when(mgmtEndPoint.getProtocol()).thenReturn("HTTP");

        when(meps.getMgmtEndpoint()).thenReturn(mgmtEndPoints);
        output = locateServiceImpl.putMgmtEndPoints(trans, meps);

        assertEquals(output.toString(), Result.ok().toString());

        when(trans.fish(any())).thenReturn(true);
        Data data = new LocateDAO.Data();
        when(mapper.locateData(mgmtEndPoint)).thenReturn(data);
        output = locateServiceImpl.removeMgmtEndPoints(trans, meps);

        assertEquals(output.toString(), Result.ok().toString());
    }

}
