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

package org.onap.aaf.auth.dao.cass;

import org.onap.aaf.auth.dao.Cacheable;
import org.onap.aaf.auth.dao.Cached;
import org.onap.aaf.auth.dao.CachedDAO;

public abstract class CacheableData implements Cacheable {
    // WARNING:  DON'T attempt to add any members here, as it will 
    // be treated by system as fields expected in Tables
    protected int seg(Cached<?,?> cache, Object ... fields) {
        return cache==null?0:cache.invalidate(CachedDAO.keyFromObjs(fields));
    }

}
