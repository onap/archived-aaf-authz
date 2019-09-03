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

package org.onap.aaf.cadi.locator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Access.Level;

public class DNSLocator implements Locator<URI> {
    private static enum Status {UNTRIED, OK, INVALID, SLOW};
    private static final int CHECK_TIME = 3000;
    
    private String host, protocol;
    private Access access;
    private Host[] hosts;
    private int startPort, endPort;
    private String suffix;

    private int size = 1; // initial, until refreshed.
    
    public DNSLocator(Access access, String protocol, String host, String range) {
        this.host = host;
        this.protocol = protocol;
        this.access = access;
        int dash = range.indexOf('-');
        if (dash<0) {
            startPort = endPort = Integer.parseInt(range);
        } else {
            startPort = Integer.parseInt(range.substring(0,dash));
            endPort = Integer.parseInt(range.substring(dash + 1));
        }
        refresh();
    }

    public DNSLocator(Access access, String aaf_locate) throws LocatorException {
        this.access = access;
        if (aaf_locate==null) {
            throw new LocatorException("Null passed into DNSLocator constructor");
        }
        int start, defPort;
        if (aaf_locate.startsWith("https:")) {
            protocol = "https";
            start = 8; // https://
            defPort = 443;
        } else if (aaf_locate.startsWith("http:")) {
            protocol = "http";
            start = 7; // http://
            defPort = 80;
        } else {
            throw new LocatorException("DNSLocator accepts only https or http protocols.  (requested URL " + aaf_locate + ')');
        }
        host = parseHostAndPorts(aaf_locate, start, defPort);
        refresh();
    }

    public static DNSLocator create(Access access, String url) throws LocatorException {
        return new DNSLocator(access, url);
    }

    @Override
    public URI get(Item item) throws LocatorException {
        return hosts[((DLItem)item).cnt].uri;
    }

    @Override
    public boolean hasItems() {
        for (Host h : hosts) {
            if (h.status==Status.OK) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invalidate(Item item) {
        DLItem di = (DLItem)item;
        hosts[di.cnt].status = Status.INVALID;
    }

    @Override
    public Item best() throws LocatorException {
        // not a good "best"
        for (int i=0;i<hosts.length;++i) {
            switch(hosts[i].status) {
                case OK:
                    return new DLItem(i);
                case INVALID:
                    break;
                case SLOW:
                    break;
                case UNTRIED:
                    try {
                        if (hosts[i].ia.isReachable(CHECK_TIME)) {
                            hosts[i].status = Status.OK;
                            return new DLItem(i);
                        }
                    } catch (IOException e) {
                        throw new LocatorException(e);
                    }
                    break;
                default:
                    break;
            }
        }
        throw new LocatorException("No Available URIs for " + host);
    }

    @Override
    public Item first() throws LocatorException {
        return new DLItem(0);
    }

    @Override
    public Item next(Item item) throws LocatorException {
        DLItem di = (DLItem)item;
        if (++di.cnt<hosts.length) {
            return di;
        } else {
            return null;
        }
    }

    @Override
    public boolean refresh() {
        try {
            InetAddress[] ias = InetAddress.getAllByName(host);
            Host[] temp = new Host[ias.length * (1 + endPort - startPort)];
            int cnt = -1;
            for (int j=startPort; j<=endPort; ++j) {
                for (int i=0;i<ias.length;++i) {
                    temp[++cnt] = new Host(ias[i], j, suffix);
                }
            }
            hosts = temp;
            size = temp.length * (endPort-startPort+1);
            return true;
        } catch (Exception e) {
            access.log(Level.ERROR, e);
        }
        return false;
    }
    
    private String parseHostAndPorts(String aaf_locate, int _start, int defaultPort) throws LocatorException {
        int slash, start;
        int colon = aaf_locate.indexOf(':',_start);
        if (colon > 0) {
            host = aaf_locate.substring(_start,colon);
            start = colon + 1;
            int left = aaf_locate.indexOf('[', start);
            if (left > 0) {
                int right = aaf_locate.indexOf(']', left + 1);
                if (right < 0) {
                    throw new LocatorException("Missing closing bracket in DNSLocator constructor.  (requested URL " + aaf_locate + ')');
                } else if (right == (left + 1)) {
                    throw new LocatorException("Missing ports in brackets in DNSLocator constructor.  (requested URL " + aaf_locate + ')');
                }
                int dash = aaf_locate.indexOf('-', left + 1);
                if (dash == (right - 1) || dash == (left + 1)) {
                    throw new LocatorException("Missing ports in brackets in DNSLocator constructor.  (requested URL " + aaf_locate + ')');
                }
                if (dash < 0) {
                    startPort = endPort = Integer.parseInt(aaf_locate.substring(left + 1, right));
                } else {
                    startPort = Integer.parseInt(aaf_locate.substring(left + 1, dash));
                    endPort = Integer.parseInt(aaf_locate.substring(dash + 1, right));
                }
                slash = aaf_locate.indexOf('/', start);
                if(slash>=0) {
                    suffix = aaf_locate.substring(slash);
                }
                
            } else {
                slash = aaf_locate.indexOf('/', start);
                if (slash == start) {
                    throw new LocatorException("Missing port before '/' in DNSLocator constructor.  (requested URL " + aaf_locate + ')');
                }
                if (slash < 0) {
                    startPort = endPort = Integer.parseInt(aaf_locate.substring(start));
                } else {
                    startPort = endPort = Integer.parseInt(aaf_locate.substring(start, slash));
                    suffix = aaf_locate.substring(slash);
                }
            }
        } else {
            slash = aaf_locate.indexOf('/', _start);
            host = slash<_start?aaf_locate.substring(_start):aaf_locate.substring(_start,slash);
            startPort = endPort = defaultPort;
        }
        
        return host;
    }

    private class Host {
        private URI uri;
        private InetAddress ia;
        private Status status;
        
        public Host(InetAddress inetAddress, int port, String suffix) throws URISyntaxException {
            ia = inetAddress;
            uri = new URI(protocol,null,inetAddress.getCanonicalHostName(),port,suffix,null,null);
            status = Status.UNTRIED;
        }
        
        public String toString() {
            return uri.toString() + " - " + status.name();
        }
    }
    
    private class DLItem implements Item {
        public DLItem(int i) {
            cnt = i;
        }

        private int cnt;
    }
    
    public void destroy() {}

    public int size() {
        return size;
    }
}
