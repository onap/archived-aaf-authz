/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.aaf.auth.cmd.perm;

import java.util.Collections;
import java.util.Comparator;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Retryable;

import aaf.v2_0.Perms;

public class List extends BaseCmd<Perm> {
    private static final String permFormat = "%-30s %-30s %-10s\n";
    private static final Comparator<aaf.v2_0.Perm> permCompare = new Comparator<aaf.v2_0.Perm>() {
        @Override
        public int compare(aaf.v2_0.Perm a, aaf.v2_0.Perm b) {
            int rc;
            if ((rc=a.getType().compareTo(b.getType()))!=0) {
                return rc;
            }
            if ((rc=a.getInstance().compareTo(b.getInstance()))!=0) {
                return rc;
            }
            return a.getAction().compareTo(b.getAction());
        }
    };
    public List(Perm parent) {
        super(parent,"list");

        cmds.add(new ListByUser(this));
        cmds.add(new ListByName(this));
        cmds.add(new ListByNS(this));
        cmds.add(new ListByRole(this));
        cmds.add(new ListActivity(this));
    }
    // Package Level on purpose
    abstract class ListPerms extends Retryable<Integer> {
        protected int list(Future<Perms> fp,String header, String parentPerm) throws CadiException  {
            if (fp.get(AAFcli.timeout())) {
                report(fp,header, parentPerm);
            } else {
                error(fp);
            }
            return fp.code();
        }
    }

    void report(Future<Perms> fp, String ... str) {
        reportHead(str);
        if (this.aafcli.isDetailed()) {    
            String format = "%-36s %-30s %-15s\n";
            String descFmt = "   %-75s\n";
            reportColHead(format + descFmt,"[PERM NS].Type","Instance","Action", "Description");
            Collections.sort(fp.value.getPerm(),permCompare);
            for (aaf.v2_0.Perm p : fp.value.getPerm()) {
                String pns = p.getNs();
                if (pns==null) {
                    pw().format(format,
                            p.getType(),
                            p.getInstance(),
                            p.getAction());
                } else {
                    pw().format(format,
                            '['+pns + "]." + p.getType().substring(pns.length()+1),
                            p.getInstance(),
                            p.getAction());
                }
                String desc = p.getDescription();
                if (desc!=null && desc.length()>0) {
                    pw().format(descFmt,p.getDescription());
                }
            }
            pw().println();
        } else {
            String format = reportColHead(permFormat,"PERM Type","Instance","Action");

            Collections.sort(fp.value.getPerm(),permCompare);
            for (aaf.v2_0.Perm p : fp.value.getPerm()) {
                pw().format(format,
                    p.getType(),
                    p.getInstance(),
                    p.getAction());
            }
            pw().println();
        }
    }

}
