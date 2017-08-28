/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import com.att.dao.aaf.cass.NsDAO;
import com.googlecode.jcsv.writer.CSVEntryConverter;

public class NsEntryConverter extends AafEntryConverter implements CSVEntryConverter<NsDAO.Data> {

	@Override
	public String[] convertEntry(NsDAO.Data nsd) {
		String[] columns = new String[5];
		
		columns[0] = nsd.name;
		// JG changed from "scope" to "type"
		columns[1] = String.valueOf(nsd.type);
		//TODO Chris: need to look at this 
//		columns[2] = formatSet(nsd.admin);
//		columns[3] = formatSet(nsd.responsible);
//		columns[4] = nsd.description==null?"":nsd.description;
		columns[5] = nsd.description==null?"":nsd.description;
		
		return columns;
	}

}
