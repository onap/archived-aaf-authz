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

import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.Trans;


/**
 * DataAccessObject Interface
 *
 * Extend the ReadOnly form (for Get), and add manipulation methods
 *
 * @author Jonathan
 *
 * @param <DATA>
 */
public interface DAO<TRANS extends Trans,DATA> extends DAO_RO<TRANS,DATA> {
    public Result<DATA> create(TRANS trans, DATA data);
    public Result<Void> update(TRANS trans, DATA data);
    // In many cases, the data has been correctly read first, so we shouldn't read again
    // Use reread=true if you are using DATA with only a Key
    public Result<Void> delete(TRANS trans, DATA data, boolean reread);
    public Object[] keyFrom(DATA data);
}
