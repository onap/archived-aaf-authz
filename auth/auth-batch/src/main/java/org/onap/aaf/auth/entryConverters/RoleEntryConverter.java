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

package org.onap.aaf.auth.entryConverters;

import org.onap.aaf.auth.dao.cass.RoleDAO;

import com.googlecode.jcsv.writer.CSVEntryConverter;

public class RoleEntryConverter extends AafEntryConverter implements CSVEntryConverter<RoleDAO.Data>  {

	@Override
	public String[] convertEntry(RoleDAO.Data rd) {
		String[] columns = new String[4];
		
		columns[0] = rd.ns;
		columns[1] = rd.name;
		columns[2] = formatSet(rd.perms);
		columns[3] = rd.description==null?"":rd.description;
		
		return columns;
	}

}
