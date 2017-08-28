/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Future;
import com.att.authz.layer.Result;
import org.onap.aaf.inno.env.util.Chrono;

public class FAPrint implements Action<Future,Void> {
	private String text;

	public FAPrint(String text) {
		this.text = text;
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, Future f) {
		trans.info().log(text,f.id,f.memo,"expiring on",Chrono.dateOnlyStamp(f.expires));
		return Result.ok();
	}
}