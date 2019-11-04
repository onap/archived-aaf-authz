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

package org.onap.aaf.auth.dao.cached;

import java.util.List;

import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.CachedDAO;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.RoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

public class CachedRoleDAO extends CachedDAO<AuthzTrans,RoleDAO, RoleDAO.Data> {
    public CachedRoleDAO(RoleDAO dao, CIDAO<AuthzTrans> info, long expiresIn) {
        super(dao, info, RoleDAO.CACHE_SEG, expiresIn);
    }

    public Result<List<Data>> readNS(AuthzTrans trans, final String ns) {
        DAOGetter getter = new DAOGetter(trans,dao()) {
        	@Override
            public Result<List<Data>> call() {
                return dao.readNS(trans, ns);
            }
        };

        Result<List<Data>> lurd = get(trans, ns, getter);
        if (lurd.isOK() && lurd.isEmpty()) {
            return Result.err(Status.ERR_RoleNotFound,"No Role found");
        }
        return lurd;
    }

    public Result<List<Data>> readName(AuthzTrans trans, final String name) {
        DAOGetter getter = new DAOGetter(trans,dao()) {
        	@Override
            public Result<List<Data>> call() {
                return dao().readName(trans, name);
            }
        };

        Result<List<Data>> lurd = get(trans, name, getter);
        if (lurd.isOK() && lurd.isEmpty()) {
            return Result.err(Status.ERR_RoleNotFound,"No Role found");
        }
        return lurd;
    }

    public Result<List<Data>> readChildren(AuthzTrans trans, final String ns, final String name) {
        // At this point, I'm thinking it's better not to try to cache "*" results
        // Data probably won't be accurate, and adding it makes every update invalidate most of the cache
        // Jonathan 2/4/2014
        return dao().readChildren(trans,ns,name);
    }

    public Result<Void> addPerm(AuthzTrans trans, RoleDAO.Data rd, PermDAO.Data perm) {
        Result<Void> rv = dao().addPerm(trans,rd,perm);
        if (trans.debug().isLoggable())
            trans.debug().log("Adding",perm,"to", rd, "with CachedRoleDAO.addPerm");
        invalidate(trans, rd);
        return rv;
    }

    public Result<Void> delPerm(AuthzTrans trans, RoleDAO.Data rd, PermDAO.Data perm) {
        Result<Void> rv = dao().delPerm(trans,rd,perm);
        if (trans.debug().isLoggable())
            trans.debug().log("Removing",perm,"from", rd, "with CachedRoleDAO.addPerm");
        invalidate(trans, rd);
        return rv;
    }

    /**
     * Add description to this role
     *
     * @param trans
     * @param ns
     * @param name
     * @param description
     * @return
     */
    public Result<Void> addDescription(AuthzTrans trans, String ns, String name, String description) {
        //TODO Invalidate?
        return dao().addDescription(trans, ns, name, description);

    }

}
