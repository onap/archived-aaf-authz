/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package org.onap.aaf.authz.cadi;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.cssa.rserv.TransFilter;
import org.onap.aaf.dao.aaf.cached.CachedCertDAO;
import org.onap.aaf.dao.aaf.cass.CertDAO.Data;

import org.onap.aaf.cadi.principal.X509Principal;
import org.onap.aaf.cadi.taf.cert.CertIdentity;
import org.onap.aaf.cadi.taf.cert.X509Taf;

/**
 * Direct view of CertIdentities
 * 
 * Warning:  this class is difficult to instantiate.  The only service that can use it is AAF itself, and is thus 
 * entered in the "init" after the CachedCertDAO is created.
 * 
 *
 */
public class DirectCertIdentity implements CertIdentity {
	private static CachedCertDAO certDAO;

	@Override
	public Principal identity(HttpServletRequest req, X509Certificate cert,	byte[] _certBytes) throws CertificateException {
	    	byte[] certBytes = _certBytes;
		if(cert==null && certBytes==null) {
		    return null;
		}
		if(certBytes==null) {
		    certBytes = cert.getEncoded();
		}
		byte[] fingerprint = X509Taf.getFingerPrint(certBytes);

		AuthzTrans trans = (AuthzTrans) req.getAttribute(TransFilter.TRANS_TAG);
		
		Result<List<Data>> cresp = certDAO.read(trans, ByteBuffer.wrap(fingerprint));
		if(cresp.isOKhasData()) {
			Data cdata = cresp.value.get(0);
			return new X509Principal(cdata.id,cert,certBytes);
		}
		return null;
	}

	public static void set(CachedCertDAO ccd) {
		certDAO = ccd;
	}

}
