/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import static com.att.xgen.html.HTMLGen.TABLE;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Slot;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

public class PassChangeForm extends Page {
	// Package on purpose
	static final String HREF = "/gui/passwd";
	static final String NAME = "PassChange";
	static final String fields[] = {"id","current","password","password2","startDate"};
	
	public PassChangeForm(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, fields,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {
			private final Slot sID = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[0]);
			@Override
			public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
				// p tags not closing right using .p() - causes issues in IE8 password form - so using leaf for the moment
				hgen.leaf("p").text("You are requesting a new Mechanical Password in the AAF System.  " +
				     "So that you can perform clean migrations, you will be able to use both this " +
				     "new password and the old one until their respective expiration dates.").end()
				     .leaf("p").text("Note: You must be a Namespace Admin where the MechID resides.").end()
					.incr("form","method=post");
				Mark table = new Mark(TABLE);
				hgen.incr(table);
				cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
					@Override
					public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
//						GregorianCalendar gc = new GregorianCalendar();
//						System.out.println(gc.toString());
						String incomingID= trans.get(sID, "");
						hgen
						.input(fields[0],"ID*",true,"value="+incomingID)
						.input(fields[1],"Current Password*",true,"type=password")
						.input(fields[2],"New Password*",true, "type=password")
						.input(fields[3], "Reenter New Password*",true, "type=password")
//						.input(fields[3],"Start Date",false,"type=date", "value="+
//								Chrono.dateOnlyFmt.format(new Date(System.currentTimeMillis()))
//								)
						.end();
					}
				});
				hgen.end();
				hgen.tagOnly("input", "type=submit", "value=Submit")
				.end();

			}
		});
	}

}
