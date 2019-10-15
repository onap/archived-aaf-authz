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

package org.onap.aaf.auth.dao.cached;

import java.util.List;

import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.CachedDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

public class CachedCredDAO extends CachedDAO<AuthzTrans, CredDAO, CredDAO.Data> {
    private final ReadID readID;
    private final ReadID readIDBath;

    public CachedCredDAO(CredDAO dao, CIDAO<AuthzTrans> info, long expiresIn) {
        super(dao, info, CredDAO.CACHE_SEG, expiresIn);
        if(FileGetter.isLoaded) {
            readID = new ReadID() {
                @Override
                public Result<List<Data>> read(AuthzTrans trans, final String id) {
                    return FileGetter.singleton(null).getter(id).get();
                }
            };
            // Both are the same... File read in only does BAth
            readIDBath = readID;
        } else {
            readID = new ReadID() {
                @Override
                public Result<List<Data>> read(AuthzTrans trans, final String id) {
                    DAOGetter getter = new DAOGetter(trans,dao()) {
                        @Override
                        public Result<List<CredDAO.Data>> call() {
                            return dao().readID(trans, id);
                        }
                    };
                
                    Result<List<CredDAO.Data>> lurd = get(trans, id, getter);
                    if (lurd.isOK() && lurd.isEmpty()) {
                        return Result.err(Status.ERR_UserNotFound,"No User Cred found");
                    }
                    return lurd;
                }
            };
        
            readIDBath = new ReadID() {
                @Override
                public Result<List<Data>> read(AuthzTrans trans, final String id) {
                     DAOGetter getter = new DAOGetter(trans,dao()) {
                         @Override
                         public Result<List<CredDAO.Data>> call() {
                             return dao().readIDBAth(trans, id);
                         }
                     };
                 
                     Result<List<CredDAO.Data>> lurd = get(trans, id, getter);
                     if (lurd.isOK() && lurd.isEmpty()) {
                         return Result.err(Status.ERR_UserNotFound,"No User Cred found");
                     }
                     return lurd;
                }
            };
        }
    }

    /**
     * Pass through Cred Lookup
     *
     * Unlike Role and Perm, we don't need or want to cache these elements... Only used for NS Delete.
     *
     * @param trans
     * @param ns
     * @return
     */
    public Result<List<CredDAO.Data>> readNS(AuthzTrans trans, final String ns) {
    
        return dao().readNS(trans, ns);
    }

    public Result<List<CredDAO.Data>> readID(AuthzTrans trans, final String id) {
        return readID.read(trans, id);
    }

    public Result<List<Data>> readIDBAth(AuthzTrans trans, String id) {
        return readIDBath.read(trans,id);
    }

    @FunctionalInterface
    private interface ReadID {
        public Result<List<CredDAO.Data>> read(final AuthzTrans trans, final String id);
    }
}
