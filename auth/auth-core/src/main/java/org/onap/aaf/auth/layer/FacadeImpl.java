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

package org.onap.aaf.auth.layer;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;



public abstract class FacadeImpl {
    protected static final String IN = "in";

    protected void setContentType(HttpServletResponse response, TYPE type) {
        response.setContentType(type==Data.TYPE.JSON?"application/json":"text.xml");
    }

    protected void setCacheControlOff(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
