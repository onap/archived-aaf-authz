/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.att.authz.Batch;
import com.att.authz.actions.Email;
import com.att.authz.actions.Message;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Approver;
import com.att.authz.helpers.Notification;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization;
import com.att.authz.org.Organization.Identity;
import com.att.authz.org.OrganizationException;
import com.att.authz.org.OrganizationFactory;
import com.att.dao.CassAccess;
import com.att.dao.aaf.cass.ApprovalDAO;
import com.att.dao.aaf.cass.ApprovalDAO.Data;
import org.onap.aaf.inno.env.APIException;

public class ApprNotify extends Batch {
	private final ApprovalDAO apprDAO;
	private Result<List<Data>> rladd;
	private Email email;

	public ApprNotify(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
		apprDAO = new ApprovalDAO(trans, cluster, CassAccess.KEYSPACE);
		session = apprDAO.getSession(trans);
		rladd = apprDAO.readByStatus(trans,"pending");
		if(isDryRun()) {
			email = new Email();//EmailPrint();
		} else {
			email = new Email();
		}
		email.subject("AAF Approval Notification (ENV: %s)",batchEnv);
		email.preamble("AAF is the AT&T System for Fine-Grained Authorizations.  "
				+ "You are being asked to Approve in the %s environment before AAF Actions can be taken. \n\n"
				+ "  Please follow this link:\n\n\t%s/approve"
				,batchEnv,env.getProperty(GUI_URL));

		Notification.load(trans, session, Notification.v2_0_14);
	}
	
	@Override
	protected void run(AuthzTrans trans) {
		if(rladd.isOK()) {
			if(rladd.isEmpty()) {
				trans.warn().log("No Pending Approvals to Process");
			} else {
				Organization org=null;
				//Map<String,Organization> users = new HashMap<String,Organization>();
				Map<String,Approver> users = new TreeMap<String,Approver>();
				
				for(Data data : rladd.value) {
					// We've already seen this approver. Simply add the new request to him.
					try {
						Approver approver = users.get(data.approver);
						if(approver==null) {
							org = OrganizationFactory.obtain(trans.env(), data.approver);
							approver = new Approver(data.approver, org);
							users.put(data.approver, approver);
						}
						approver.addRequest(data.user);
					} catch (OrganizationException e) {
						trans.error().log(e);
					}
				}
	
				// Notify
				Message msg = new Message();
				for(Approver approver : users.values()) {
					try {
						Notification n = Notification.addApproval(trans, org.getIdentity(trans, approver.name));
						approver.build(msg);
						n.set(msg);
						if(n.update(trans, session, isDryRun())) {
							Identity user = n.org.getIdentity(trans, approver.name);
							email.clear();
							email.addTo(user.email());
							email.msg(msg);
							email.exec(trans, n.org);
						}
					} catch (OrganizationException e) {
						trans.error().log(e);
					}
				}
			}
		} else {
			trans.error().log('[',rladd.status,']',rladd.details);
		}
	}
	
	@Override
	protected void _close(AuthzTrans trans) {
		apprDAO.close(trans);
	}
	
	

}
