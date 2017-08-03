/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.att.dao.aaf.cass.CredDAO;
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
