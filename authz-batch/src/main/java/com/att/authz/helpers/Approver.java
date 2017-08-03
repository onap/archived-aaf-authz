/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.HashMap;
import java.util.Map;

import com.att.authz.actions.Message;
import com.att.authz.org.Organization;

public class Approver {
	public String name;
	public Organization org;
	public Map<String, Integer> userRequests;
	
	public Approver(String approver, Organization org) {
		this.name = approver;
		this.org = org;
		userRequests = new HashMap<String, Integer>();
	}
	
	public void addRequest(String user) {
		if (userRequests.get(user) == null) {
		    userRequests.put(user, 1);
		} else {
			Integer curCount = userRequests.remove(user);
			userRequests.put(user, curCount+1);
		}
	}
	
	/**
	 * @param sb
	 * @return
	 */
	public void build(Message msg) {
		msg.clear();
		msg.line("You have %d total pending approvals from the following users:", userRequests.size());
		for (Map.Entry<String, Integer> entry : userRequests.entrySet()) {
			msg.line("  %s (%d)",entry.getKey(),entry.getValue());
		}
	}

}
