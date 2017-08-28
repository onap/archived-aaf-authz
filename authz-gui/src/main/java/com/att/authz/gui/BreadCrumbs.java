/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import static com.att.xgen.html.HTMLGen.A;
import static com.att.xgen.html.HTMLGen.LI;
import static com.att.xgen.html.HTMLGen.UL;

import java.io.IOException;

import org.onap.aaf.inno.env.APIException;
import com.att.xgen.Cache;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

public class BreadCrumbs extends NamedCode {
	private Page[] breadcrumbs;

	public BreadCrumbs(Page ... pages) {
		super(false,"breadcrumbs");
		breadcrumbs = pages;
	}
	
	@Override
	public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
		// BreadCrumbs
		Mark mark = new Mark();
		hgen.incr(mark, UL);
		for(Page p : breadcrumbs) {
			hgen.incr(LI,true)
				.leaf(A,"href="+p.url()).text(p.name())
				.end(2);
		}
		hgen.end(mark);
	}
}
