/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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
package org.onap.aaf.auth.batch.reports.bodies;

import java.util.GregorianCalendar;
import java.util.List;

import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.misc.env.util.Chrono;

public abstract class NotifyCredBody extends NotifyBody {

    private final String explanation;
    private final String instruction;

    public NotifyCredBody(Access access, String name) {
        super(access,"cred",name);

        // Default
        explanation = "The following Credentials that you are responsible for "
                + "are expiring on the dates shown. <br><br>"
                ;

        instruction = "<br><h3>Instructions for 'Password':</h3><ul>"
                + "<li><b><i>Click</i></b> on the Fully Qualified ID to ADD a new Password</li>"
                + "<li><b>REMEMBER!</b> You are not finished until you <ol>"
                + "<li><b>CHANGE <i>ALL</i></b> the configurations on <b><i>ALL</i></b> your processes!!</li>"
                + "<li><b>BOUNCE</b> them</li></ol>"
                + "<li>IF there is a WARNING, click the link for more information</li>"
                + "</ul>";
    }

    /**
     * Default Dynamic Text.  Override is expected
     * @return
     */
    protected String dynamic() {
        return "Failure to act before the expiration date will cause your App's Authentications to fail.";
    }

    @Override
    public boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id) {
        print(sb,indent,explanation);
        print(sb,indent,dynamic());
        println(sb,indent,instruction);
        println(sb,indent,"<table>");
        indent += 2;
        println(sb,indent,"<tr>");
        indent += 2;
        println(sb,indent,"<th>Fully Qualified ID</th>");
        println(sb,indent,"<th>Unique ID</th>");
        println(sb,indent,"<th>Type</th>");
        println(sb,indent,"<th>Expires</th>");
        println(sb,indent,"<th>Warnings</th>");
        indent -= 2;
        println(sb,indent,"</tr>");
        String theid;
        String type;
        String info;
        String expires;
        String warnings;
        GregorianCalendar gc = new GregorianCalendar();
        for(List<String> row : rows.get(id)) {
            theid=row.get(1);
            switch(row.get(3)) {
                case "1":
                case "2":
                    type = "Password";
                    break;
                case "200":
                    type = "x509 (Certificate)";
                    break;
                default:
                    type = "Unknown, see AAF GUI";
                    break;
            }
            theid = "<a href=\"" + n.guiURL + "/creddetail?ns=" + row.get(2) + "\">" + theid + "</a>";
            gc.setTimeInMillis(Long.parseLong(row.get(5)));
            expires = Chrono.niceUTCStamp(gc);
            info = row.get(6);
            //TODO get Warnings
            warnings = "";

            println(sb,indent,"<tr>");
            indent+=2;
            printCell(sb,indent,theid);
            printCell(sb,indent,info);
            printCell(sb,indent,type);
            printCell(sb,indent,expires);
            printCell(sb,indent,warnings);
            indent-=2;
            println(sb,indent,"</tr>");
        }
        indent-=2;
        println(sb,indent,"</table>");

        return true;
    }

    @Override
    public void record(AuthzTrans trans, StringBuilder query, String id, List<String> notified, LastNotified ln) {
        for(List<String> row : rows.get(id)) {
            for(String n : notified) {
                // Need to match LastNotified Key ... cred.id + '|' + inst.type + '|' + inst.tag;
                ln.update(query, n, row.get(0), row.get(1)+'|'+row.get(3)+'|'+row.get(6));
            }
        }
    }

    @Override
    public String user(List<String> row) {
        if( (row != null) && row.size()>1) {
            return row.get(1);
        }
        return null;
    }


}
