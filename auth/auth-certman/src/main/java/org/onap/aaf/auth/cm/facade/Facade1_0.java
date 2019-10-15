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

package org.onap.aaf.auth.cm.facade;

import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.mapper.Mapper;
import org.onap.aaf.auth.cm.service.CMService;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;

import aaf.v2_0.Error;
import certman.v1_0.Artifacts;
import certman.v1_0.BaseRequest;
import certman.v1_0.CertInfo;

/**
 * @author Jonathan
 *
 */
public class Facade1_0 extends FacadeImpl<BaseRequest,CertInfo, Artifacts, Error> {
    public Facade1_0(AAF_CM certman,
                     CMService service,
                     Mapper<BaseRequest,CertInfo,Artifacts,Error> mapper,
                     Data.TYPE type) throws APIException {
        super(certman, service, mapper, type);
    }
}
