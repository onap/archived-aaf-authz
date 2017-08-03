/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;

public interface Action<T,RV> {
	public Result<RV> exec(AuthzTrans trans, T ur);
}