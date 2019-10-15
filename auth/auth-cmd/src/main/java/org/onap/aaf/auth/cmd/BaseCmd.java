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

import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;


public class BaseCmd<CMD extends Cmd> extends Cmd  {
    protected List<Cmd>     cmds;

    public BaseCmd(AAFcli aafcli, String name, Param ... params) {
        super(aafcli, null, name, params);
        cmds = new ArrayList<>();
    }

    public BaseCmd(CMD parent, String name, Param ... params) {
        super(parent.aafcli, parent, name, params);
        cmds = new ArrayList<>();
    }


    @Override
    public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
        if (args.length-idx<1) {
            pw().println(build(new StringBuilder(),null).toString());
        } else {
            String s = args[idx];
            String name;
            Cmd empty = null;
            for (Cmd c: cmds) {
                name = c.getName();
                if (name==null && empty==null) { // Mark with Command is null, and take the first one.  
                    empty = c;
                } else if (s.equalsIgnoreCase(c.getName()))
                    return c.exec(idx+1, args);
            }
            if (empty!=null) {
                return empty.exec(idx, args); // If name is null, don't account for it on command line.  Jonathan 4-29
            }
            pw().println("Instructions not understood.");
        }
        return 0;
    }
}
