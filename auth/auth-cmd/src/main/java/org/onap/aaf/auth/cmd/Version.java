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

package org.onap.aaf.auth.cmd;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

public class Version extends Cmd {
    private final String version;

    public Version(AAFcli aafcli) {
        super(aafcli, "version");
        version = aafcli.access.getProperty(Config.AAF_DEPLOYED_VERSION, Config.AAF_DEFAULT_API_VERSION);
    }

    @Override
    protected int _exec(int idx, String... args) throws CadiException, APIException, LocatorException {
        pw().println("AAF Command Line Tool");
        pw().print("Version: ");
        pw().println(version);
        return 200;
    }
}
