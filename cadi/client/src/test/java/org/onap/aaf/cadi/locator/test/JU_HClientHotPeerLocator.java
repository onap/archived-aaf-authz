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

package org.onap.aaf.cadi.locator.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.http.HX509SS;
import org.onap.aaf.cadi.locator.HClientHotPeerLocator;

public class JU_HClientHotPeerLocator {

    @Mock private HX509SS ssMock;

    private PropAccess access;
    private ByteArrayOutputStream outStream;

    // Note: - The IP and port are irrelevant for these tests
    private static final String goodURL1 = "fakeIP1:fakePort1/38/-90";  // Approx St Louis
    private static final String goodURL2 = "fakeIP2:fakePort2/33/-96";  // Approx Dallas
    private static final String badURL = "~%$!@#$//";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        outStream = new ByteArrayOutputStream();
        access = new PropAccess(new PrintStream(outStream), new String[0]);
    }

    @Test
    public void test() throws LocatorException {
        HClientHotPeerLocator loc;
        String urlStr = goodURL1 + ',' + goodURL2;
        loc = new HClientHotPeerLocator(access, urlStr, 0, "38.627", "-90.199", ssMock);
        assertThat(loc.hasItems(), is(true));

        String[] messages = outStream.toString().split(System.lineSeparator());
        String preffered = messages[2].split(" ", 4)[3];
        String alternate = messages[3].split(" ", 4)[3];
        assertThat(preffered, is("Preferred Client is " + goodURL1));
        assertThat(alternate, is("Alternate Client is " + goodURL2));

        HClient firstClient = loc.get(loc.first());
        HClient bestClient = loc.bestClient();
        assertThat(bestClient, is(firstClient));

        Locator.Item item = loc.first();
        assertThat(loc.info(item), is(goodURL1));

        item = loc.next(item);
        assertThat(loc.info(item), is(goodURL2));

        item = loc.next(item);
        assertThat(item, is(nullValue()));
        assertThat(loc.info(item), is("Invalid Item"));

        item = loc.first();
        loc.invalidate(item);

        loc.invalidate(loc.bestClient());
        loc.invalidate(loc.get(loc.next(item)));
        loc.destroy();
    }

    @Test(expected = LocatorException.class)
    public void failuresTest() throws LocatorException {
        HClientHotPeerLocator loc;
        String urlStr = goodURL1 + ',' + goodURL2 + ',' + badURL;
        loc = new HClientHotPeerLocator(access, urlStr, 1000000, "38.627", "-90.199", ssMock);
        String[] messages = outStream.toString().split(System.lineSeparator());
        String preffered = messages[2].split(" ", 4)[3];
        String alternate1 = messages[3].split(" ", 4)[3];
        String alternate2 = messages[4].split(" ", 4)[3];
        assertThat(preffered, is("Preferred Client is " + badURL));
        assertThat(alternate1, is("Alternate Client is " + goodURL1));
        assertThat(alternate2, is("Alternate Client is " + goodURL2));

        outStream.reset();

        loc.invalidate(loc.first());

        loc.destroy();
        loc.best();
    }

    @Test
    public void hasNoItemTest() throws LocatorException {
        HClientHotPeerLocator loc;
        loc = new HClientHotPeerLocator(access, badURL, 0, "38.627", "-90.199", ssMock);
        assertThat(loc.hasItems(), is(false));
        loc.invalidate(loc.first());
    }

    @Test(expected = LocatorException.class)
    public void invalidClientTest() throws LocatorException {
        @SuppressWarnings("unused")
        HClientHotPeerLocator loc = new HClientHotPeerLocator(access, "InvalidClient", 0, "38.627", "-90.199", ssMock);
    }

    @Test(expected = LocatorException.class)
    public void coverageTest() throws LocatorException {
        CoverageLocator loc;
        String urlStr = goodURL1 + ',' + goodURL2;
        loc = new CoverageLocator(access, urlStr, 0, "38.627", "-90.199", ssMock);
        assertThat(loc._invalidate(null), is(nullValue()));
        loc._destroy(null);

        loc._newClient("bad string");
    }

    private class CoverageLocator extends HClientHotPeerLocator {
        public CoverageLocator(Access access, String urlstr, long invalidateTime, String localLatitude,
                String localLongitude, HX509SS ss) throws LocatorException {
            super(access, urlstr, invalidateTime, localLatitude, localLongitude, ss);
        }
        public HClient _newClient(String clientInfo) throws LocatorException { return super._newClient(clientInfo); }
        public HClient _invalidate(HClient client) { return super._invalidate(client); }
        public void _destroy(HClient client) { super._destroy(client); }
    }
}
