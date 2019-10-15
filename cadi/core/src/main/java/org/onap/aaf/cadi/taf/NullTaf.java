/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.cadi.taf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.Taf;
import org.onap.aaf.cadi.CachedPrincipal.Resp;


/**
 * This TAF is set at the very beginning of Filters and Valves so that if any configuration issues hit while
 * starting, the default behavior is to shut down traffic rather than leaving an open hole
 * <p>
 * @author Jonathan
 *
 */
public class NullTaf implements Taf, HttpTaf {
    // Singleton Pattern
    public NullTaf() {}

    /**
     * validate 
     * <p>
     * Always Respond with a NullTafResp, which declares it is unauthenticated, and unauthorized
     */
    public TafResp validate(LifeForm reading, String... info) {
        return NullTafResp.singleton();
    }

    /**
     * validate 
     * <p>
     * Always Respond with a NullTafResp, which declares it is unauthenticated, and unauthorized
     */
    public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        return NullTafResp.singleton();
    }

    public Resp revalidate(CachedPrincipal prin, Object state) {
        return Resp.NOT_MINE;
    }
}
