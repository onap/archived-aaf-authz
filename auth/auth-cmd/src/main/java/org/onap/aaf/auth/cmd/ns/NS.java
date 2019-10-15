/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.aaf.auth.cmd.ns;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.auth.cmd.DeprecatedCMD;

public class NS extends BaseCmd<NS> {

    public NS(AAFcli aafcli) {
        super(aafcli, "ns");

        cmds.add(new Create(this));
        cmds.add(new Delete(this));
        cmds.add(new Admin(this));
        cmds.add(new Owner(this));
        cmds.add(new DeprecatedCMD<NS>(this,"responsible","'responsible' is deprecated.  use 'owner'")); // deprecated
        cmds.add(new Describe(this));
        cmds.add(new Attrib(this));
        cmds.add(new List(this));
    }
}
