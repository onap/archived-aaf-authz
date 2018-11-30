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

package org.onap.aaf.auth.batch.actions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.misc.env.util.Chrono;

public class Email implements Action<Organization,Void, String>{
    protected final List<String> toList;
    protected final List<String> ccList;
    private final String[] defaultCC;
    protected String subject;
    private String preamble;
    private Message msg;
    private String sig;
    protected String lineIndent="  ";
    private long lastSent=0L;

    
    public Email(String ... defaultCC) {
        toList = new ArrayList<>();
        this.defaultCC = defaultCC;
        ccList = new ArrayList<>();
        clear();
    }
    
    public Email clear() {
        toList.clear();
        ccList.clear();
        for (String s: defaultCC) {
            ccList.add(s);
        }
        return this;
    }
    

    public void indent(String indent) {
        lineIndent = indent;
    }
    
    public void preamble(String format, Object ... args) {
        preamble = String.format(format, args);
    }

    public Email addTo(Identity id) {
        if (id!=null && !toList.contains(id.email())) {
                toList.add(id.email());
        }
        return this;
    }

    public Email addTo(Collection<String> users) {
        for (String u : users) {
            addTo(u);
        }
        return this;
    }

    public Email addTo(String email) {
        if (!toList.contains(email)) {
            toList.add(email);
        }
        return this;
    }

    public Email addCC(Identity id) {
        if (id!=null && !ccList.contains(id.email())) {
                ccList.add(id.email());
        }
        return this;
    }

    public Email addCC(String email) {
        if (!ccList.contains(email)) {
            ccList.add(email);
        }
        return this;
    }

    
    public Email add(Identity id, boolean toSuper) throws OrganizationException {
        Identity responsible = id.responsibleTo();
        if (toSuper) {
            addTo(responsible.email());
            addCC(id.email());
        } else {
            addCC(responsible.email());
            addTo(id.email());
        }
        return this;
    }
    
    public Email subject(String format, Object ... args) {
        if (format.contains("%s")) {
            subject = String.format(format, args);
        } else {
            subject = format;
        }
        return this;
    }
    
    
    public Email signature(String format, Object ... args) {
        sig = String.format(format, args);
        return this;
    }
    
    public void msg(Message msg) {
        this.msg = msg;
    }
    
    @Override
    public Result<Void> exec(AuthzTrans trans, Organization org, String text) {
        StringBuilder sb = new StringBuilder();
        if (preamble!=null) {
            sb.append(lineIndent);
            sb.append(preamble);
            sb.append("\n\n");
        }
        
        if (msg!=null) {
            msg.msg(sb,lineIndent);
            sb.append("\n");
        }

        if (sig!=null) {
            sb.append(sig);
            sb.append("\n");
        }
        
        long ct = System.currentTimeMillis();
        long wait = ct-lastSent;
        lastSent = ct;
        if (wait < 100) { // 10 per second
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
            }
        }
        return exec(trans,org,sb);
    }

    protected Result<Void> exec(AuthzTrans trans, Organization org, StringBuilder sb) {
        try {
            /* int status = */
            org.sendEmail(trans,
                toList, 
                ccList, 
                subject, 
                sb.toString(), 
                false);
        } catch (Exception e) {
            return Result.err(Result.ERR_ActionNotCompleted,e.getMessage());
        }
        return Result.ok();

    }

    public void log(PrintStream ps, String text) {
        ps.print(Chrono.dateTime());
        boolean first = true;
        for (String s : toList) {
            if (first) {
                first = false;
                ps.print(": ");
            } else {
                ps.print(", ");
            }
            ps.print(s);
        }
        if (!ccList.isEmpty()) {
            first=true;
            for (String s : ccList) {
                if (first) {
                    first = false;
                    ps.print(" [");
                } else {
                    ps.print(", ");
                }
                ps.print(s);
            }
            ps.print(']');
        }

        ps.print(' ');
        ps.println(text);
    }
}
