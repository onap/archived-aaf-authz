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
package com.att.authz.env;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.att.cadi.CadiException;
import com.att.cadi.Connector;
import com.att.cadi.TrustChecker;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.TrustPrincipal;
import com.att.cadi.principal.X509Principal;
import com.att.cssa.rserv.TransFilter;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans.Metric;

public class AuthzTransFilter extends TransFilter<AuthzTrans> {
	private AuthzEnv env;
	public Metric serviceMetric;
	public static Slot transIDslot;

	public static final String TRANS_ID_SLOT = "TRANS_ID_SLOT";
	public static final int BUCKETSIZE = 2;

	public AuthzTransFilter(AuthzEnv env, Connector con, TrustChecker tc, Object ... additionalTafLurs) throws CadiException {
		super(env,con, tc, additionalTafLurs);
		this.env = env;
		serviceMetric = new Metric();
		serviceMetric.buckets = new float[BUCKETSIZE];
		if(transIDslot==null) {
			transIDslot = env.slot(TRANS_ID_SLOT);
		}
	}
	
	@Override
	protected AuthzTrans newTrans() {
		AuthzTrans at = env.newTrans();
		at.setLur(getLur());
		return at;
	}

	@Override
	protected TimeTaken start(AuthzTrans trans, ServletRequest request) {
		trans.set((HttpServletRequest)request);
		return trans.start("Trans " + //(context==null?"n/a":context.toString()) +
		" IP: " + trans.ip() +
		" Port: " + trans.port()
		, Env.SUB);
	}

	@Override
	protected void authenticated(AuthzTrans trans, Principal p) {
		trans.setUser(p);
	}

	@Override
	protected void tallyHo(AuthzTrans trans) {
		if(trans.info().isLoggable()) {
			// Transaction is done, now post
			StringBuilder sb = new StringBuilder("AuditTrail\n");
			// We'll grabAct sub-metrics for Remote Calls and JSON
			// IMPORTANT!!! if you add more entries here, change "BUCKETSIZE"!!!
			Metric m = trans.auditTrail(1, sb, Env.REMOTE,Env.JSON);

			// Add current Metrics to total metrics
			serviceMetric.total+= m.total;
			for(int i=0;i<serviceMetric.buckets.length;++i) {
				serviceMetric.buckets[i]+=m.buckets[i];
			}
			
			// Log current info
			sb.append("  Total: ");
			sb.append(m.total);
			sb.append(" Remote: ");
			sb.append(m.buckets[0]);
			sb.append(" JSON: ");
			sb.append(m.buckets[1]);
			trans.info().log(sb);
		} else {
			// IMPORTANT!!! if you add more entries here, change "BUCKETSIZE"!!!
			StringBuilder content = new StringBuilder(); 
			Metric m = trans.auditTrail(1, content, Env.REMOTE,Env.JSON);
			// Add current Metrics to total metrics
			serviceMetric.total+= m.total;
			for(int i=0;i<serviceMetric.buckets.length;++i) {
				serviceMetric.buckets[i]+=m.buckets[i];
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("user=");
			Principal p = trans.getUserPrincipal();
			if(p==null) {
				sb.append("n/a");
			} else {
				sb.append(p.getName());
				if(p instanceof TrustPrincipal) {
					sb.append('(');
					sb.append(((TrustPrincipal)p).getOrigName());
					sb.append(')');
				} else {
					sb.append('[');
					if(p instanceof X509Principal) {
						sb.append("x509");
					} else if(p instanceof BasicPrincipal) {
						sb.append("BAth");
					} else {
						sb.append(p.getClass().getSimpleName());
					}
					sb.append(']');
				}
			}
			sb.append(",ip=");
			sb.append(trans.ip());
			sb.append(",port=");
			sb.append(trans.port());
			sb.append(",ms=");
			sb.append(m.total);
			sb.append(",meth=");
			sb.append(trans.meth());
			sb.append(",path=");
			sb.append(trans.path());

			Long tsi;
			if((tsi=trans.get(transIDslot, null))!=null) {
				sb.append(",traceID=");
				sb.append(Long.toHexString(tsi));
			}
				
			if(content.length()>0) {
				sb.append(",msg=\"");
				sb.append(content);
				sb.append('"');
			}
			
			trans.warn().log(sb);
		}
	}

}
