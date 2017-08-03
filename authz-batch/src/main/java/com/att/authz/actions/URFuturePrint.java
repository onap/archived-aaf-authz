/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.util.ArrayList;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization.Identity;
import com.att.inno.env.util.Chrono;


public class URFuturePrint implements  Action<UserRole,List<Identity>> {
	private String text;
	private final static List<Identity> rv = new ArrayList<Identity>();

	public URFuturePrint(String text) {
		this.text = text;
	}

	@Override
	public Result<List<Identity>> exec(AuthzTrans trans, UserRole ur) {
		trans.info().log(text,ur.user,"to",ur.role,"on",Chrono.dateOnlyStamp(ur.expires));
		return Result.ok(rv);
	}}