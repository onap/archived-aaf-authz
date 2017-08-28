/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.GregorianCalendar;

import com.att.authz.env.AuthzTrans;
import org.onap.aaf.inno.env.APIException;
import com.datastax.driver.core.Cluster;

public abstract class ActionPuntDAO<T, RV> extends ActionDAO<T, RV> {
	private static final SecureRandom random = new SecureRandom();
	private int months, range;
	protected static final Date now = new Date();

	public ActionPuntDAO(AuthzTrans trans, Cluster cluster, int months, int range) throws APIException, IOException {
		super(trans, cluster);
		this.months = months;
		this.range = range;
	}

	public ActionPuntDAO(AuthzTrans trans, ActionDAO<?, ?> predecessor, int months, int range) {
		super(trans, predecessor);
		this.months = months;
		this.range = range;
	}
	

	protected Date puntDate() {
		GregorianCalendar temp = new GregorianCalendar();
		temp.setTime(now);
		if(range>0) {
			int forward = months+Math.abs(random.nextInt()%range);
			temp.add(GregorianCalendar.MONTH, forward);
			temp.add(GregorianCalendar.DAY_OF_MONTH, (random.nextInt()%30)-15);
		}
		return temp.getTime();
		
	}

}
