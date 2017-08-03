/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.inno.env.util.Chrono;

public class URPrint implements Action<UserRole,Void> {
	private String text;

	public URPrint(String text) {
		this.text = text;
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, UserRole ur) {
		trans.info().log(text,ur.user,"to",ur.role,"expiring on",Chrono.dateOnlyStamp(ur.expires));
		return Result.ok();
	}

}