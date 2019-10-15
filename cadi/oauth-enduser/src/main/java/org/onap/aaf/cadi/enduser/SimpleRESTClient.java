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
package org.onap.aaf.cadi.enduser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.security.Principal;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.EClient;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.cadi.oauth.TzClient;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.StringBuilderWriter;

public class SimpleRESTClient {
    private static final String APPLICATION_JSON = "application/json";
    private static final String[] EMPTY = new String[0];
    private final TokenClient tokenClient;
    private final TzClient restClient;
    private int callTimeout;
    private String client_id;
    private String app;
    private String chain;
    private Headers headers = new Headers() {
        @Override
        public String[] headers() {
            return EMPTY;
        }};

    public SimpleRESTClient(final TokenClientFactory tcf, final String tokenURL, final String endpoint, final String[] scope) throws CadiException, LocatorException, APIException {
        callTimeout = Integer.parseInt(tcf.access.getProperty(Config.AAF_CALL_TIMEOUT,Config.AAF_CALL_TIMEOUT_DEF));
        tokenClient = tcf.newClient(tokenURL);
        Result<TimedToken> rtt = tokenClient.getToken(scope);
        if (rtt.isOK()) {
            restClient = tcf.newTzClient(endpoint);

            if ((client_id = tcf.access.getProperty(Config.AAF_APPID, null))==null) {
                if ((client_id = tcf.access.getProperty(Config.CADI_ALIAS, null))==null) {
                    throw new CadiException(Config.AAF_APPID + " or " + Config.CADI_ALIAS + " needs to be defined");
                }
            }
            try {
                restClient.setToken(client_id,rtt.value);
            } catch (IOException e) {
                throw new CadiException(e);
            }
        } else {
            throw new CadiException(rtt.error);
        }
    }

    public SimpleRESTClient timeout(int newTimeout) {
        callTimeout = newTimeout;
        return this;
    }

    //Format:<ID>:<APP>:<protocol>[:AS][,<ID>:<APP>:<protocol>]*
    public SimpleRESTClient endUser(Principal principal) {
        if (principal==null) {
            chain = null;
        } else {
            if (principal instanceof TaggedPrincipal) {
                TaggedPrincipal tp = (TaggedPrincipal)principal;
                chain = tp.getName() + ':' + (app==null?"":app) + ':' + tp.tag() + ":AS";
            } else {
                chain = principal.getName() + (app==null?"":':'+app);
            }
        }
        return this;
    }

    /**
     * Single Threaded Class for building up content
     * @author Instrumental
     *
     */
    public static class Input {
        private static final byte[] EMPTY_STREAM_BYTES = "".getBytes();

        private String content;
        private StringBuilder sb;

        public Input() {
            content = null;
            sb = null;
        }

        public Input(final String content) {
            this.content = content;
        }

        public void set(final String content) {
            this.content = content;
        }

        public PrintWriter writer() {
            return new PrintWriter(new StringBuilderWriter(builder()));
        }

        public StringBuilder builder() {
            if (sb==null) {
                sb = new StringBuilder();
                content = null;
            }
            return sb;
        }

        /**
         * Reuse StringBuilder object
         */
        public void clear() {
            content = null;
            if (sb!=null) {
                sb.setLength(0);
            }
        }

        @Override
        public String toString() {
            if (content!=null) {
                return content;
            } else if (sb!=null) {
                return sb.toString();
            } else {
                return "";
            }
        }

        public byte[] getBytes() {
            byte[] rv;
            if (content==null) {
                if (sb==null) {
                    rv = EMPTY_STREAM_BYTES;
                } else {
                    rv = sb.toString().getBytes();
                }
            } else {
                rv = content.getBytes();
            }
            content = null;
            return rv;
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // CREATE
    //
    /////////////////////////////////////////////////////////////
    public void create(final String path, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        post(path,APPLICATION_JSON, input);
    }

    public void post(final String path, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        post(path,APPLICATION_JSON, input);
    }

    public void post(final String path, final String contentType, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        Future<Void> future = restClient.best(new Retryable<Future<Void>>() {
            @Override
            public Future<Void> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                return client.create(path, contentType, new ETransfer(input));
            }
        });
        if (!future.get(callTimeout)) {
            throw new RESTException(future);
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // READ
    //
    /////////////////////////////////////////////////////////////
    public String read(final String path) throws RESTException, CadiException, LocatorException, APIException  {
        return get(path,APPLICATION_JSON);
    }

    public String get(final String path) throws RESTException, CadiException, LocatorException, APIException  {
        return get(path,APPLICATION_JSON);
    }

    public String get(final String path, final String accepts) throws RESTException, CadiException, LocatorException, APIException  {
        Future<String> future = restClient.best(new Retryable<Future<String>>() {
            @Override
            public Future<String> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                return client.read(path,accepts, headers());
            }
        });
        if (future.get(callTimeout)) {
            return future.value;
        } else {
            throw new RESTException(future);
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // UPDATE
    //
    /////////////////////////////////////////////////////////////

    public String update(final String path, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        return put(path,APPLICATION_JSON, input);
    }

    public String put(final String path, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        return put(path,APPLICATION_JSON, input);
    }

    public String put(final String path, final String contentType, final Input input) throws RESTException, CadiException, LocatorException, APIException  {
        Future<String> future = restClient.best(new Retryable<Future<String>>() {
            @Override
            public Future<String> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                return client.update(path, contentType, new ETransfer(input));
            }
        });
        if (future.get(callTimeout)) {
            return future.value;
        } else {
            throw new RESTException(future);
        }
    }

    /////////////////////////////////////////////////////////////
    //
    // DELETE
    //
    /////////////////////////////////////////////////////////////
    public void delete(final String path) throws RESTException, CadiException, LocatorException, APIException  {
        delete(path,APPLICATION_JSON);
    }

    public void delete(final String path, final String contentType) throws RESTException, CadiException, LocatorException, APIException  {
        Future<Void> future = restClient.best(new Retryable<Future<Void>>() {
            @Override
            public Future<Void> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                return client.delete(path, contentType);
            }
        });
        if (!future.get(callTimeout)) {
            throw new RESTException(future);
        }
    }

    /////////////////////////////////////////////////////////////

    private static class ETransfer implements EClient.Transfer {
        private Input input;
        public ETransfer(final Input input) {
            this.input = input;
        }

        @Override
        public void transfer(OutputStream os) throws IOException, APIException {
            os.write(input.getBytes());
        }
    }

    public interface Headers {
        String[] headers();
    }

    public String[] headers() {
        if (chain==null) {
            return headers.headers();
        } else {
            String[] strs = headers.headers();
            String[] rv = new String[strs.length+2];
            rv[0]=Config.CADI_USER_CHAIN;
            rv[1]=chain;
            for (int i = 0;i<strs.length;++i) {
                rv[i+2]=strs[i];
            }
            return rv;
        }
    }
}
