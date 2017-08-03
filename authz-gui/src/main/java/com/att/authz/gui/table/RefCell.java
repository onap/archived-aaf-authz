/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import static com.att.xgen.html.HTMLGen.A;

import com.att.xgen.html.HTMLGen;

/**
 * Write a Reference Link into a Cell
 *
 */
public class RefCell extends AbsCell {
	public final String name;
	public final String href;
	private String[] attrs;
	
	public RefCell(String name, String href, String... attributes) {
		attrs = new String[attributes.length];
		System.arraycopy(attributes, 0, attrs, 0, attributes.length);
		this.name = name;
		this.href = href;
	}
	
	@Override
	public void write(HTMLGen hgen) {
		hgen.leaf(A,"href="+href).text(name);
	}
	
	@Override
	public String[] attrs() {
		return attrs;
	}
}
