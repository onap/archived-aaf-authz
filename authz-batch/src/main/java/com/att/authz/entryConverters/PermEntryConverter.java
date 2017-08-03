/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import com.att.dao.aaf.cass.PermDAO;
import com.googlecode.jcsv.writer.CSVEntryConverter;

public class PermEntryConverter extends AafEntryConverter implements CSVEntryConverter<PermDAO.Data>  {

		@Override
		public String[] convertEntry(PermDAO.Data pd) {
			String[] columns = new String[6];
			
			columns[0] = pd.ns;
			columns[1] = pd.type;
			columns[2] = pd.instance;
			columns[3] = pd.action;
			columns[4] = formatSet(pd.roles);
			columns[5] = pd.description==null?"":pd.description;
			
			return columns;
		}
}
