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
package org.onap.aaf.auth.cm.ca;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.onap.aaf.auth.env.NullTrans;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.cadi.configure.Factory;


/**
 * Have to put the Cert and resulting Trust Chain together. 
 * Treating them separately has caused issues
 * <p>
 * @author JonathanGathman
 *
 */
public class X509andChain {
    protected X509Certificate cert;
    protected String[] trustChain;

    public X509andChain() {
        cert = null;
        trustChain = null;
    }

    public X509andChain(X509Certificate cert, String[] tc) {
        this.cert = cert;
        trustChain=tc;
    }

    public X509andChain(X509Certificate cert, List<String> chain) {
        this.cert = cert;
        trustChain = new String[chain.size()+1];
        chain.toArray(trustChain);
    }


    public void addTrustChainEntry(X509Certificate x509) throws IOException, CertException {
        if (trustChain==null) {
            trustChain = new String[] {Factory.toString(NullTrans.singleton(),x509)};
        } else {
            String[] temp = new String[trustChain.length+1];
            System.arraycopy(trustChain, 0, temp, 0, trustChain.length);
            temp[trustChain.length]=Factory.toString(NullTrans.singleton(),x509);
            trustChain=temp;
        }
    }


    public X509Certificate getX509() {
        return cert;
    }

    public String[] getTrustChain() {
        return trustChain;
    }

}
