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
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import javax.management.RuntimeErrorException;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cmpv2client.CertificateRequestMessageGenerator;
import org.onap.aaf.auth.cm.cmpv2client.CmpSendHttpRequest;
import org.onap.aaf.auth.cm.cmpv2client.CmpUtil;
import org.onap.aaf.auth.cm.cmpv2client.api.CmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the CmpClient Interface conforming to RFC4210 (Certificate Management Protocol (CMP)) and RFC4211
 * (Certificate Request Message Format (CRMF)) standards.
 */
public final class CmpClientImpl implements CmpClient {

    private final Logger LOGGER = LoggerFactory.getLogger(CmpClientImpl.class);

    private String CA_Name;
    private String CA_Profile;
    private static final String DEFAULT_PROFILE = "RA";
    private static final String DEFAULT_CA_NAME = "Certification Authority";

    @Override
    public Certificate createCertRequest(final String caName, final String profile, final CSRMeta csrMeta,
        final Certificate csr, final Optional<Date> notBefore, final Optional<Date> notAfter)
        throws CAOfflineException, CmpClientException {
        validationCheck(csrMeta, csr, caName, profile, notBefore, notAfter);

        CertificateRequestMessageGenerator crmg =
            new CertificateRequestMessageGenerator.CertificateRequestMessageGeneratorBuilder(csrMeta.issuerx500Name(),
                csrMeta.x500Name(), csrMeta.keyPair(), csrMeta.password())
                .setNotBefore(notBefore).setNotAfter(notAfter).setSansList(csrMeta.sans())
                .build();
        PKIMessage protectedCmpCertificateRequest = crmg.generateCertificateRequestMessage();

        PKIMessage responseMessage = CmpSendHttpRequest.cmpSendHttpPostRequest(protectedCmpCertificateRequest,
            csrMeta.externalCaUrl());
        //todo: add response validation and return Certificate
        return null;
    }

    @Override
    public Certificate createCertRequest(final String caName, String profile,
        final CSRMeta csrMeta, final Certificate csr)
        throws CAOfflineException, CmpClientException {
        return createCertRequest(caName, profile, csrMeta, csr, null, null);
    }

    @Override
    public Certificate revokeCertRequest(String caName, Certificate cert, int reason,
        Date invalidityTime) throws CAOfflineException, CmpClientException {
        //TODO
        return null;
    }

    /**
     * Checks whether all the needed values for creating a certificate request message exist and are correct.
     *
     * @param csrMeta CSRMeta object containing all the neccessary variables for creating a certificate request
     * @param cert    Certificate object needed to validate the response from the external CA server
     * @param caName  Date specifying certificate is not valid before this date
     * @param profile Date specifying certificate is not valid after this date
     */
    private void validationCheck(final CSRMeta csrMeta, final Certificate cert, final String caName,
        final String profile, final Optional<Date> notBefore, final Optional<Date> notAfter) throws CmpClientException {
        CA_Name = CmpUtil.isNullOrEmpty(caName) ? caName : DEFAULT_CA_NAME;
        CA_Profile = CmpUtil.isNullOrEmpty(profile) ? profile : DEFAULT_PROFILE;

        LOGGER.info("Checking values to Generate Certificate Request for External CA :{} in Mode {} ", CA_Name,
            CA_Profile);

        CmpUtil.notNull(csrMeta, "CSRMeta Instance");
        CmpUtil.notNull(csrMeta.x500Name(), "Subject DN");
        CmpUtil.notNull(csrMeta.issuerx500Name(), "Issuer DN");
        CmpUtil.notNull(csrMeta.password(), "IAK/RV Password");
        CmpUtil.notNull(cert, "Certificate Signing Request (CSR)");
        CmpUtil.notNull(csrMeta.externalCaUrl(), "External CA URL");
        CmpUtil.notNull(csrMeta.keypair(null), "Subject KeyPair");
        if (notBefore.isPresent() && notAfter.isPresent() && notBefore.get().compareTo(notAfter.get()) > 0) {
            throw new CmpClientException("Before Date is set after the After Date");
        }
    }
}

