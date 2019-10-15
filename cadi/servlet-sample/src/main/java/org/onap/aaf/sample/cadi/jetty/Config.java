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

package org.onap.aaf.sample.cadi.jetty;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.webapp.WebAppContext;
import org.onap.aaf.cadi.filter.CadiFilter;



public class Config {
    /**
     * Method to make jetty configurations (others?) with more complex function possible
     * <p>
     * @param sc
     */
    public static final void addToContext(WebAppContext sc, String propFile) {
        sc.addFilter(CadiFilter.class,"/*",EnumSet.of(DispatcherType.REQUEST));
        sc.setInitParameter(org.onap.aaf.cadi.config.Config.CADI_PROP_FILES, propFile);
    }


}
