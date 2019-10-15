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

package org.onap.aaf.auth.service.facade;

import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.service.AuthzCassServiceImpl;
import org.onap.aaf.auth.service.mapper.Mapper_2_0;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;


public class AuthzFacadeFactory {
    public static AuthzFacade_2_0 v2_0(AuthzEnv env, AuthzTrans trans, Data.TYPE type, Question question) throws APIException {
        return new AuthzFacade_2_0(env,
                new AuthzCassServiceImpl<
                    aaf.v2_0.Nss,
                    aaf.v2_0.Perms,
                    aaf.v2_0.Pkey,
                    aaf.v2_0.Roles,
                    aaf.v2_0.Users,
                    aaf.v2_0.UserRoles,
                    aaf.v2_0.Delgs,
                    aaf.v2_0.Certs,
                    aaf.v2_0.Keys,
                    aaf.v2_0.Request,
                    aaf.v2_0.History,
                    aaf.v2_0.Error,
                    aaf.v2_0.Approvals>
                    (trans,new Mapper_2_0(question),question),
                type);
    }


}
