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

package org.onap.aaf.auth.env;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.principal.TrustPrincipal;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans.Metric;

public class AuthzTransFilter extends TransFilter<AuthzTrans> {
    private AuthzEnv env;
    public Metric serviceMetric;
    public static Slot transIDslot,specialLogSlot;

    public static final String TRANS_ID_SLOT = "TRANS_ID_SLOT";
    public static final String SPECIAL_LOG_SLOT = "SPECIAL_LOG_SLOT";

    public static final int BUCKETSIZE = 2;
    
    public AuthzTransFilter(AuthzEnv env, Connector con, TrustChecker tc, Object ... additionalTafLurs) throws CadiException, LocatorException {
        super(env.access(),con, tc, additionalTafLurs);
        this.env = env;
        serviceMetric = new Metric();
        serviceMetric.buckets = new float[BUCKETSIZE];
        if (transIDslot==null) {
            transIDslot = env.slot(TRANS_ID_SLOT);
        }
        if (specialLogSlot==null) {
            specialLogSlot = env.slot(SPECIAL_LOG_SLOT);
        }
    }
    
    @Override
    protected AuthzTrans newTrans(HttpServletRequest req, HttpServletResponse resp) {
        AuthzTrans at = env.newTrans();
        at.setLur(getLur());
        at.set(req,resp);
        return at;
    }

    @Override
    protected TimeTaken start(AuthzTrans trans) {
        return trans.start("Trans " + //(context==null?"n/a":context.toString()) +
        " IP: " + trans.ip() +
        " Port: " + trans.port()
        , Env.SUB);
    }

    @Override
    protected void authenticated(AuthzTrans trans, Principal p) {
        trans.setUser((TaggedPrincipal)p); // We only work with TaggedPrincipals in Authz
    }

    @Override
    protected void tallyHo(AuthzTrans trans, String target) {
        Boolean b = trans.get(specialLogSlot, false);
        LogTarget lt = b?trans.warn():trans.debug();
        
        if (lt.isLoggable()) {
            // Transaction is done, now post full Audit Trail
            StringBuilder sb = new StringBuilder("AuditTrail\n");
            // We'll grabAct sub-metrics for Remote Calls and JSON
            // IMPORTANT!!! if you add more entries here, change "BUCKETSIZE"!!!
            Metric m = trans.auditTrail(lt,1, sb, Env.REMOTE,Env.JSON);

            // Add current Metrics to total metrics
            serviceMetric.total+= m.total;
            for (int i=0;i<serviceMetric.buckets.length;++i) {
                serviceMetric.buckets[i]+=m.buckets[i];
            }
            
            Long tsi;
            if ((tsi=trans.get(transIDslot, null))!=null) {
                sb.append("  TraceID=");
                sb.append(Long.toHexString(tsi));
                sb.append('\n');
            }
            // Log current info
            sb.append("  Total: ");
            sb.append(m.total);
            sb.append(" Remote: ");
            sb.append(m.buckets[0]);
            sb.append(" JSON: ");
            sb.append(m.buckets[1]);
            lt.log(sb);
        } else {
            // Single Line entry
            // IMPORTANT!!! if you add more entries here, change "BUCKETSIZE"!!!
            StringBuilder content = new StringBuilder(); 
            Metric m = trans.auditTrail(lt,1, content, Env.REMOTE,Env.JSON);
            // Add current Metrics to total metrics
            serviceMetric.total+= m.total;
            for (int i=0;i<serviceMetric.buckets.length;++i) {
                serviceMetric.buckets[i]+=m.buckets[i];
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("user=");
            Principal p = trans.getUserPrincipal();
            if (p==null) {
                lt=trans.warn();
                sb.append(target);
                sb.append("[None]");
            } else {
                lt=trans.info();
                sb.append(p.getName());
                if (p instanceof TrustPrincipal) {
                    sb.append('(');
                    sb.append(((TrustPrincipal)p).personalName()); // UserChain
                    sb.append(')');
                } else { 
                    sb.append('[');
                    if (p instanceof TaggedPrincipal) {
                        sb.append(((TaggedPrincipal)p).tag());
                    } else {
                        sb.append(p.getClass().getSimpleName());
                    }
                    sb.append(']');
                }
            }
            String tag = trans.getTag();
            if(tag!=null) {
                sb.append(",tag=");
                sb.append(tag);
            }
            sb.append(",ip=");
            sb.append(trans.ip());
            sb.append(",port=");
            sb.append(trans.port());
//            Current code won't ever get here... Always does a Full Audit Trail
//            Long tsi;
//            if ((tsi=trans.get(transIDslot, null))!=null) {
//                sb.append(",TraceID=");
//                sb.append(Long.toHexString(tsi));
//            }
            sb.append(",ms=");
            sb.append(m.total);
            sb.append(",status=");
            sb.append(trans.hresp().getStatus());
            sb.append(",meth=");
            sb.append(trans.meth());
            sb.append(",path=");
            sb.append(trans.path());

            if (content.length()>0) {
                sb.append(",msg=\"");
                int start = content.lastIndexOf(",msg=\"");
                if (start>=0) {
                    sb.append(content,start+6,content.length()-1);
                } else {
                    sb.append(content);
                }
                sb.append('"');
            }
            
            lt.log(sb);
        }
    }

}
