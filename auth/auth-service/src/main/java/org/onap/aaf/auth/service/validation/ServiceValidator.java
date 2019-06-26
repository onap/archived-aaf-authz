/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.service.validation;

import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.rserv.Pair;
import org.onap.aaf.auth.validation.Validator;

/**
 * Validator
 * Consistently apply content rules for content (incoming)
 * 
 * Note: We restrict content for usability in URLs (because RESTful service), and avoid 
 * issues with Regular Expressions, and other enabling technologies. 
 * @author Jonathan
 *
 */
public class ServiceValidator extends Validator {
    public ServiceValidator perm(Result<PermDAO.Data> rpd) {
        if (rpd.notOK()) {
            msg(rpd.details);
        } else {
            perm(rpd.value);
        }
        return this;
    }


    public ServiceValidator perm(PermDAO.Data pd) {
        if (pd==null) {
            msg("Perm Data is null.");
        } else {
        	if(!pd.ns.contains("@")) { 
        		ns(pd.ns);
        	}
            permType(pd.type,pd.ns);
            permInstance(pd.instance);
            permAction(pd.action);
            if (pd.roles!=null) { 
                for (String role : pd.roles) {
                    role(role);
                }
            }
            if (pd.roles!=null) {
                for (String r : pd.roles) {
                    role(r);
                }
            }
            description("Perm",pd.description);
        }
        return this;
    }

    public ServiceValidator role(Result<RoleDAO.Data> rrd) {
        if (rrd.notOK()) {
            msg(rrd.details);
        } else {
            role(rrd.value);
        }
        return this;
    }
    
    public ServiceValidator role(RoleDAO.Data pd) {
        if (pd==null) {
            msg("Role Data is null.");
        } else {
            ns(pd.ns);
            role(pd.name);
            if (pd.perms!=null) {
                for (String perm : pd.perms) {
                    String[] ps = perm.split("\\|");
                    if (ps.length!=3) {
                        msg("Perm [" + perm + "] in Role [" + pd.fullName() + "] is not correctly separated with '|'");
                    } else {
                        permType(ps[0],null);
                        permInstance(ps[1]);
                        permAction(ps[2]);
                    }
                }
            }
            description("Role",pd.description);
        }
        return this;
    }

    public ServiceValidator delegate(Organization org, Result<DelegateDAO.Data> rdd) {
        if (rdd.notOK()) {
            msg(rdd.details);
        } else {
            delegate(org, rdd.value);
        }
        return this;
    }

    public ServiceValidator delegate(Organization org, DelegateDAO.Data dd) {
        if (dd==null) {
            msg("Delegate Data is null.");
        } else {
            user(org,dd.user);
            user(org,dd.delegate);
        }
        return this;
    }


    public ServiceValidator cred(AuthzTrans trans, Organization org, Result<CredDAO.Data> rcd, boolean isNew) {
        if (rcd.notOK()) {
            msg(rcd.details);
        } else {
            cred(trans, org,rcd.value,isNew);
        }
        return this;
    }

    public ServiceValidator cred(AuthzTrans trans, Organization org, CredDAO.Data cd, boolean isNew) {
        if (cd==null) {
            msg("Cred Data is null.");
        } else {
            if (!org.isValidCred(trans, cd.id)) {
                msg("ID [" + cd.id + "] is invalid in " + org.getName());
            }
            String str = cd.id;
            int idx = str.indexOf('@');
            if (idx>0) {
                str = str.substring(0,idx);
            }
            
            if (org.supportsRealm(cd.id)) {
                String resp = org.isValidID(trans, str);
                if (isNew && (resp!=null && resp.length()>0)) {
                    msg(cd.id,str);
                }
            }
    
            if (cd.type==null) {
                msg("Credential Type must be set");
            } else {
                switch(cd.type) {
                    case CredDAO.BASIC_AUTH_SHA256:
                    case CredDAO.FQI:
                        // ok
                        break;
                    default:
                        msg("Credential Type [",Integer.toString(cd.type),"] is invalid");
                }
            }
        }
        return this;
    }


    public ServiceValidator user(Organization org, String user) {
        if (nob(user,ID_CHARS)) {
            msg("User [",user,"] is invalid.");
        }
        return this;
    }

    public ServiceValidator ns(Result<Namespace> nsd) {
        notOK(nsd);
        ns(nsd.value);
        return this;
    }

    public ServiceValidator ns(Namespace ns) {
        ns(ns.name);
        for (String s : ns.admin) {
            if (nob(s,ID_CHARS)) {
                msg("Admin [" + s + "] is invalid.");        
            }
            
        }
        for (String s : ns.owner) {
            if (nob(s,ID_CHARS)) {
                msg("Responsible [" + s + "] is invalid.");        
            }
            
        }
        
        if (ns.attrib!=null) {
            for (Pair<String, String> at : ns.attrib) {
                if (nob(at.x,NAME_CHARS)) {
                    msg("Attribute tag [" + at.x + "] is invalid.");
                }
                if (nob(at.x,NAME_CHARS)) {
                    msg("Attribute value [" + at.y + "] is invalid.");
                }
            }
        }

        description("Namespace",ns.description);
        return this;
    }

    public ServiceValidator user_role(String user, UserRoleDAO.Data urdd) {
        role(user,urdd.role);
        if(!urdd.role.startsWith(user)) { 
	        nullOrBlank("UserRole.ns",urdd.ns);
	        nullOrBlank("UserRole.rname",urdd.rname);
        }
        return this;
    }

    
    public ServiceValidator user_role(UserRoleDAO.Data urdd) {
        if (urdd==null) {
            msg("UserRole is null");
        } else {
            role(urdd.role);
            nullOrBlank("UserRole.ns",urdd.ns);
            nullOrBlank("UserRole.rname",urdd.rname);
        }
        return this;
    }

    public ServiceValidator nullOrBlank(PermDAO.Data pd) {
        if (pd==null) {
            msg("Permission is null");
        } else {
            nullOrBlank("NS",pd.ns).
            nullOrBlank("Type",pd.type).
            nullOrBlank("Instance",pd.instance).
            nullOrBlank("Action",pd.action);
        }
        return this;
    }

    public ServiceValidator nullOrBlank(RoleDAO.Data rd) {
        if (rd==null) {
            msg("Role is null");
        } else {
            nullOrBlank("NS",rd.ns).
            nullOrBlank("Name",rd.name);
        }
        return this;
    }
}
