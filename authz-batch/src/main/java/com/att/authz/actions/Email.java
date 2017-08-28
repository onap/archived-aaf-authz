/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization;

public class Email implements Action<Organization,Void>{
	protected final List<String> toList;
	protected final List<String> ccList;
	private final String[] defaultCC;
	protected String subject;
	private String preamble;
	private Message msg;
	private String sig;
	protected String lineIndent="  ";

	
	public Email(String ... defaultCC) {
		toList = new ArrayList<String>();
		this.defaultCC = defaultCC;
		ccList = new ArrayList<String>();
		clear();
	}
	
	public Email clear() {
		toList.clear();
		ccList.clear();
		for(String s: defaultCC) {
			ccList.add(s);
		}
		return this;
	}
	

	public void indent(String indent) {
		lineIndent = indent;
	}
	
	public void preamble(String format, Object ... args) {
		preamble = String.format(format, args);
	}

	public Email addTo(Collection<String> users) {
		toList.addAll(users);
		return this;
	}

	public Email addTo(String email) {
		toList.add(email);
		return this;
	}
	
	
	public Email subject(String format, Object ... args) {
		subject = String.format(format, args);
		return this;
	}
	
	
	public Email signature(String format, Object ... args) {
		sig = String.format(format, args);
		return this;
	}
	
	public void msg(Message msg) {
		this.msg = msg;
	}
	
	@Override
	public Result<Void> exec(AuthzTrans trans, Organization org) {
		StringBuilder sb = new StringBuilder();
		if(preamble!=null) {
			sb.append(lineIndent);
			sb.append(preamble);
			sb.append("\n\n");
		}
		
		if(msg!=null) {
			msg.msg(sb,lineIndent);
			sb.append("\n");
		}

		if(sig!=null) {
			sb.append(sig);
			sb.append("\n");
		}

		return exec(trans,org,sb);
	}

	protected Result<Void> exec(AuthzTrans trans, Organization org, StringBuilder sb) {
		try {
			/* int status = */
			org.sendEmail(trans,
				toList, 
				ccList, 
				subject, 
				sb.toString(), 
				false);
		} catch (Exception e) {
			return Result.err(Result.ERR_ActionNotCompleted,e.getMessage());
		}
		return Result.ok();

	}
}
