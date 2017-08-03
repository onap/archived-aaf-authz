/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.entryConverters;

import java.util.Set;

public abstract class AafEntryConverter {

	protected String formatSet(Set<String> set) {
		if (set==null || set.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		int curr = 0;
		sb.append("{");
		for (String s : set) {
			sb.append("'");
			sb.append(s);
			sb.append("'");
			if (set.size() != curr + 1) {
				sb.append(",");
			}
			curr++;
		}
		sb.append("}");
		return sb.toString();
	}

}
