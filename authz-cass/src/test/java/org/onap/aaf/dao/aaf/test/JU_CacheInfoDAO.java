/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.aaf.dao.aaf.test;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.dao.CIDAO;
import org.onap.aaf.dao.DAOException;
import org.onap.aaf.dao.aaf.cass.CacheInfoDAO;
import org.onap.aaf.dao.aaf.cass.RoleDAO;
import org.onap.aaf.dao.aaf.cass.Status;

import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.util.Chrono;

import junit.framework.Assert;


public class JU_CacheInfoDAO extends AbsJUCass {

	@Test
	public void test() throws DAOException, APIException, IOException {
		CIDAO<AuthzTrans> id = new CacheInfoDAO(trans, cluster, AUTHZ);
		Date date  = new Date();
		
		id.touch(trans, RoleDAO.TABLE,1);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		Result<Void> rid = id.check(trans);
		Assert.assertEquals(rid.status,Status.OK);
		Date[] dates = CacheInfoDAO.info.get(RoleDAO.TABLE);
		if(dates.length>0 && dates[1]!=null) {
			System.out.println(Chrono.dateStamp(dates[1]));
			System.out.println(Chrono.dateStamp(date));
			Assert.assertTrue(Math.abs(dates[1].getTime() - date.getTime())<20000); // allow for 4 seconds, given Remote DB
		}
	}

}
