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

package org.onap.aaf.auth.locate.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import locate.v1_0.Endpoint;
import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoint.SpecialPorts;
import locate.v1_0.MgmtEndpoints;

public class JU_LocateValidatorTest {

    @Mock
    private Endpoint endpoint;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Endpoints endpoints;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgmtEndpoints me;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgmtEndpoint mgmtEndpoint;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SpecialPorts specialPort;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNullEndPoint() {
        LocateValidator validator = new LocateValidator();

        validator.endpoint(null);
        assertEquals("Endpoint Data is null.\n", validator.errs());
    }

    @Test
    public void testEndPoint() {
        LocateValidator validator = new LocateValidator();

        when(endpoint.getName()).thenReturn("Endpoint1");
        when(endpoint.getHostname()).thenReturn("HOST1");
        when(endpoint.getPort()).thenReturn(9090);
        when(endpoint.getProtocol()).thenReturn("HTTP");

        validator.endpoint(endpoint);

        assertEquals("Endpoint Name must prefixed by Namespace\n", validator.errs());
    }

    @Test
    public void testSubProtoCol() {
        LocateValidator validator = new LocateValidator();

        List<String> subProtocol = new ArrayList<>();
        subProtocol.add(null);

        when(endpoint.getName()).thenReturn("EndPoint.Endpoint1");
        when(endpoint.getHostname()).thenReturn("HOST1");
        when(endpoint.getPort()).thenReturn(9090);
        when(endpoint.getProtocol()).thenReturn("HTTP");
        when(endpoint.getSubprotocol()).thenReturn(subProtocol);

        validator.endpoint(endpoint);

        assertEquals("Endpoint Subprotocol is null.\n", validator.errs());
    }

    @Test
    public void testNullEndpoints() {
        LocateValidator validator = new LocateValidator();

        validator.endpoints(null, false);
        validator.mgmt_endpoint_key(null);
        validator.mgmt_endpoints(null, false);
        assertEquals("Endpoints Data is null.\n" + "MgmtEndpoints Data is null.\n" + "MgmtEndpoints Data is null.\n",
                validator.errs());
    }

    @Test
    public void testEndpointsWithListContaingNull() {
        LocateValidator validator = new LocateValidator();
        when(endpoints.getEndpoint().size()).thenReturn(0);
        when(me.getMgmtEndpoint().size()).thenReturn(0);

        validator.endpoints(endpoints, true);
        validator.mgmt_endpoints(me, false);
        assertEquals("Endpoints contains no endpoints\n" + "MgmtEndpoints contains no data\n", validator.errs());
    }

    @Test
    public void testEndpointsWithSpecialPortsNull() {
        LocateValidator validator = new LocateValidator();

        when(endpoint.getName()).thenReturn("EndPoint.Endpoint1");
        when(endpoint.getHostname()).thenReturn("HOST1");
        when(endpoint.getPort()).thenReturn(9090);
        when(endpoint.getProtocol()).thenReturn("HTTP");
        List<String> subprotocol = new ArrayList<>();
        when(endpoint.getSubprotocol()).thenReturn(subprotocol);

        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpoint);

        when(mgmtEndpoint.getName()).thenReturn("EndPoint.Endpoint1");
        when(mgmtEndpoint.getHostname()).thenReturn("HOST1");
        when(mgmtEndpoint.getPort()).thenReturn(9090);
        when(mgmtEndpoint.getProtocol()).thenReturn("HTTP");
        List<SpecialPorts> specialPorts = new ArrayList<>();
        specialPorts.add(null);
        when(mgmtEndpoint.getSpecialPorts()).thenReturn(specialPorts);
        List<MgmtEndpoint> mgmtEndpoints = new ArrayList<>();
        mgmtEndpoints.add(mgmtEndpoint);

        when(endpoints.getEndpoint()).thenReturn(endpointList);
        when(me.getMgmtEndpoint()).thenReturn(mgmtEndpoints);

        validator.endpoints(endpoints, false);
        validator.mgmt_endpoints(me, true);
        assertEquals("Special Ports is null.\n", validator.errs());
    }

    @Test
    public void testEndpointsWithSpecialPorts() {
        LocateValidator validator = new LocateValidator();

        when(mgmtEndpoint.getName()).thenReturn("EndPoint.Endpoint1");
        when(mgmtEndpoint.getHostname()).thenReturn("HOST1");
        when(mgmtEndpoint.getPort()).thenReturn(9090);
        when(mgmtEndpoint.getProtocol()).thenReturn("HTTP");

        List<SpecialPorts> specialPorts = new ArrayList<>();
        specialPorts.add(specialPort);

        when(specialPort.getName()).thenReturn("Port1");
        when(specialPort.getProtocol()).thenReturn("HTTP");
        when(specialPort.getPort()).thenReturn(9090);

        List<String> versions = new ArrayList<>();
        versions.add("1");

        when(specialPort.getProtocolVersions()).thenReturn(versions);

        when(mgmtEndpoint.getSpecialPorts()).thenReturn(specialPorts);
        List<MgmtEndpoint> mgmtEndpoints = new ArrayList<>();
        mgmtEndpoints.add(mgmtEndpoint);

        when(me.getMgmtEndpoint()).thenReturn(mgmtEndpoints);

        validator.endpoints(endpoints, false);
        validator.mgmt_endpoints(me, true);
        validator.mgmt_endpoint_key(me);
        assertEquals(false, validator.err());

    }
}
