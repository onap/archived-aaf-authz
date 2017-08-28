/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.att.dao.aaf.cass.UserRoleDAO;
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
