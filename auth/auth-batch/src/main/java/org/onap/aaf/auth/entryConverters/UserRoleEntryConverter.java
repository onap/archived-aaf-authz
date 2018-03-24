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

import org.onap.aaf.auth.dao.cass.UserRoleDAO;

import com.googlecode.jcsv.writer.CSVEntryConverter;

public class UserRoleEntryConverter extends AafEntryConverter implements CSVEntryConverter<UserRoleDAO.Data> {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
	
	@Override
	public String[] convertEntry(UserRoleDAO.Data urd) {
		String[] columns = new String[3];
		
		columns[0] = urd.user;
		columns[1] = urd.role;
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		columns[2] = df.format(urd.expires);
		
		return columns;
	}
}
