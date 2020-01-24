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
package org.onap.aaf.auth.cm;

import java.io.IOException;

import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.ca.X509andChain;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.Trans;

public class LocalCAImpl extends CA {

    protected LocalCAImpl(Access access, String caName, String env) throws IOException, CertException {
        super(access, caName, env);
        // TODO Auto-generated constructor stub
    }

    public LocalCAImpl(Access access, final String name, final String env, final String[][] params) throws IOException, CertException {
        super(access, name, env);
    }

    @Override
    public X509andChain sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
        // TODO Auto-generated method stub
        return null;
    }
}