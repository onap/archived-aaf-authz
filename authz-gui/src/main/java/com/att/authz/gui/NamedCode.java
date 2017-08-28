/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import com.att.xgen.Code;
import com.att.xgen.html.HTMLGen;



public abstract class NamedCode implements Code<HTMLGen> {
	public final boolean no_cache;
	protected String[] idattrs;
	
	/*
	 *  Mark whether this code should not be cached, and any attributes 
	 */
	public NamedCode(final boolean no_cache, String ... idattrs) {
		this.idattrs = idattrs;
		this.no_cache = no_cache;
	}
	
	/**
	 * Return ID and Any Attributes needed to create a "div" section of this code
	 * @return
	 */
	public String[] idattrs() {
		return idattrs;
	}

}
