/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.CredDAO;
import com.att.inno.env.util.Chrono;

public class CredPrint implements Action<CredDAO.Data,Void> {
	private String text;

	public CredPrint(String text) {
		this.text = text;
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, CredDAO.Data cred) {
		trans.info().log(text,cred.id,type(cred.type),Chrono.dateOnlyStamp(cred.expires));
		return Result.ok();
	}
	
	
	public static String type(int type) {
		switch(type) {
			case CredDAO.BASIC_AUTH: // 1
					return "OLD";
			case CredDAO.BASIC_AUTH_SHA256: // 2 
					return "U/P"; 
			case CredDAO.CERT_SHA256_RSA: // 200
					return "Cert"; 
			default: 
				return "Unknown";
		}
	}

}