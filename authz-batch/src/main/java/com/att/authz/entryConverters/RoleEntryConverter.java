/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import com.att.dao.aaf.cass.RoleDAO;
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
