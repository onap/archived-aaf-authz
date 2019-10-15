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

package org.onap.aaf.auth.direct;

import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.dao.cass.CertDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.principal.X509Principal;
import org.onap.aaf.cadi.taf.cert.CertIdentity;
import org.onap.aaf.cadi.taf.cert.X509Taf;

/**
 * Direct view of CertIdentities
 *
 * Warning:  this class is difficult to instantiate.  The only service that can use it is AAF itself, and is thus
 * entered in the "init" after the CachedCertDAO is created.
 *
 * @author Jonathan
 *
 */
public class DirectCertIdentity implements CertIdentity {
    private static CachedCertDAO certDAO;

    @Override
    public TaggedPrincipal identity(HttpServletRequest req, X509Certificate cert,    byte[] _certBytes) throws CertificateException {
            byte[] certBytes = _certBytes;
        if (cert==null && certBytes==null) {
            return null;
        }
        if (certBytes==null) {
            certBytes = cert.getEncoded();
        }
        byte[] fingerprint = X509Taf.getFingerPrint(certBytes);

        AuthzTrans trans = (AuthzTrans) req.getAttribute(TransFilter.TRANS_TAG);

        Result<List<Data>> cresp = certDAO.read(trans, ByteBuffer.wrap(fingerprint));
        if (cresp.isOKhasData()) {
            Data cdata = cresp.value.get(0);
            return new X509Principal(cdata.id,cert,certBytes,null);
        }
        return null;
    }

    public static void set(CachedCertDAO ccd) {
        certDAO = ccd;
    }

}
