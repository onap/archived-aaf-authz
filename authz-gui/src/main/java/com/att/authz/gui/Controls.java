/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import java.io.IOException;

import org.onap.aaf.inno.env.APIException;
import com.att.xgen.Cache;
import com.att.xgen.html.HTMLGen;

public class Controls extends NamedCode {
	public Controls() {
		super(false,"controls");
	}
	
	@Override
	public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
		hgen.incr("form","method=post")
			.incr("input", true, "type=checkbox", "name=vehicle", "value=Bike").text("I have a bike").end()
			.text("Password: ")
			.incr("input", true, "type=password", "id=password1").end()
			.tagOnly("input", "type=submit", "value=Submit")
			.end();
	}

}
