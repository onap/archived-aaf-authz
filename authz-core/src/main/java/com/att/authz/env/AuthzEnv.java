/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;
import com.att.inno.env.APIException;
import com.att.inno.env.Decryptor;
import com.att.inno.env.Encryptor;
import com.att.inno.env.impl.Log4JLogTarget;
import com.att.inno.env.log4j.LogFileNamer;
import com.att.rosetta.env.RosettaEnv;


/**
 * AuthzEnv is the Env tailored to Authz Service
 * 
 * Most of it is derived from RosettaEnv, but it also implements Access, which
 * is an Interface that Allows CADI to interact with Container Logging
 * 
 *
 */
public class AuthzEnv extends RosettaEnv implements Access {
	private long[] times = new long[20];
	private int idx = 0;
	//private int mask = Level.AUDIT.maskOf();

	public AuthzEnv() {
		super();
	}

	public AuthzEnv(String ... args) {
		super(args);
	}

	public AuthzEnv(Properties props) {
		super(Config.CADI_PROP_FILES,props);
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
		Properties props = new Properties();
		props.load(is);
		for(Entry<Object, Object> es : props.entrySet()) {
			String key = es.getKey().toString();
			String value =es.getValue().toString();
			put(staticSlot(key==null?null:key.trim()),value==null?null:value.trim());
		}
	}

	@Override
	public void log(Level lvl, Object... msgs) {
//		if(lvl.inMask(mask)) {
//			switch(lvl) {
//				case INIT:
//					init().log(msgs);
//					break;
//				case AUDIT:
//					audit().log(msgs);
//					break;
//				case DEBUG:
//					debug().log(msgs);
//					break;
//				case ERROR:
//					error().log(msgs);
//					break;
//				case INFO:
//					info().log(msgs);
//					break;
//				case WARN:
//					warn().log(msgs);
//					break;
//				case NONE:
//					break;
//			}
//		}
	}

	@Override
	public void log(Exception e, Object... msgs) {
		error().log(e,msgs);
	}

	//@Override
	public void printf(Level level, String fmt, Object... elements) {
		if(willLog(level)) {
			log(level,String.format(fmt, elements));
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.Access#willLog(com.att.cadi.Access.Level)
	 */
	@Override
	public boolean willLog(Level level) {
		
//		if(level.inMask(mask)) {
//			switch(level) {
//				case INIT:
//					return init().isLoggable();
//				case AUDIT:
//					return audit().isLoggable();
//				case DEBUG:
//					return debug().isLoggable();
//				case ERROR:
//					return error().isLoggable();
//				case INFO:
//					return info().isLoggable();
//				case WARN:
//					return warn().isLoggable();
//				case NONE:
//					return false;
//			}
//		}
		return false;
	}

	@Override
	public void setLogLevel(Level level) {
		super.debug().isLoggable();
		//level.toggle(mask);
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
	}
	
	private static final byte[] ENC="enc:???".getBytes();
	public String decrypt(String encrypted, final boolean anytext) throws IOException {
		if(encrypted==null) {
			throw new IOException("Password to be decrypted is null");
		}
		if(anytext || encrypted.startsWith("enc:")) {
			if(decryptor.equals(Decryptor.NULL) && getProperty(Config.CADI_KEYFILE)!=null) {
				final Symm s = Symm.obtain(this);
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
							return "enc:???"+s.enpass(data);
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
	
	
	
}
