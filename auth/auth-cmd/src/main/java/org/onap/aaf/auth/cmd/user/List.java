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

package org.onap.aaf.auth.cmd.user;

import java.util.Collections;
import java.util.Comparator;

import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;
import aaf.v2_0.Delg;
import aaf.v2_0.Delgs;
import aaf.v2_0.Users;

public class List extends BaseCmd<User> {

    public List(User parent) {
        super(parent,"list");
        cmds.add(new ListForRoles(this));
        cmds.add(new ListForPermission(this));
        cmds.add(new ListForCreds(this));
        cmds.add(new ListDelegates(this));
        cmds.add(new ListApprovals(this));
        cmds.add(new ListActivity(this));
    }

     
    void report(Users users, boolean count, String ... str) {
        reportHead(str);
        int idx = 0;
        java.util.List<aaf.v2_0.Users.User> sorted = users.getUser();
        Collections.sort(sorted, (Comparator<aaf.v2_0.Users.User>) (u1, u2) -> {
            if (u1==null || u2 == null) {
                return -1;
            }
            return u1.getId().compareTo(u2.getId());
        });
        String format = reportColHead("%-48s %-5s %-11s %-16s\n","User","Type","Expires","Tag");
        String date = "XXXX-XX-XX";
        for (aaf.v2_0.Users.User user : sorted) {
            if (!aafcli.isTest()) {
                date = Chrono.dateOnlyStamp(user.getExpires());
            }
            String tag=user.getTag();
            Integer type = user.getType();
            if(tag==null) {
            	tag="";
            } else if(type!=null && type>=200) {
            	tag = "\n\tfingerprint: " + tag;
            }
            pw().format(format, 
                    count? (Integer.valueOf(++idx) + ") " + user.getId()): user.getId(),
                    org.onap.aaf.auth.cmd.ns.List.getType(user),
                    date,
                    tag);
        }
        pw().println();
    }

    public void report(Approvals approvals, String title, String id) {
        reportHead(title,id);
        String format = reportColHead("  %-20s %-20s %-11s %-6s %12s\n","User","Approver","Type","Status","Updated");
        java.util.List<Approval> lapp = approvals.getApprovals();
        Collections.sort(lapp, (Comparator<Approval>) (a1, a2) -> a1.getTicket().compareTo(a2.getTicket()));
        String ticket = null;
        String prev = null;
        for (Approval app : lapp ) {
            ticket = app.getTicket();
            if (!ticket.equals(prev)) {
                pw().print("Ticket: ");
                pw().println(ticket);
            }
            prev = ticket;

            pw().format(format,
                    app.getUser(),
                    app.getApprover(),
                    app.getType(),
                    app.getStatus(),
                    Chrono.niceDateStamp(app.getUpdated())
                    );
        }
    }

    public void report(Delgs delgs, String title, String id) {
        reportHead(title,id);
        String format = reportColHead(" %-25s %-25s  %-10s\n","User","Delegate","Expires");
        String date = "XXXX-XX-XX";
        for (Delg delg : delgs.getDelgs()) {
            if (!this.aafcli.isTest()) 
                date = Chrono.dateOnlyStamp(delg.getExpires());
            pw().printf(format, 
                        delg.getUser(),
                        delg.getDelegate(),
                        date
                        );
        }
    }


}
