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

import org.onap.aaf.auth.dao.cass.NsDAO;

import com.googlecode.jcsv.writer.CSVEntryConverter;

public class NsEntryConverter extends AafEntryConverter implements CSVEntryConverter<NsDAO.Data> {

	@Override
	public String[] convertEntry(NsDAO.Data nsd) {
		String[] columns = new String[5];
		
		columns[0] = nsd.name;
		// Jonathan changed from "scope" to "type"
		columns[1] = String.valueOf(nsd.type);
		//TODO Chris: need to look at this 
//		columns[2] = formatSet(nsd.admin);
//		columns[3] = formatSet(nsd.responsible);
//		columns[4] = nsd.description==null?"":nsd.description;
		columns[5] = nsd.description==null?"":nsd.description;
		
		return columns;
	}

}
