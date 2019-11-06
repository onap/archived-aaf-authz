/*
 * Copyright (C) 2019 Ericsson Software Technology AB. All rights reserved.
 *
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
 * limitations under the License
 */
package org.onap.aaf.auth.cm.cmpv2client.impl;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cmpv2client.api.CmpClient;

/**
 * Implementation of the CmpClient Interface conforming to RFC4210 (Certificate Management Protocol (CMP)) and RFC4211 (
 * Certificate Request Message Format (CRMF)) standards.
 */
public final class CmpClientImpl implements CmpClient {

    @Override
    public X509Certificate createCertRequest(final String caName, final String profile, final CSRMeta csrMeta,
        final Certificate csr, final Date notBefore, final Date notAfter)
        throws CAOfflineException, CmpClientException {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public X509Certificate createCertRequest(final String caName, final String profile, final CSRMeta csrMeta,
        final Certificate csr)
        throws CAOfflineException, CmpClientException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public X509Certificate revokeCertRequest(final String caName, final X509Certificate cert, final int reason,
        final Date invalidityTime)
        throws CAOfflineException, CmpClientException {
        // TODO Auto-generated method stub
        return null;
    }
}

