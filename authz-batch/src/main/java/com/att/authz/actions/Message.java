/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.util.ArrayList;
import java.util.List;

public class Message {
	public final List<String> lines;
		
	public Message() {
		lines = new ArrayList<String>();
	}

	public void clear() {
		lines.clear();
	}
	
	public void line(String format, Object ... args) {
		lines.add(String.format(format, args));
	}

	public void msg(StringBuilder sb, String lineIndent) {
		if(lines.size()>0) {
			for(String line : lines) {
				sb.append(lineIndent);
				sb.append(line);
				sb.append('\n');
			}
		}
	}
}
