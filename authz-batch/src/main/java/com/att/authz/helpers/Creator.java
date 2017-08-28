/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import com.datastax.driver.core.Row;

public abstract class Creator<T> {
	public abstract T create(Row row);
	public abstract String select();
	
	public String query(String where) {
		StringBuilder sb = new StringBuilder(select());
		if(where!=null) {
			sb.append(" WHERE ");
			sb.append(where);
		}
		sb.append(';');
		return sb.toString();
	}


}