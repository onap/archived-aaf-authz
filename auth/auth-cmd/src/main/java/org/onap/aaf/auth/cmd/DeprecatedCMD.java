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

package org.onap.aaf.auth.cmd;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;


/**
 * Use this class to deprecate methods and features, by pointing to the new
 * usages.
 * <p>
 * These commands will not show up in Help
 * @author Jonathan
 *
 * @param <X>
 */
public class DeprecatedCMD<X extends Cmd> extends BaseCmd<X> {
    private String text;

    @SuppressWarnings("unchecked")
    public DeprecatedCMD(Cmd cmd, String name, String text) {
        super((X)cmd,name);
        this.text = text;
    }

    @Override
    public int _exec(int idx, final String ... args) throws CadiException, APIException, LocatorException {
        pw().println(text);
        return idx;
    }

}
