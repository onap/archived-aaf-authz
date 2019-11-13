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
import javax.management.RuntimeErrorException;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cmpv2client.CertificateRequestMessageGenerator;
import org.onap.aaf.auth.cm.cmpv2client.CmpSendHttpRequest;
import org.onap.aaf.auth.cm.cmpv2client.api.CmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the CmpClient Interface conforming to RFC4210 (Certificate Management Protocol (CMP))
 * and RFC4211 (Certificate Request Message Format (CRMF)) standards.
 */
public final class CmpClientImpl implements CmpClient {

    private final Logger LOGGER = LoggerFactory.getLogger(CmpClientImpl.class);

    @Override
    public Certificate createCertRequest(final String caName, final String profile, final CSRMeta csrMeta,
        final Certificate csr, final Date notBefore, final Date notAfter)
        throws CAOfflineException, CmpClientException {

        if(caName != null) {
            LOGGER.info("Information regarding external CA: {} ", caName);
        }
        if(profile != null) {
            LOGGER.info("Certificate request for external CA in {} mode.", profile);
        }

        validationCheck(csrMeta, csr, notBefore, notAfter);
        CertificateRequestMessageGenerator crmg =
            new CertificateRequestMessageGenerator.CertificateRequestMessageGeneratorBuilder(csrMeta.issuerx500Name(),
            csrMeta.x500Name(), csrMeta.keyPair(), csrMeta.password())
            .setNotBefore(notBefore).setNotAfter(notAfter).setSansList(csrMeta.sans())
            .build();
        PKIMessage protectedCmpCertificateRequest = crmg.generateCertificateRequestMessage();

        PKIMessage responseMessage = new CmpSendHttpRequest(HttpClients.createDefault())
            .cmpSendHttpPostRequest(protectedCmpCertificateRequest, csrMeta.externalCaUrl());
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
     * Incorrect values include null values and nonsensical values eg. a before date set after an after date.
     * @param csrMeta CSRMeta object containing all the neccessary variables for creating a certificate request
     * @param cert Certificate object needed to validate the response from the external CA server
     * @param notBefore Date specifying certificate is not valid before this date
     * @param notAfter Date specifying certificate is not valid after this date
     * @throws CmpClientException This exception should only be called if there are any null/bad values.
     */
    private void validationCheck(CSRMeta csrMeta, Certificate cert,
        Date notBefore, Date notAfter)
        throws CmpClientException {
        LOGGER.info("Checking the Values passed through to generate certificate request, confirming values are valid.");


        ArrayList<String> missingValues = new ArrayList<>();
        if (csrMeta == null) {
            throw new CmpClientException("csrMeta object is null, cannot issue certificate request with null csrMeta",
                new RuntimeErrorException(new Error("csrMeta is null")));
        }
        if (csrMeta.x500Name() == null) {
            missingValues.add("subjectDn");
        }
        if (csrMeta.issuerx500Name() == null) {
            missingValues.add("issuerDn");
        }
        if (csrMeta.keypair(null) == null) {
            missingValues.add("subjectKeyPair");
        }
        if (csrMeta.password() == null) {
            missingValues.add("password");
        }
        if (cert == null) {
            missingValues.add("certificate");
        }
        if (csrMeta.externalCaUrl() == null) {
            missingValues.add("externalCaUrl");
        }
        if (notBefore != null && notAfter != null && notBefore.compareTo(notAfter) > 0) {
            missingValues.add(", Before Date is set after the After Date");
        }
        if (!missingValues.isEmpty()) {
            StringBuilder errorString = new StringBuilder(
                "error occurred while building certificate request:"
                    + " the following needed values have been set to null: ");
            for (String s : missingValues) {
                errorString.append(s);
                errorString.append(" ");
            }
            CmpClientException cmpClientException = new CmpClientException(errorString.toString());
            LOGGER.error("null values for non-null variables found");
            throw cmpClientException;
        }
        LOGGER.info("No needed csrMeta values are null, and Date values are valid");
    }
}

