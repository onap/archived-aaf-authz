/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import com.att.xgen.html.HTMLGen;

/**
 * Write Simple Text into a Cell
 *
 */
public class TextCell extends AbsCell {
	public final String name;
	private String[] attrs;
	
	public TextCell(String name, String... attributes) {
		attrs = new String[attributes.length];
		System.arraycopy(attributes, 0, attrs, 0, attributes.length);
		this.name = name;
	}
	
	@Override
	public void write(HTMLGen hgen) {
		hgen.text(name);
	}
	
	@Override
	public String[] attrs() {
		return attrs;
	}
}
