/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.PrintStream;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization;

public class EmailPrint extends Email {

	public EmailPrint(String... defaultCC) {
		super(defaultCC);
	}

	/* (non-Javadoc)
	 * @see com.att.authz.actions.Email#exec(com.att.authz.org.Organization, java.lang.StringBuilder)
	 */
	@Override
	protected Result<Void> exec(AuthzTrans trans, Organization org, StringBuilder msg) {
		PrintStream out = System.out;
		boolean first = true;
		out.print("To: ");
		for(String s: toList) {
			if(first) {first = false;}
			else {out.print(',');}
			out.print(s);
		}
		out.println();
		
		first = true;
		out.print("CC: ");
		for(String s: ccList) {
			if(first) {first = false;}
			else {out.print(',');}
			out.print(s);
		}
		out.println();

		out.print("Subject: ");
		out.println(subject);
		out.println();
		
		out.println(msg);
		return Result.ok();

	}

}
