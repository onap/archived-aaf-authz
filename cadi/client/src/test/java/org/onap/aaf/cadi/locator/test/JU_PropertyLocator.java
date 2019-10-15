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
import static org.mockito.Mockito.*;
import org.junit.*;
import org.mockito.*;

import java.net.Socket;
import java.net.URI;

import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.locator.PropertyLocator;

public class JU_PropertyLocator {

    @Mock
    Socket socketMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(socketMock.isConnected()).thenReturn(true);
        when(socketMock.isClosed()).thenReturn(true).thenReturn(false);
    }

    @Test
    public void test() throws Exception {
        String uris = "https://fred.wilma.com:26444,https://tom.jerry.com:[534-535]";
        PropertyLocator pl = new PropertyLocator(uris, 0L, 1000*60*20L) {
            @Override protected Socket createSocket() { return socketMock; }
        };
        String str = pl.toString();
        assertThat(str.contains("https://fred.wilma.com:26444"), is(true));
        assertThat(str.contains("https://tom.jerry.com:534"), is(true));
        assertThat(str.contains("https://tom.jerry.com:535"), is(true));

        Item item = pl.first();
        assertThat(item.toString(), is("Item: 0 order: 0"));

        URI uri = pl.get(item);
        assertThat(uri.toString(), is("https://fred.wilma.com:26444"));

        assertThat(pl.get(null), is(nullValue()));

        assertThat(pl.hasItems(), is(true));

        assertThat(countItems(pl), is(3));
        pl.invalidate(pl.best());

        assertThat(countItems(pl), is(2));
        pl.invalidate(pl.best());

        assertThat(countItems(pl), is(1));

        pl.invalidate(pl.best());

        assertThat(pl.hasItems(), is(false));
        assertThat(countItems(pl), is(0));

        Thread.sleep(20L); // PL checks same milli...
        pl.refresh();

        assertThat(pl.hasItems(), is(true));

        assertThat(pl.next(null), is(nullValue()));

        // coverage...
        pl.invalidate(null);
        pl.invalidate(null);
        pl.invalidate(null);
        pl.invalidate(null);

        pl.destroy();

        pl = new PropertyLocator(uris);

    }

    @Test(expected=LocatorException.class)
    public void exceptionTest() throws LocatorException {
        new PropertyLocator(null);
    }

    private int countItems(PropertyLocator pl) throws LocatorException {
        int count = 0;
        for (Item i = pl.first(); i != null; i = pl.next(i)) {
            ++count;
        }
        return count;
    }

}
