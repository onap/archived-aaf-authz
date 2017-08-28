/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import static com.att.xgen.html.HTMLGen.A;

import com.att.xgen.html.HTMLGen;

public class TextAndRefCell extends RefCell {

	private String text;
		
	public TextAndRefCell(String text, String name, String href, String[] attributes) {
		super(name, href, attributes);
		this.text = text;
	}

	@Override
	public void write(HTMLGen hgen) {
		hgen.text(text);
		hgen.leaf(A,"href="+href).text(name);
	}

}
