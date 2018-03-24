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

package org.onap.aaf.cadi.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.EClient.Transfer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.util.Pool;
import org.onap.aaf.misc.env.util.Pool.Pooled;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

public abstract class Rcli<CT> {
	public static final String FORM_ENCODED = "application/x-www-form-urlencoded";
	public static final String APPL_JSON = "application/json";
	public static final String APPL_XML = "application/xml";
	public static final String BLANK = "";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";

	protected static final String POST = "POST";
	protected static final String GET = "GET";
	protected static final String PUT = "PUT";
	protected static final String DELETE = "DELETE";
	protected TYPE type;
	protected String apiVersion;
	protected int readTimeout = 5000;
	protected int connectionTimeout = 3000;
	protected URI uri;
	private String queryParams, fragment;
	public static Pool<byte[]> buffPool = new Pool<byte[]>(new Pool.Creator<byte[]>() {
		@Override
		public byte[] create() throws APIException {
			return new byte[1024];
		}

		@Override
		public void destroy(byte[] t) {
		}

		@Override
		public boolean isValid(byte[] t) {
			return true;
		}

		@Override
		public void reuse(byte[] t) {
		}
	});


	public Rcli() {
		super();
	}

	public abstract void setSecuritySetter(SecuritySetter<CT> ss);
	public abstract SecuritySetter<CT> getSecuritySetter();


	public Rcli<CT> forUser(SecuritySetter<CT> ss) {
		Rcli<CT> rv = clone(uri==null?this.uri:uri,ss);
		setSecuritySetter(ss);
		rv.type = type;
		rv.apiVersion = apiVersion;
		return rv;
	}
	
	protected abstract Rcli<CT> clone(URI uri, SecuritySetter<CT> ss);
	
	public abstract void invalidate() throws CadiException;

	public Rcli<CT> readTimeout(int millis) {
		readTimeout = millis;
		return this;
	}

	public Rcli<CT> connectionTimeout(int millis) {
		connectionTimeout = millis;
		return this;
	}

	public Rcli<CT> type(TYPE type) {
		this.type=type;
		return this;
	}

	public Rcli<CT> apiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
		return this;
	}
	
	public boolean isApiVersion(String prospective) {
		return apiVersion.equals(prospective);
	}


	public String typeString(Class<?> cls) {
		return "application/"+cls.getSimpleName()+"+"+type.name().toLowerCase()+
				(apiVersion==null?BLANK:";version="+apiVersion);
	}

	protected abstract EClient<CT> client() throws CadiException;


	public<T> Future<T> create(String pathinfo, String contentType, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,contentType);
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureCreate(df.getTypeClass());
	}

	public<T> Future<T> create(String pathinfo, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,typeString(df.getTypeClass()));
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureCreate(df.getTypeClass());
	}

	public<T> Future<T> create(String pathinfo, Class<?> cls, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,typeString(cls));
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureCreate(df.getTypeClass());
	}

	public<T> Future<T> create(String pathinfo, Class<T> cls) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,typeString(cls));
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureCreate(cls);
	}

	public Future<Void> create(String pathinfo, String contentType) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,contentType);
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureCreate(Void.class);
	}


	/**
	 * Post Data in WWW expected format, with the format tag1=value1&tag2=value2, etc
	 * Note Shortcut:
	 *   Because typically, you will want to have a variable as value, you can type, as long as tag ends with "="
	 *   postForm(..., "tag1=value1","tag2=",var2);
	 * @param pathinfo
	 * @param df
	 * @param cls
	 * @param formParam
	 * @return
	 * @throws APIException
	 * @throws CadiException
	 */
	public <T> Future<T> postForm(String pathinfo, final RosettaDF<T> df, final String ... formParam) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,FORM_ENCODED);
		switch(type) {
			case JSON:
				client.addHeader(ACCEPT, APPL_JSON);
				break;
			case XML:
				client.addHeader(ACCEPT, APPL_XML);
				break;
			default:
				break;
		}
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				PrintStream ps;
				if(os instanceof PrintStream) {
					ps = (PrintStream)os;
				} else {
					ps = new PrintStream(os);
				}
				boolean first = true;
				for(String fp : formParam) {
					if(fp!=null) {
						if(first) {
							first = false;
						} else {
							ps.print('&');
						}
						if(fp.endsWith("=")) {
							first = true;
						}
						ps.print(fp);
					}
				}
			}});
		client.send();
		queryParams = fragment = null;
		return client.futureRead(df,TYPE.JSON);
	}

	/**
	 * Read String, using POST for keyInfo
	 * 
	 * @param pathinfo
	 * @param df
	 * @param t
	 * @param resp
	 * @return
	 * @throws APIException
	 * @throws CadiException
	 */
	public<T> Future<String> readPost(String pathinfo, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,typeString(df.getTypeClass()));
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureReadString();
	}

	/**
	 * Read using POST for keyInfo, responding with marshaled Objects
	 *
	 * @param pathinfo
	 * @param df
	 * @param t
	 * @param resp
	 * @return
	 * @throws APIException
	 * @throws CadiException
	 */
	public<T,R> Future<R> readPost(String pathinfo, final RosettaDF<T> df, final T t, final RosettaDF<R> resp) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,typeString(df.getTypeClass()));
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureRead(resp,resp.getOutType());
	}

	public Future<String> readPost(String pathinfo, String contentType, String ... headers) throws CadiException, APIException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(POST);
		client.addHeader(CONTENT_TYPE,contentType);
		client.setPathInfo(pathinfo);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
			}});
		client.send();
		queryParams = fragment = null;
		return client.futureReadString();
	}

	public Future<String> read(String pathinfo, String accept, String ... headers) throws APIException, CadiException {
		final String qp;
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}
	
		EClient<CT> client = client();
		client.setMethod(GET);
		client.addHeader(ACCEPT, accept);
		
		for(int i=1;i<headers.length;i=i+2) {
			client.addHeader(headers[i-1],headers[i]);
		}
		client.setQueryParams(qp);
		client.setFragment(fragment);
	
		client.setPathInfo(pathinfo);
		
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureReadString();
	}

	public<T> Future<T> read(String pathinfo, String accept, RosettaDF<T> df, String ... headers) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(GET);
		client.addHeader(ACCEPT, accept);
		for(int i=1;i<headers.length;i=i+2) {
			client.addHeader(headers[i-1],headers[i]);
		}
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureRead(df,type);
	}

	public<T> Future<T> read(String pathinfo, RosettaDF<T> df,String ... headers) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(GET);
		client.addHeader(ACCEPT, typeString(df.getTypeClass()));
		for(int i=1;i<headers.length;i=i+2) {
			client.addHeader(headers[i-1],headers[i]);
		}
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureRead(df,type);
	}

	public<T> Future<T> read(String pathinfo, Class<?> cls, RosettaDF<T> df) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(GET);
		client.addHeader(ACCEPT, typeString(cls));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.futureRead(df,type);
	}

	public<T> Future<T> update(String pathinfo, String contentType, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(PUT);
		client.addHeader(CONTENT_TYPE,contentType);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}
	
	public<T> Future<String> updateRespondString(String pathinfo, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(PUT);
		client.addHeader(CONTENT_TYPE, typeString(df.getTypeClass()));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.futureReadString();
	}


	public<T> Future<T> update(String pathinfo, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(PUT);
		client.addHeader(CONTENT_TYPE, typeString(df.getTypeClass()));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}
	
	public<T> Future<T> update(String pathinfo, Class<?> cls, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(PUT);
		client.addHeader(CONTENT_TYPE, typeString(cls));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}

	/**
	 * A method to update with a VOID
	 * @param pathinfo
	 * @param resp
	 * @param expected
	 * @return
	 * @throws APIException
	 * @throws CadiException
	 */
	public<T> Future<Void> update(String pathinfo) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(PUT);
		client.addHeader(CONTENT_TYPE, typeString(Void.class));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
//		client.setPayload(new EClient.Transfer() {
//			@Override
//			public void transfer(OutputStream os) throws IOException, APIException {
//			}
//		});
		client.send();
		queryParams = fragment = null;
		return client.future(null);
	}

	public<T> Future<T> delete(String pathinfo, String contentType, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(DELETE);
		client.addHeader(CONTENT_TYPE, contentType);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}

	public<T> Future<T> delete(String pathinfo, Class<?> cls, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(DELETE);
		client.addHeader(CONTENT_TYPE, typeString(cls));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});
		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}

	public<T> Future<T> delete(String pathinfo, final RosettaDF<T> df, final T t) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(DELETE);
		client.addHeader(CONTENT_TYPE, typeString(df.getTypeClass()));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(new EClient.Transfer() {
			@Override
			public void transfer(OutputStream os) throws IOException, APIException {
				df.newData().out(type).direct(t,os);
			}
		});

		client.send();
		queryParams = fragment = null;
		return client.future(t);
	}


	public<T> Future<T> delete(String pathinfo, Class<T> cls) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}


		EClient<CT> client = client();
		client.setMethod(DELETE);
		client.addHeader(CONTENT_TYPE, typeString(cls));
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.future((T)null);
	}

	public Future<Void> delete(String pathinfo, String contentType) throws APIException, CadiException {
		final String qp; 
		if(pathinfo==null) {
			qp=queryParams;
		} else {
			final int idx = pathinfo.indexOf('?');
			if(idx>=0) {
				qp=pathinfo.substring(idx+1);
				pathinfo=pathinfo.substring(0,idx);
			} else {
				qp=queryParams;
			}
		}

		EClient<CT> client = client();
		client.setMethod(DELETE);
		client.addHeader(CONTENT_TYPE, contentType);
		client.setQueryParams(qp);
		client.setFragment(fragment);
		client.setPathInfo(pathinfo);
		client.setPayload(null);
		client.send();
		queryParams = fragment = null;
		return client.future(null);
	}

	public Future<Void> transfer(final HttpServletRequest req, final HttpServletResponse resp, final String pathParam, final int expected) throws CadiException, APIException {
		EClient<CT> client = client();
		URI uri;
		try {
			uri = new URI(req.getRequestURI());
		} catch (Exception e) {
			throw new CadiException("Invalid incoming URI",e);
		}
		String name;
		for(Enumeration<String> en = req.getHeaderNames();en.hasMoreElements();) {
			name = en.nextElement();
			client.addHeader(name,req.getHeader(name));
		}
		client.setQueryParams(req.getQueryString());
		client.setFragment(uri.getFragment());
		client.setPathInfo(pathParam);
		String meth = req.getMethod();
		client.setMethod(meth);
		if(!"GET".equals(meth)) {
			client.setPayload(new EClient.Transfer() {
				@Override
				public void transfer(OutputStream os) throws IOException, APIException {
					final ServletInputStream is = req.getInputStream();
					int read;
					// reuse Buffers
					Pooled<byte[]> pbuff = buffPool.get();
					try { 
						while((read=is.read(pbuff.content))>=0) {
							os.write(pbuff.content,0,read);
						}
					} finally {
						pbuff.done();
					}
				}
			});
		}
		client.send();
		return client.future(resp, expected);
	}

	public String toString() {
		return uri.toString();
	}

	/**
	 * @param queryParams the queryParams to set
	 * @return 
	 */
	public Rcli<CT> setQueryParams(String queryParams) {
		this.queryParams = queryParams;
		return this;
	}
	

	/**
	 * @param fragment the fragment to set
	 * @return 
	 */
	public Rcli<CT> setFragment(String fragment) {
		this.fragment = fragment;
		return this;
	}

	public URI getURI() {
		return uri;
	}

}