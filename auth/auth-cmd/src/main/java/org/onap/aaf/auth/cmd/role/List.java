/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modification Copyright (c) 2019 IBM
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

package org.onap.aaf.auth.cmd.role;

import java.util.Collections;
import java.util.Comparator;

import javax.xml.datatype.XMLGregorianCalendar;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoles;



public class List extends BaseCmd<Role> {
    private static final String XXXX_XX_XX = "XXXX-XX-XX";
    private static final String LIST_ROLES_BY_NAME = "list roles for role";

    public List(Role parent) {
        super(parent,"list");
        cmds.add(new ListByUser(this));
        cmds.add(new ListByRole(this));
        cmds.add(new ListByNS(this));
        cmds.add(new ListByNameOnly(this));
        cmds.add(new ListByPerm(this));
        cmds.add(new ListActivity(this));
    }

    // Package Level on purpose
    abstract class ListRoles extends Retryable<Integer> {
        protected int list(Future<Roles> fr,Rcli<?> client, String header) throws APIException, CadiException {
            if (fr.get(AAFcli.timeout())) {
                Perms perms=null;
                if (aafcli.isDetailed()) {
                    for (aaf.v2_0.Role r : fr.value.getRole()) {
                        Future<Perms> fp = client.read(
                                "/authz/perms/role/"+r.getName()+(aafcli.isDetailed()?"?ns":""),
                                getDF(Perms.class)
                            );
                        if (fp.get(AAFcli.timeout())) {
                            if (perms==null) {
                                perms = fp.value;
                            } else {
                                perms.getPerm().addAll(fp.value.getPerm());
                            }
                        }
                    }
                }
                report(fr.value,perms,null,header);
            } else {
                error(fr);
            }
            return fr.code();
        }
    }

    private static final String roleFormat = "%-56s Expires %s\n";
    private static final String roleFormatNoDate = "%-61s\n";
    private static final String roleExpiredFormat = "%-53s !!! EXPIRED !!! %s\n";
    private static final String permFormat = "   %-30s %-30s %-15s\n";


    private static final Comparator<aaf.v2_0.Role> roleCompare = new Comparator<aaf.v2_0.Role>() {
        @Override
        public int compare(aaf.v2_0.Role a, aaf.v2_0.Role b) {
            return a.getName().compareTo(b.getName());
        }
    };
    public void report(Roles roles, Perms perms, UserRoles urs, String ... str) {
        reportHead(str);
        XMLGregorianCalendar now = Chrono.timeStamp().normalize();
        if (roles==null || roles.getRole().isEmpty()) {
            pw().println("<No Roles Found>");
        } else if (aafcli.isDetailed()){
            if (str[0].toLowerCase().contains(LIST_ROLES_BY_NAME)) {
                String description = roles.getRole().get(0).getDescription();
                if (description == null) {
                    description = "";
                }
                reportColHead("%-80s\n","Description: " + description);
            }

            String fullFormat = roleFormat+permFormat;
            reportColHead(fullFormat,"[ROLE NS].Name","","[PERM NS].Type","Instance","Action");
            Collections.sort(roles.getRole(),roleCompare);
            for (aaf.v2_0.Role r : roles.getRole()) {
                String roleName = r.getName();
                String ns = r.getNs();
                if (aafcli.isTest()) {
                    if (ns==null) {
                        pw().format(roleFormat, roleName,XXXX_XX_XX);
                    } else {
                        pw().format(roleFormat, "["+ns+"]"+roleName.substring(ns.length()),XXXX_XX_XX);
                    }
                } else {
                    String fullname;
                    if(ns==null) {
                        fullname = roleName;
                    } else {
                        fullname = ns+'.'+roleName;
                    }
                    UserRole ur = get(fullname,urs);
                    if (ur!=null && now.compare(ur.getExpires().normalize())>0) {
                        if (ns==null) {
                            pw().format(roleExpiredFormat, roleName,Chrono.dateOnlyStamp(ur.getExpires()));
                        } else {
                            pw().format(roleExpiredFormat, "["+ns+"]."+roleName,Chrono.dateOnlyStamp(ur.getExpires()));
                        }
                    } else {
                        if (ns==null) {
                            pw().format(roleFormat, roleName,ur!=null?Chrono.dateOnlyStamp(ur.getExpires()):"");
                        } else {
                            pw().format(roleFormat, "["+ns+"]."+roleName,ur!=null?Chrono.dateOnlyStamp(ur.getExpires()):"");
                        }
                    }
                }

                for (Pkey pkey : r.getPerms()) {
                    Perm perm = get(pkey,perms);
                    if (perm==null || perm.getNs()==null) {
                        pw().format(permFormat,
                                pkey.getType(),
                                pkey.getInstance(),
                                pkey.getAction());
                    } else {
                        String ns1 = perm.getNs();
                        pw().format(permFormat,
                                '['+ns1+"]"+perm.getType().substring(ns1.length()),
                                perm.getInstance(),
                                perm.getAction());
                    }
                }
            }
        } else {
            String fullFormat = roleFormat;
            reportColHead(fullFormat,"ROLE Name","","PERM Type","Instance","Action");
            Collections.sort(roles.getRole(),roleCompare);
            for (aaf.v2_0.Role r : roles.getRole()) {
                if (urs != null) {
                    String roleName = r.getName();
                    if (!aafcli.isTest()) {
                        UserRole ur = get(roleName,urs);
                        if (ur!=null && now.compare(ur.getExpires().normalize())>0) {
                            pw().format(roleExpiredFormat, roleName+"*",Chrono.dateOnlyStamp(ur.getExpires()));
                        } else {
                            pw().format(roleFormat, roleName,ur!=null?Chrono.dateOnlyStamp(ur.getExpires()):"");
                        }
                    } else {
                        pw().format(roleFormat, roleName,XXXX_XX_XX);
                    }
                } else {
                    pw().format(roleFormatNoDate, r.getName());
                    for (Pkey perm : r.getPerms()) {
                        pw().format(permFormat,
                                perm.getType(),
                                perm.getInstance(),
                                perm.getAction());
                    }
                }
            }
        }
    }
    private Perm get(Pkey pkey, Perms perms) {
        if (perms!=null) {
            for (Perm p : perms.getPerm()) {
                if (pkey.getAction().equals(p.getAction()) &&
                   pkey.getInstance().equals(p.getInstance()) &&
                   pkey.getType().equals(p.getType())) {
                    return p;
                }
            }
        }
        return null;
    }
    // The assumption is that these UserRoles are already pulled in by User... no need to check
    private UserRole get(String roleName, UserRoles urs) {
        if (urs!=null) {
            for (UserRole ur : urs.getUserRole()) {
                if (roleName.equals(ur.getRole())) {
                    return ur;
                }
            }
        }
        return null;
    }

}
