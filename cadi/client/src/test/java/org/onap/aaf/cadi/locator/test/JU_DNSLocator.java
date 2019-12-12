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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.locator.DNSLocator;

public class JU_DNSLocator {

    private PropAccess access;

    @Before
    public void setup() {
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
    }

    @Test
    public void test() throws LocatorException {
        DNSLocator dl;
        Item item;
        URI uri;

        dl = new DNSLocator(access, "https", "localhost", "8100-8101");

        item = dl.best();
        uri = dl.get(item);
        assertThat(uri.toString(), is("https://localhost:8100"));
        item = dl.best();
        assertThat(uri.toString(), is("https://localhost:8100"));

        assertThat(dl.hasItems(), is(true));
        for (item = dl.first(); item != null; item = dl.next(item)) {
            dl.invalidate(item);
        }
        assertThat(dl.hasItems(), is(false));

        // This doesn't actually do anything besides increase coverage
        dl.destroy();
    }

    @Test
    public void constructorTest() throws LocatorException {
        // For coverage
        new DNSLocator(access, "https", "localhost", "8100");
        new DNSLocator(access, "https", "localhost", "8100-8101");

        new DNSLocator(access, "http://localhost");
        new DNSLocator(access, "https://localhost");
        new DNSLocator(access, "https://localhost:8100");
        new DNSLocator(access, "https://localhost:[8100]");
        new DNSLocator(access, "https://localhost:[8100-8101]");
        new DNSLocator(access, "https://localhost:8000/");
        new DNSLocator(access, "https://aaf-locatexx.onapxxx:8095/locate");
        try {
            new DNSLocator(access, "https:localhost:8000");
            fail("Invalid URL should not pass");
        } catch (LocatorException e) {
            access.log(Level.DEBUG, "Valid Exception");

        }
    }

    @Test
    public void refreshTest() throws LocatorException {
        DNSLocator dl = new DNSLocator(access, "https", "bogushost", "8100-8101",
        	// Note: Lambda would be nice but need JDK 1.7 still
        	// PowerMock couldn't do InetAddress
        	new DNSLocator.DNSLookup() {
				@Override
				public InetAddress[] getAllByName(String host) throws UnknownHostException {
 					return new InetAddress[0];
				}
        	}
        );
        assertThat(dl.refresh(), is(true));
    }

    @Test(expected = LocatorException.class)
    public void throws1Test() throws LocatorException {
        new DNSLocator(access, null);
    }

    @Test(expected = LocatorException.class)
    public void throws2Test() throws LocatorException {
        new DNSLocator(access, "ftp:invalid");
    }

    @Test(expected = LocatorException.class)
    public void throws3Test() throws LocatorException {
        new DNSLocator(access, "https:localhost:[8100");
    }

    @Test(expected = LocatorException.class)
    public void throws4Test() throws LocatorException {
        new DNSLocator(access, "https:localhost:[]");
    }

    @Test(expected = LocatorException.class)
    public void throws5Test() throws LocatorException {
        new DNSLocator(access, "https:localhost:[8100-]");
    }

    @Test(expected = LocatorException.class)
    public void throws6Test() throws LocatorException {
        new DNSLocator(access, "https:localhost:[-8101]");
    }

    @Test(expected = LocatorException.class)
    public void throws7Test() throws LocatorException {
        new DNSLocator(access, "https:localhost:/");
    }

}
