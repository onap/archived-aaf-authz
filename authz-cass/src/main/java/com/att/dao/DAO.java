/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.dao;

import com.att.authz.layer.Result;
import com.att.inno.env.Trans;


/**
 * DataAccessObject Interface
 *
 * Extend the ReadOnly form (for Get), and add manipulation methods
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
