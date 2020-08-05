/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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

package org.onap.aaf.auth.cm.util;

import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.layer.Result;

import java.net.InetAddress;

/**
 * A POJO for use in CMService, containing only an InetAddress object
 * and a Result object. This can be safely removed if CMService is ever
 * refactored to throw custom exceptions which are then translated into
 * Results, but for the time being this seemed the most expedient and
 * elegant solution.
 *
 * @author sh265m
 */

public class InetAddressAndResultPOJO {
    private InetAddress inetAddress;
    private Result<CertResp> result;

    public InetAddressAndResultPOJO() {
        // empty constructor
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public Result<CertResp> getResult() {
        return result;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setResult(Result<CertResp> result) {
        this.result = result;
    }
}