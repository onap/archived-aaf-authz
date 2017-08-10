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
package com.att.dao.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.att.cssa.rserv.TransFilter;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.EnvStore;
import com.att.inno.env.Slot;
import com.att.inno.env.TransStore;
import com.att.inno.env.util.Pool;
import com.att.inno.env.util.Pool.Creator;
import com.att.inno.env.util.Pool.Pooled;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class SessionFilter<TRANS extends TransStore> implements Filter {
	public static final String SESSION_SLOT = "__SESSION__";
	private static Slot sessionSlot;
	private static Pool<Session> pool;

	public SessionFilter(EnvStore<?> env, Cluster cluster, String keyspace) {
		synchronized(env) {
			if(sessionSlot==null) {
				sessionSlot = env.slot(SESSION_SLOT);
			}
			if(pool==null) {
				pool = new Pool<Session>(new SessionCreator(env,cluster,keyspace));
			}
		}
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		// Session does not need any sort of configuration from Filter
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,	FilterChain chain) throws IOException, ServletException {
		@SuppressWarnings("unchecked")
		TRANS trans = (TRANS)req.getAttribute(TransFilter.TRANS_TAG);
		try {
			Pooled<Session> psess = pool.get();
			try {
				trans.put(sessionSlot, psess.content);
				chain.doFilter(req, resp);
			} finally {
				psess.done();
			}
		} catch (APIException e) {
			throw new ServletException(e);
		}
	}

	public Pooled<Session> load(TRANS trans) throws APIException {
		Pooled<Session> psess = pool.get();
		trans.put(sessionSlot, psess.content);
		return psess;
	}
	
	
	/**
	 * Clear will drain the pool, so that new Sessions will be constructed.
	 * 
	 * Suitable for Management calls.	 
	 */
	public static void clear() {
		if(pool!=null) {
			pool.drain();
		} 
	}
	
	@Override
	public void destroy() {
		pool.drain();
	}

	private class SessionCreator implements Creator<Session> {
		private Cluster cluster;
		private String keyspace;
		private Env env;
		
		public SessionCreator(Env env, Cluster cluster, String keyspace) {
			this.cluster = cluster;
			this.keyspace = keyspace;
			this.env = env;
		}
		
		@Override
		public Session create() throws APIException {
			env.info().log("Creating a Cassandra Session");
			return cluster.connect(keyspace);
		}

		@Override
		public void destroy(Session t) {
			env.info().log("Shutting down a Cassandra Session");
			t.close();
		}

		@Override
		public boolean isValid(Session t) {
			return true;
		}

		@Override
		public void reuse(Session t) {
			// Nothing is needed to reuse this Session
		}
		
	}
}
