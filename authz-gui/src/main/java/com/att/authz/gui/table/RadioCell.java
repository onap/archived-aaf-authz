/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import com.att.xgen.html.HTMLGen;

public class RadioCell extends AbsCell {
	private String[] attrs;
	
	public RadioCell(String name, String radioClass, String value, String ... attributes) {
		attrs = new String[4+attributes.length];
		attrs[0]="type=radio";
		attrs[1]="name="+name;
		attrs[2]="class="+radioClass;
		attrs[3]="value="+value;
		System.arraycopy(attributes, 0, attrs, 4, attributes.length);
	}
	
	@Override
	public void write(HTMLGen hgen) {
		hgen.incr("input",true,attrs).end();
	}

	@Override
	public String[] attrs() {
		return AbsCell.CENTER;
	}
}
