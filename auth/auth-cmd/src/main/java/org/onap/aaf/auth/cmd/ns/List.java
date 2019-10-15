/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cmd.ns;

import java.util.Collections;
import java.util.Comparator;

import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.auth.cmd.DeprecatedCMD;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perms;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

public class List extends BaseCmd<NS> {

    private static final String cformat = "        %-30s %-6s %-24s\n";
    private static final String pformat = "        %-30s %-24s %-15s\n";
    private static final String sformat = "        %-72s\n";
    protected static final String kformat = "  %-72s\n";

    public List(NS parent) {
        super(parent,"list");
        cmds.add(new ListByName(this));
    
//        TODO: uncomment when on cassandra 2.1.2 if we like cli command to get all ns's 
//                a user is admin or responsible for 
        cmds.add(new ListAdminResponsible(this));
        cmds.add(new DeprecatedCMD<List>(this,"responsible","'responsible' is deprecated.  use 'owner'")); // deprecated
        cmds.add(new ListActivity(this));
        cmds.add(new ListUsers(this));
        cmds.add(new ListChildren(this));
        cmds.add(new ListNsKeysByAttrib(this));
    }

    public void report(Future<Nss> fp, String ... str) {
        reportHead(str);
        if (fp==null) {
            pw().println("    *** Namespace Not Found ***");
        }
    
        if (fp!=null && fp.value!=null) {
            for (Ns ns : fp.value.getNs()) {
                pw().println(ns.getName());
                if (this.aafcli.isDetailed()) {
                    pw().println("    Description");
                    pw().format(sformat,ns.getDescription()==null?"":ns.getDescription());
                }
                if (!(ns.getAdmin().isEmpty())) {
                    pw().println("    Administrators");
                    for (String admin : ns.getAdmin()) {
                        pw().format(sformat,admin);
                    }
                }
                if (!(ns.getResponsible().isEmpty())) {
                    pw().println("    Owners (Responsible for Namespace)");
                    for (String responsible : ns.getResponsible()) {
                        pw().format(sformat,responsible);
                    }
                }
                if (!(ns.getAttrib().isEmpty())) {
                    pw().println("    Namespace Attributes");
                    for (  Ns.Attrib attr : ns.getAttrib()) {
                        StringBuilder sb = new StringBuilder(attr.getKey());
                        if (attr.getValue()==null || attr.getValue().length()>0) {
                            sb.append('=');
                            sb.append(attr.getValue());
                        }
                        pw().format(sformat,sb.toString());
                    }
                
                }
            }
        }
    }

    public void reportName(Future<Nss> fp, String ... str) {
        reportHead(str);
        if (fp!=null && fp.value!=null) {
            java.util.List<Ns> nss = fp.value.getNs();
            Collections.sort(nss, new Comparator<Ns>() {
                @Override
                public int compare(Ns ns1, Ns ns2) {
                    return ns1.getName().compareTo(ns2.getName());
                }
            });
        
            for (Ns ns : nss) {
                pw().println(ns.getName());
                if (this.aafcli.isDetailed() && ns.getDescription() != null) {
                    pw().println("   " + ns.getDescription());
                }
            }
        }
    }

    public void reportRole(Future<Roles> fr) {
        if (fr!=null && fr.value!=null && !(fr.value.getRole().isEmpty())) {
            pw().println("    Roles");
            for (aaf.v2_0.Role r : fr.value.getRole()) {
                pw().format(sformat,r.getName());
            }
        }
    }

    public void reportPerm(Future<Perms> fp) {
        if (fp!=null && fp.value!=null && !(fp.value.getPerm().isEmpty())) {
            pw().println("    Permissions");
            for (aaf.v2_0.Perm p : fp.value.getPerm()) {
                pw().format(pformat,p.getType(),p.getInstance(),p.getAction());
            }
        }
    }


    public void reportCred(Future<Users> fc) {    
        if (fc!=null && fc.value!=null && !(fc.value.getUser().isEmpty())) {
            pw().println("    Credentials");
            java.util.List<User> users = fc.value.getUser();
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User u1, User u2) {
                    return u1.getId().compareTo(u2.getId());
                }
            });
            for (aaf.v2_0.Users.User u : users) {
                if (this.aafcli.isTest()) {
                    pw().format(sformat,u.getId());
                } else {
                    pw().format(cformat,u.getId(),getType(u),Chrono.niceDateStamp(u.getExpires()));
                }
            }
        }
    }

    public static String getType(User u) {
        Integer type;
        if ((type=u.getType())==null) {
            type = 9999;
        } 
        return Define.getCredType(type);
    }


}
