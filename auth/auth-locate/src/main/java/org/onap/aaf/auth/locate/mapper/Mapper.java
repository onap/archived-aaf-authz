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

package org.onap.aaf.auth.locate.mapper;

import java.util.List;

import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.layer.Result;

import locate.v1_0.MgmtEndpoint;

public interface Mapper<IN,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIG,ERROR>
{
    public enum API{IN_REQ,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIG,ERROR,VOID};
    public Class<?> getClass(API api);
    public<A> A newInstance(API api);

    public ERROR errorFromMessage(StringBuilder holder, String msgID, String text, String... detail);
    public Result<ENDPOINTS> endpoints(Result<List<Data>> resultDB, String version, String other);
    public Data locateData(MgmtEndpoint me);

}
