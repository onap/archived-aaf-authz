/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import com.att.xgen.html.HTMLGen;

public class ButtonCell extends AbsCell {
	private String[] attrs;
	
	public ButtonCell(String value, String ... attributes) {
		attrs = new String[2+attributes.length];
		attrs[0]="type=button";
		attrs[1]="value="+value;
		System.arraycopy(attributes, 0, attrs, 2, attributes.length);
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
