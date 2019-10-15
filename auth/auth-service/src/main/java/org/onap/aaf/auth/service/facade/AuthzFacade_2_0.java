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

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.service.AuthzService;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;

import aaf.v2_0.Approvals;
import aaf.v2_0.Certs;
import aaf.v2_0.Delgs;
import aaf.v2_0.Error;
import aaf.v2_0.History;
import aaf.v2_0.Keys;
import aaf.v2_0.Nss;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Request;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;

public class AuthzFacade_2_0 extends AuthzFacadeImpl<
    Nss,
    Perms,
    Pkey,
    Roles,
    Users,
    UserRoles,
    Delgs,
    Certs,
    Keys,
    Request,
    History,
    Error,
    Approvals>
{
    public AuthzFacade_2_0(AuthzEnv env,
            AuthzService<Nss, Perms, Pkey, Roles, Users, UserRoles, Delgs, Certs, Keys, Request, History, Error, Approvals> service,
            Data.TYPE type) throws APIException {
        super(env, service, type);
    }
}
