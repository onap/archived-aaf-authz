/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.table;

import com.att.xgen.html.HTMLGen;

public abstract class AbsCell {
	private static final String[] NONE = new String[0];
	protected static final String[] CENTER = new String[]{"class=center"};

	/**
	 * Write Cell Data with HTMLGen generator
	 * @param hgen
	 */
	public abstract void write(HTMLGen hgen);
	
	public final static AbsCell Null = new AbsCell() {
		@Override
		public void write(final HTMLGen hgen) {
		}
	};
	
	public String[] attrs() {
		return NONE;
	}
}
