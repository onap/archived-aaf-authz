/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.authz.cadi;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.cadi.principal.X509Principal;
import com.att.cadi.taf.cert.CertIdentity;
import com.att.cadi.taf.cert.X509Taf;
import com.att.cssa.rserv.TransFilter;
import com.att.dao.aaf.cached.CachedCertDAO;
import com.att.dao.aaf.cass.CertDAO.Data;

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
