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
package org.onap.aaf.cadi.principal;

import java.security.Principal;

import org.onap.aaf.cadi.CadiException;

public abstract class TaggedPrincipal implements Principal {

    public TaggedPrincipal() {
        tagLookup = null;
    }

    public TaggedPrincipal(final TagLookup tl) {
        tagLookup = tl;
    }

    public abstract String tag();  // String representing what kind of Authentication occurred.

    public interface TagLookup {
        public String lookup() throws CadiException;
    }
    
    private TagLookup tagLookup;
    
    public void setTagLookup(TagLookup tl) {
        tagLookup = tl;
    }

    public String personalName() {
        if (tagLookup == null) {
            return getName();
        }
        try {
            return tagLookup.lookup();
        } catch (CadiException e) {
            return getName();
        }
    }

}
