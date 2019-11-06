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
package org.onap.aaf.auth.cm.cmpv2client.api;

import java.security.cert.Certificate;
import java.util.Date;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cmpv2client.impl.CAOfflineException;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientException;

/**
 * This class represent CmpV2Client Interface for obtaining X.509 Digital Certificates in a Public Key Infrastructure
 * (PKI), making use of Certificate Management Protocol (CMPv2) operating on newest version: cmp2000(2).
 */
public interface CmpClient {

    /**
     * Requests for a External Root CA Certificate to be created for the passed public keyPair wrapped in a CSRMeta with
     * common details, accepts self-signed certificate. Basic Authentication using IAK/RV, Verification of the signature
     * (proof-of-possession) on the request is performed and an Exception thrown if verification fails or issue
     * encountered in fetching certificate from CA.
     *
     * @param caName    Information about the External Root Certificate Authority (CA) performing the event CA Name.
     *                  Could be {@code null}.
     * @param profile   Profile on CA server Client/RA Mode configuration on Server. Could be {@code null}.
     * @param csrMeta   Certificate Signing Request Meta Data. Must not be {@code null}.
     * @param csr       Certificate Signing Request {.cer} file. Must not be {@code null}.
     * @param notBefore An optional validity to set in the created certificate, Certificate not valid before this date.
     * @param notAfter  An optional validity to set in the created certificate, Certificate not valid after this date.
     * @return The newly created Certificate.
     *
     * @throws CAOfflineException if External CA that is offline
     * @throws CmpClientException if client error occurs.
     */
    Certificate createCertRequest(String caName, String profile, CSRMeta csrMeta, Certificate csr,
        Date notBefore, Date notAfter)
        throws CAOfflineException, CmpClientException;

    /**
     * Requests for a External Root CA Certificate to be created for the passed public keyPair wrapped in a CSRMeta with
     * common details, accepts self-signed certificate. Basic Authentication using IAK/RV, Verification of the signature
     * (proof-of-possession) on the request is performed and an Exception thrown if verification fails or issue
     * encountered in fetching certificate from CA.
     *
     * @param caName  Information about the External Root Certificate Authority (CA) performing the event CA Name. Could
     *                be {@code null}.
     * @param csrMeta Certificate Signing Request Meta Data. Must not be {@code null}.
     * @param csr     Certificate Signing Request {.cer} file. Must not be {@code null}.
     * @return The newly created Certificate.
     *
     * @throws CAOfflineException if External CA that is offline
     * @throws CmpClientException if client error occurs.
     */
    Certificate createCertRequest(String caName, String profile, CSRMeta csrMeta, Certificate csr)
        throws CAOfflineException, CmpClientException;

    /**
     * Requests to Revoke a Certificate. If the certificate is deemed to be no longer trustable prior to its expiration
     * date, it can be revoked by the issuing Certificate Authority (CA). Methods of revocation  to be used, Certificate
     * Revocation List (CRL) Or Online Certificate Status Protocol (OCSP) responses.
     *
     * @param caName         CA name. Could be {@code null}.
     * @param cert           Target certificate. Must not be {@code null}.
     * @param reason         Revocation reason.
     * @param invalidityTime Invalidity time. Could be {@code null}.
     * @return return Certificate.
     *
     * @throws CmpClientException if client error occurs.
     */
    Certificate revokeCertRequest(String caName, Certificate cert, int reason, Date invalidityTime)
        throws CAOfflineException, CmpClientException;
}
