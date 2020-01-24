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
 * *
 ******************************************************************************/
package org.onap.aaf.org;

import java.io.IOException;
import java.util.List;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.org.Identities.Data;

/**
 * Org Users are essential representations of Identities within the Org.  Since this is a highly individual
 * thing for most Orgs, i.e. some use LDAP, some need feed, some use something else, this object will allow
 * the Organization to connect to their own Identity systems...
 *
 *
 */
public class DefaultOrgIdentity implements Identity {
    private static final String CONTRACTOR = "c";
    private static final String EMPLOYEE = "e";
    private static final String APPLICATION = "a";
    private static final String NON_ACTIVE = "n";

    final static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);

    private DefaultOrg org;
    //package on purpose
    Data identity;
    private AuthzTrans trans;

    public DefaultOrgIdentity(AuthzTrans trans, String key, DefaultOrg dorg) throws OrganizationException {
        this.trans = trans;
        org = dorg;
        identity=null;
        try {
            org.identities.open(trans, TIMEOUT);
            try {
                Reuse r = org.identities.reuse();
                int at = key.indexOf(dorg.getDomain());
                String search;
                if (at>=0) {
                    search = key.substring(0,at);
                } else {
                    search = key;
                }
                identity = org.identities.find(search, r);



                if (identity==null) {
                    identity = Identities.NO_DATA;
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
        if (b instanceof DefaultOrgIdentity) {
            return identity.id.equals(((DefaultOrgIdentity)b).identity.id);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return identity.hashCode();
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
            case EMPLOYEE: return DefaultOrg.Types.Employee.name();
            case CONTRACTOR: return DefaultOrg.Types.Contractor.name();
            case APPLICATION: return DefaultOrg.Types.Application.name();
            case NON_ACTIVE: return DefaultOrg.Types.NotActive.name();
            default:
                return "Unknown";
        }
    }

    @Override
    public Identity responsibleTo() throws OrganizationException {
    	if(isFound()) {
	        if ("".equals(identity.responsibleTo)) { // cover the situation of Top Dog... reports to no-one.
	            return this;
	        } else {
	            return org.getIdentity(trans, identity.responsibleTo);
	        }
        } else {
        	throw new OrganizationException("Identity doesn't exist");
        }
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
    public String firstName() {
        return identity.fname;
    }

    @Override
    public String mayOwn() {
        // Assume only Employees are responsible for Resources.
        if (identity.status==null|| identity.status.length()==0) {
            return "Identity must have valid status";
        } else if (EMPLOYEE.equals(identity.status)) {
            return null; // This is "Yes, is Responsible"
        } else {
            return "Reponsible Party must be an Employee";
        }
    }

    @Override
    public boolean isFound() {
        return identity!=Identities.NO_DATA; // yes, object comparison intended
    }

    @Override
    public boolean isPerson() {
        return !identity.status.equals(APPLICATION);
    }

    @Override
    public Organization org() {
        return org;
    }


}
