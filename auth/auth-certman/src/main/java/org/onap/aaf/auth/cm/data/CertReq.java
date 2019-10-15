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

package org.onap.aaf.auth.cm.data;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.cert.BCFactory;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.configure.CertException;

public class CertReq {
    // These cannot be null
    public CA certAuthority;
    public String mechid;
    public List<String> fqdns;
    // Notify
    public List<String> emails;


    // These may be null
    public String sponsor;
    public XMLGregorianCalendar start;
    public XMLGregorianCalendar end;

    public CSRMeta getCSRMeta() throws CertException {
        return BCFactory.createCSRMeta(certAuthority, mechid, sponsor,fqdns);
    }
}
