/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package org.onap.aaf.osaaf.defOrg;

import java.io.IOException;
import java.util.List;

import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.local.AbsData.Reuse;
import org.onap.aaf.authz.org.Organization;
import org.onap.aaf.authz.org.OrganizationException;
import org.onap.aaf.authz.org.Organization.Identity;
import org.onap.aaf.osaaf.defOrg.Identities.Data;

import org.onap.aaf.cadi.config.Config;

/**
 * Org Users are essential representations of Identities within the Org.  Since this is a highly individual 
 * thing for most Orgs, i.e. some use LDAP, some need feed, some use something else, this object will allow
 * the Organization to connect to their own Identity systems...
 * 
 *
 */
public class DefaultOrgIdentity implements Identity {
    private final static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);
	
	private DefaultOrg org;
	private Data identity;
	private Identity owner;

	public DefaultOrgIdentity(AuthzTrans trans, String key, DefaultOrg dorg) throws OrganizationException {
		org = dorg;
		identity=null;
		try {
			org.identities.open(trans, TIMEOUT);
			try {
				Reuse r = org.identities.reuse();
				identity = org.identities.find(key, r);
				if(identity==null) {
					identity = Identities.NO_DATA;
				} else {
					if("a".equals(identity.status)) {
						owner = new DefaultOrgIdentity(trans,identity.responsibleTo,org);
					} else {
						owner = null;
					}
				}
			} finally {
				org.identities.close(trans);
			}
		} catch (IOException e) {
			throw new OrganizationException(e);
		}
	}
	
	@Override
	public boolean equals(Object b) {
		if(b instanceof DefaultOrgIdentity) {
			return identity.id.equals(((DefaultOrgIdentity)b).identity.id);
		}
		return false;
	}

	@Override
	public String id() {
		return identity.id;
	}

	@Override
	public String fullID() {
		return identity.id+'@'+org.getDomain();
	}

	@Override
	public String type() {
		switch(identity.status) {
			case "e": return DefaultOrg.Types.Employee.name();
			case "c": return DefaultOrg.Types.Contractor.name();
			case "a": return DefaultOrg.Types.Application.name();
			case "n": return DefaultOrg.Types.NotActive.name();
			default:
				return "Unknown";
		}
	}

	@Override
	public String responsibleTo() {
		return identity.responsibleTo;
	}

	@Override
	public List<String> delegate() {
		//NOTE:  implement Delegate system, if desired
		return DefaultOrg.NULL_DELEGATES;
	}

	@Override
	public String email() {
		return identity.email;
	}

	@Override
	public String fullName() {
		return identity.name;
	}

	@Override
	public boolean isResponsible() {
		return "e".equals(identity.status); // Assume only Employees are responsible for Resources.  
	}

	@Override
	public boolean isFound() {
		return identity!=null;
	}

	@Override
	public Identity owner() throws OrganizationException {
		return owner;
	}

	@Override
	public Organization org() {
		return org;
	}

}
