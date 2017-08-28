/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import java.io.IOException;

import org.onap.aaf.inno.env.APIException;
import com.att.xgen.Cache;
import com.att.xgen.html.HTMLGen;

public class Form extends NamedCode {
	private String preamble;
	private NamedCode content;
	
	public Form(boolean no_cache, NamedCode content) {
		super(no_cache,content.idattrs());
		this.content = content;
		preamble=null;
		idattrs = content.idattrs();
	}
	
	public Form preamble(String preamble) {
		this.preamble = preamble;
		return this;
	}
	

	@Override
	public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
		if(preamble!=null) {
			hgen.incr("p","class=preamble").text(preamble).end();
		}
		hgen.incr("form","method=post");
	
		content.code(cache, hgen);
		
		hgen.tagOnly("input", "type=submit", "value=Submit")
			.tagOnly("input", "type=reset", "value=Reset")
		.end();
	}

	/* (non-Javadoc)
	 * @see com.att.authz.gui.NamedCode#idattrs()
	 */
	@Override
	public String[] idattrs() {
		return content.idattrs();
	}

}
