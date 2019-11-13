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

package org.onap.aaf.auth.cm.cmpv2client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.onap.aaf.auth.cm.cmpv2client.impl.CAOfflineException;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmpSendHttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmpSendHttpRequest.class);
    private static final String CONTENT_TYPE = "Content-type";
    private static final String CMP_REQUEST_MIMETYPE = "application/pkixcmp";

    private CmpSendHttpRequest() {
    }

    /**
     * Sends Post Request to External CA based upon data in PKIMessage and url_string.
     *
     * @param pkiMessage contains all relevant data needed to generate certificate
     * @param urlString  url containing the full path to the External CA
     * @return PKIMessage containing response from External CA server
     * @throws CmpClientException based upon possible IOException while writing to and from CA server
     * @throws CAOfflineException based upon being unable to connect to the external CA server
     */
    public static PKIMessage cmpSendHttpPostRequest(final PKIMessage pkiMessage, final String urlString)
        throws CmpClientException, CAOfflineException {
        final ByteArrayOutputStream byteArrOutputStream = new ByteArrayOutputStream();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPost postRequest = new HttpPost(urlString);
            final byte[] requestBytes = pkiMessage.getEncoded();

            postRequest.setEntity(new ByteArrayEntity(requestBytes));
            postRequest.setHeader(CONTENT_TYPE, CMP_REQUEST_MIMETYPE);

            try (CloseableHttpResponse response = httpclient.execute(postRequest)) {
                response.getEntity().writeTo(byteArrOutputStream);
            }
        } catch (ConnectException ce) {
            CAOfflineException caoe = new CAOfflineException(ce);
            LOGGER.error("CmpClient is unable to connect to external CA");
            throw caoe;
        } catch (IOException ioe) {
            CmpClientException cmpClientException = new CmpClientException("IOException occurred "
                + "while trying to Send Post Request", ioe);
            LOGGER.error("IOException occurred in CmpClient");
            throw cmpClientException;
        }
        return PKIMessage.getInstance(byteArrOutputStream.toByteArray());
    }
}