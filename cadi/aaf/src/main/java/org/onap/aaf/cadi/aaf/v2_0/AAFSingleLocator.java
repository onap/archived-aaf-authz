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
 */

package org.onap.aaf.cadi.aaf.v2_0;

import java.net.URI;
import java.net.URISyntaxException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;

/**
 * This Locator good for using Inside Docker or K8s, where there is no real lookup,
 * and there is conflict between external and internal host names, due to
 * Service abstraction.
 *
 * @author Instrumental(Jonathan)
 *
 */
public class AAFSingleLocator implements Locator<URI> {

    private final URI uri;

    /**
     * NS here is "container" ns.  AAF NS is assumed to be AAF_NS at this level of client code.
     * @param cont_ns
     * @param prefix
     * @param version
     * @throws URISyntaxException
     */
    public AAFSingleLocator(final String uri) throws URISyntaxException {
        this.uri = new URI(uri);
    }

    @Override
    public URI get(Item item) throws LocatorException {
        return uri;
    }

    @Override
    public boolean hasItems() {
        return true;
    }

    @Override
    public void invalidate(Item item) throws LocatorException {
    }

    @Override
    public Item best() throws LocatorException {
        return new SingleItem();
    }

    @Override
    public Item first() throws LocatorException {
        return new SingleItem();
    }

    @Override
    public Item next(Item item) throws LocatorException {
        return null; // only one item
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void destroy() {
    }

    private class SingleItem implements Item {
    }

    public static AAFSingleLocator create(Access access, String url) throws URISyntaxException {
        return new AAFSingleLocator(url);
    }
}
