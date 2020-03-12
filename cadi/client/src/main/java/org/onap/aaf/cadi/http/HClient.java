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

package org.onap.aaf.cadi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.EClient;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.util.Pool.Pooled;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.owasp.encoder.Encode;

/**
 * Low Level Http Client Mechanism. Chances are, you want the high level "HRcli"
 * for Rosetta Object Translation
 *
 * @author Jonathan
 *
 */
public class HClient implements EClient<HttpURLConnection> {
    private URI uri;
    private ArrayList<Header> headers;
    private String meth;
    private String pathinfo;
    private String query;
    private String fragment;
    private Transfer transfer;
    private SecuritySetter<HttpURLConnection> ss;
    private HttpURLConnection huc;
    private int connectTimeout;

    public HClient(SecuritySetter<HttpURLConnection> ss, URI uri,int connectTimeout) throws LocatorException {
        if (uri == null) {
            throw new LocatorException("No Service available to call");
        }
        this.uri = uri;
        this.ss = ss;
        this.connectTimeout = connectTimeout;
        pathinfo = query = fragment = null;
    }

    @Override
    public void setMethod(String meth) {
        this.meth = meth;
    }

    @Override
    public void setPathInfo(String pathinfo) {
        this.pathinfo = pathinfo;
    }

    @Override
    public void setPayload(Transfer transfer) {
        this.transfer = transfer;
    }

    @Override
    public void addHeader(String tag, String value) {
        if (headers == null)
            headers = new ArrayList<>();
        headers.add(new Header(tag, value));
    }

    @Override
    public void setQueryParams(String q) {
        query = q;
    }

    @Override
    public void setFragment(String f) {
        fragment = f;
    }

    @Override
    public void send() throws APIException {
        // Build URL from given URI plus current Settings
        if (uri.getPath()==null) {
            throw new APIException("Invalid URL entered for HClient");
        }
        StringBuilder pi=null;
        if (pathinfo!=null) { // additional pathinfo
            pi = new StringBuilder(uri.getPath());
            if (!pathinfo.startsWith("/")) {
                pi.append('/');
            }
            pi.append(pathinfo);
        }
           URI sendURI = null;
        try {
            sendURI = new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    pi==null?uri.getPath():pi.toString(),
                    query==null?uri.getQuery():query,
                    fragment==null?uri.getFragment():fragment
                    );
            huc = getConnection(sendURI, pi);
            huc.setRequestMethod(meth);
            if (ss!=null) {
                ss.setSecurity(huc);
            }
            if (headers != null)
                for (Header d : headers) {
                    huc.addRequestProperty(d.tag, d.value);
                }
            huc.setDoInput(true);
            huc.setDoOutput(true);
            huc.setUseCaches(false);
            huc.setConnectTimeout(connectTimeout);
            huc.connect();
            if (transfer != null) {
                transfer.transfer(huc.getOutputStream());
            }
            // TODO other settings? There's a bunch here.
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            if(sendURI==null) {
                throw new APIException("Cannot connect to Root URI: '" + uri.toString() + '\'',e);
            } else {
                throw new APIException("Cannot connect to '" + sendURI.toString() + "' (Root URI: '" + uri.toString() + "')",e);
            }
        } finally { // ensure all these are reset after sends
            meth=pathinfo=null;
            if (headers!=null) {
                headers.clear();
            }
            pathinfo = query = fragment = "";
        }
    }

    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public int timeout() {
        return connectTimeout;
    }

    protected HttpURLConnection getConnection(URI uri, StringBuilder pi) throws IOException, URISyntaxException {
        URL url = new URI(
                uri.getScheme(),
                uri.getAuthority(),
                pi==null?uri.getPath():pi.toString(),
                query,
                fragment).toURL();
        return (HttpURLConnection) url.openConnection();
    }

     public abstract class HFuture<T> extends Future<T> {
        protected HttpURLConnection huc;
        protected int respCode;
        protected IOException exception;
        protected StringBuilder errContent;

        public HFuture(final HttpURLConnection huc) {
            this.huc = huc;
        }

        protected boolean evalInfo(HttpURLConnection huc) throws APIException, IOException{
            return respCode == 200;
        };

        @Override
        public final boolean get(int timeout) throws CadiException {
            try {
                huc.setReadTimeout(timeout);
                respCode = huc.getResponseCode();
                ss.setLastResponse(respCode);
                if (evalInfo(huc)) {
                    return true;
                } else {
                    extractError();
                    return false;
                }
            } catch (IOException | APIException e) {
                throw new CadiException(e);
            } finally {
                close();
            }
        }

        private void extractError() {
            InputStream is = huc.getErrorStream();
            try {
                if (is==null) {
                    is = huc.getInputStream();
                }
                if (is!=null) {
                errContent = new StringBuilder();
                int c;
                    while ((c=is.read())>=0) {
                        errContent.append((char)c);
                    }
                }
            } catch (IOException e) {
                exception = e;
            }
        }

        // Typically only used by Read
        public StringBuilder inputStreamToString(InputStream is) {
            // Avoids Carriage returns, and is reasonably efficient, given
            // the buffer reads.
            try {
                StringBuilder sb = new StringBuilder();
                Reader rdr = new InputStreamReader(is);
                try {
                    char[] buf = new char[256];
                    int read;
                    while ((read = rdr.read(buf)) >= 0) {
                        sb.append(buf, 0, read);
                    }
                } finally {
                    rdr.close();
                }
                return sb;
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }


        @Override
        public int code() {
            return respCode;
        }

        public HttpURLConnection huc() {
            return huc;
        }

        public IOException exception() {
            return exception;
        }

        @Override
        public String header(String tag) {
            return huc.getHeaderField(tag);
        }

        public void close() {
            if (huc!=null) {
                huc.disconnect();
            }
        }
    }

    @Override
    public <T> Future<T> futureCreate(Class<T> t) {
        return new HFuture<T>(huc) {
            public boolean evalInfo(HttpURLConnection huc) {
                return respCode==201;
            }

            @Override
            public String body() {
                if (errContent != null) {
                    return errContent.toString();
                }
                return "";
            }
        };
    }

    @Override
    public Future<String> futureReadString() {
        return new HFuture<String>(huc) {
            public boolean evalInfo(HttpURLConnection huc) throws IOException {
                if (respCode == 200) {
                    StringBuilder sb = inputStreamToString(huc.getInputStream());
                    if (sb != null) {
                        value = sb.toString();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public String body() {
                if (value != null) {
                    return value;
                } else if (errContent != null) {
                    return errContent.toString();
                }
                return "";
            }

        };
    }

    @Override
    public <T> Future<T> futureRead(final RosettaDF<T> df, final TYPE type) {
        return new HFuture<T>(huc) {
            private Data<T> data;

            public boolean evalInfo(HttpURLConnection huc) throws APIException, IOException {
                if (respCode == 200) {
                    data = df.newData().in(type).load(huc.getInputStream());
                    value = data.asObject();
                    return true;
                }
                return false;
            }

            @Override
            public String body() {
                if (data != null) {
                    try {
                        return data.asString();
                    } catch (APIException e) {
                    }
                } else if (errContent != null) {
                    return errContent.toString();
                }
                return "";
            }
        };
    }

    @Override
    public <T> Future<T> future(final T t) {
        return new HFuture<T>(huc) {
            public boolean evalInfo(HttpURLConnection huc) {
                if (respCode == 200) {
                    value = t;
                    return true;
                }
                return false;
            }

            @Override
            public String body() {
                if (errContent != null) {
                    return errContent.toString();
                }
                return Integer.toString(respCode);
            }
        };
    }

    @Override
    public Future<Void> future(final HttpServletResponse resp, final int expected) throws APIException {
        return new HFuture<Void>(huc) {
            public boolean evalInfo(HttpURLConnection huc) throws IOException, APIException {
                resp.setStatus(respCode);
                int read;
                InputStream is;
                OutputStream os = resp.getOutputStream();
                if (respCode==expected) {
                    is = huc.getInputStream();
                    // reuse Buffers
                    Pooled<byte[]> pbuff = Rcli.buffPool.get();
                    try {
                    	String strTemp; 
                        while ((read=is.read(pbuff.content))>=0) {
                        	strTemp = new String(pbuff.content,0,read);
                        	os.write(Encode.forJava(strTemp).getBytes());
                        }
                    } finally {
                        pbuff.done();
                    }
                    return true;
                } else {
                    is = huc.getErrorStream();
                    if (is==null) {
                        is = huc.getInputStream();
                    }
                    if (is!=null) {
                        errContent = new StringBuilder();
                        Pooled<byte[]> pbuff = Rcli.buffPool.get();
                        try {
                        	String strTemp; 
                            while ((read=is.read(pbuff.content))>=0) {
                                //os.write(pbuff.content,0,read);
                            	strTemp = new String(pbuff.content,0,read);
                            	os.write(Encode.forJava(strTemp).getBytes());
                            }
                        } finally {
                            pbuff.done();
                        }
                    }
                }
                return false;
            }

            @Override
            public String body() {
                return errContent==null?null:errContent.toString();
            }
        };
    }

    private static class Header {
        public final String tag;
        public final String value;

        public Header(String t, String v) {
            this.tag = t;
            this.value = v;
        }

        public String toString() {
            return tag + '=' + value;
        }
    }

    public String toString() {
        return "HttpURLConnection Client configured to " + uri.toString();
    }
}
