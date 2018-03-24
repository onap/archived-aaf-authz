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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.PropAccess.LogIt;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.impl.Log4JLogTarget;
import org.onap.aaf.misc.env.log4j.LogFileNamer;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;


/**
 * AuthzEnv is the Env tailored to Authz Service
 * 
 * Most of it is derived from RosettaEnv, but it also implements Access, which
 * is an Interface that Allows CADI to interact with Container Logging
 * 
 * @author Jonathan
 *
 */
public class AuthzEnv extends RosettaEnv implements Access {
	private long[] times = new long[20];
	private int idx = 0;
	private PropAccess access;

	public AuthzEnv() {
		super();
		_init(new PropAccess());
	}

	public AuthzEnv(String ... args) {
		super();
		_init(new PropAccess(args));
	}

	public AuthzEnv(Properties props) {
		super();
		_init(new PropAccess(props));
	}
	

	public AuthzEnv(PropAccess pa) {
		super();
		_init(pa);
	}
	
	private final void _init(PropAccess pa) { 
		access = pa;
		times = new long[20];
		idx = 0;
	}
	
	private class Log4JLogit implements LogIt {
		
		@Override
		public void push(Level level, Object... elements) {
			switch(level) {
				case AUDIT:
					audit.log(elements);
					break;
				case DEBUG:
					debug.log(elements);
					break;
				case ERROR:
					error.log(elements);
					break;
				case INFO:
					info.log(elements);
					break;
				case INIT:
					init.log(elements);
					break;
				case NONE:
					break;
				case WARN:
					warn.log(elements);
					break;
			}
			
		}
		
	}

	@Override
	public AuthzTransImpl newTrans() {
		synchronized(this) {
			times[idx]=System.currentTimeMillis();
			if(++idx>=times.length)idx=0;
		}
		return new AuthzTransImpl(this);
	}

	/**
	 *  Create a Trans, but do not include in Weighted Average
	 * @return
	 */
	public AuthzTrans newTransNoAvg() {
		return new AuthzTransImpl(this);
	}

	public long transRate() {
		int count = 0;
		long pot = 0;
		long prev = 0;
		for(int i=idx;i<times.length;++i) {
			if(times[i]>0) {
				if(prev>0) {
					++count;
		pot += times[i]-prev;
				}
				prev = times[i]; 
			}
		}
		for(int i=0;i<idx;++i) {
			if(times[i]>0) {
				if(prev>0) {
					++count;
					pot += times[i]-prev;
				}
				prev = times[i]; 
			}
		}

		return count==0?300000L:pot/count; // Return Weighted Avg, or 5 mins, if none avail.
	}
	
	@Override
	public ClassLoader classLoader() {
		return getClass().getClassLoader();
	}

	@Override
	public void load(InputStream is) throws IOException {
		access.load(is);
	}

	@Override
	public void log(Level lvl, Object... msgs) {
		access.log(lvl, msgs);
	}

	@Override
	public void log(Exception e, Object... msgs) {
		access.log(e,msgs);
	}

	@Override
	public void printf(Level level, String fmt, Object... elements) {
		access.printf(level, fmt, elements);
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.cadi.Access#willLog(org.onap.aaf.cadi.Access.Level)
	 */
	@Override
	public boolean willLog(Level level) {
		return access.willLog(level);
	}

	@Override
	public void setLogLevel(Level level) {
		access.setLogLevel(level);
	}

	public void setLog4JNames(String path, String root, String _service, String _audit, String _init, String _trace) throws APIException {
		LogFileNamer lfn = new LogFileNamer(root);
		if(_service==null) {
			throw new APIException("AuthzEnv.setLog4JNames \"_service\" required (as default).  Others can be null");
		}
		String service=_service=lfn.setAppender(_service); // when name is split, i.e. authz|service, the Appender is "authz", and "service"
		String audit=_audit==null?service:lfn.setAppender(_audit);     // is part of the log-file name
		String init=_init==null?service:lfn.setAppender(_init);
		String trace=_trace==null?service:lfn.setAppender(_trace);
		//TODO Validate path on Classpath
		lfn.configure(path);
		super.fatal = new Log4JLogTarget(service,org.apache.log4j.Level.FATAL);
		super.error = new Log4JLogTarget(service,org.apache.log4j.Level.ERROR);
		super.warn = new Log4JLogTarget(service,org.apache.log4j.Level.WARN);
		super.audit = new Log4JLogTarget(audit,org.apache.log4j.Level.WARN);
		super.init = new Log4JLogTarget(init,org.apache.log4j.Level.WARN);
		super.info = new Log4JLogTarget(service,org.apache.log4j.Level.INFO);
		super.debug = new Log4JLogTarget(service,org.apache.log4j.Level.DEBUG);
		super.trace = new Log4JLogTarget(trace,org.apache.log4j.Level.TRACE);
		
		access.set(new Log4JLogit());
	}
	
	private static final byte[] ENC="enc:".getBytes();
	public String decrypt(String encrypted, final boolean anytext) throws IOException {
		if(encrypted==null) {
			throw new IOException("Password to be decrypted is null");
		}
		if(anytext || encrypted.startsWith("enc:")) {
			if(decryptor.equals(Decryptor.NULL) && getProperty(Config.CADI_KEYFILE)!=null) {
				final Symm s;
				try {
					s = Symm.obtain(this);
				} catch (CadiException e1) {
					throw new IOException(e1);
				}
				decryptor = new Decryptor() {
					private Symm symm = s;
					@Override
					public String decrypt(String encrypted) {
						try {
							return (encrypted!=null && (anytext || encrypted.startsWith(Symm.ENC)))
									? symm.depass(encrypted)
									: encrypted;
						} catch (IOException e) {
							return "";
						}
					}
				};
				encryptor = new Encryptor() {
					@Override
					public String encrypt(String data) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						try {
							baos.write(ENC);
							return "enc:"+s.enpass(data);
						} catch (IOException e) {
							return "";
						}
					}
	
				};
			}
			return decryptor.decrypt(encrypted);
		} else {
			return encrypted;
		}
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.misc.env.impl.BasicEnv#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		return access.getProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.misc.env.impl.BasicEnv#getProperties(java.lang.String[])
	 */
	@Override
	public Properties getProperties(String... filter) {
		return access.getProperties();
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.misc.env.impl.BasicEnv#getProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		return access.getProperty(key, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.misc.env.impl.BasicEnv#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public String setProperty(String key, String value) {
		access.setProperty(key, value);
		return value;
	}

	public PropAccess access() {
		return access;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.cadi.Access#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return access.getProperties();
	};
	
}
