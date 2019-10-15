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
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

public class CachedCertDAO extends CachedDAO<AuthzTrans, CertDAO, CertDAO.Data> {
    public CachedCertDAO(CertDAO dao, CIDAO<AuthzTrans> info, long expiresIn) {
        super(dao, info, CertDAO.CACHE_SEG, expiresIn);
    }

    /**
     * Pass through Cert ID Lookup
     *
     * @param trans
     * @param ns
     * @return
     */

    public Result<List<CertDAO.Data>> readID(AuthzTrans trans, final String id) {
        return dao().readID(trans, id);
    }

    public Result<List<CertDAO.Data>> readX500(AuthzTrans trans, final String x500) {
        return dao().readX500(trans, x500);
    }


}
