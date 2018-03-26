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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.onap.aaf.auth.dao.cass.CredDAO;

import com.datastax.driver.core.utils.Bytes;
import com.googlecode.jcsv.writer.CSVEntryConverter;

public class CredEntryConverter extends AafEntryConverter implements CSVEntryConverter<CredDAO.Data> {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
	
	@Override
	public String[] convertEntry(CredDAO.Data cd) {
		String[] columns = new String[5];
		
		columns[0] = cd.id;
		columns[1] = String.valueOf(cd.type);
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		columns[2] = df.format(cd.expires);
		columns[3] = Bytes.toHexString(cd.cred);
		columns[4] = (cd.ns==null)?"":cd.ns;
		
		return columns;
	}
}
