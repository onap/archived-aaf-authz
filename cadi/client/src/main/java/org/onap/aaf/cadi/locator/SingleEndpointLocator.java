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
package org.onap.aaf.cadi.locator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.onap.aaf.cadi.LocatorException;

public class SingleEndpointLocator implements SizedLocator<URI> {
    private final URI uri;
    private final static Item item = new Item() {};
    private Date noRetryUntil;

    public SingleEndpointLocator(final URI uri) {
        this.uri = uri;
    }

    public SingleEndpointLocator(final String endpoint) throws LocatorException {
        try {
            this.uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new LocatorException(e);
        }
    }

    @Override
    public URI get(Item item) throws LocatorException {
        return uri;
    }

    @Override
    public boolean hasItems() {
        if (noRetryUntil!=null) {
            if (new Date().after(noRetryUntil)) {
                noRetryUntil = null;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void invalidate(Item item) throws LocatorException {
        // one minute timeout, because there is no other item
        noRetryUntil = new Date(System.currentTimeMillis()+60000);
    }

    @Override
    public Item best() throws LocatorException {
        return item;
    }

    @Override
    public Item first() throws LocatorException {
        return item;
    }

    @Override
    public Item next(Item inItem) throws LocatorException {
        // only one item
        return null;
    }

    @Override
    public boolean refresh() {
        // Never refreshed
        return true;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void destroy() {
        // Nothing to do here
    }
}
