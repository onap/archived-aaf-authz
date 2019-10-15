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

package org.onap.aaf.auth.dao;

import java.util.List;

import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.Trans;

/**
 * DataAccessObject - ReadOnly
 * <p>
 * It is useful to have a ReadOnly part of the interface for CachedDAO
 * <p>
 * Normal DAOs will implement full DAO
 * <p>
 * @author Jonathan
 *
 * @param <DATA>
 */
public interface DAO_RO<TRANS extends Trans,DATA> {
    /**
     * Get a List of Data given Key of Object Array
     * @param objs
     * @return
     * @throws DAOException
     */
    public Result<List<DATA>> read(TRANS trans, Object ... key);

    /**
     * Get a List of Data given Key of DATA Object
     * @param trans
     * @param key
     * @return
     * @throws DAOException
     */
    public Result<List<DATA>> read(TRANS trans, DATA key);

    /**
     * close DAO
     */
    public void close(TRANS trans);

    /**
     * Return name of referenced Data
     * @return
     */
    public String table();


}
