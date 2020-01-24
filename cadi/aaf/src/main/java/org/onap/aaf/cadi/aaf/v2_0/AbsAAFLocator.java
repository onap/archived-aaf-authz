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

package org.onap.aaf.cadi.aaf.v2_0;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.cadi.routing.GreatCircle;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Split;

import locate.v1_0.Endpoint;

public abstract class AbsAAFLocator<TRANS extends Trans> implements Locator<URI> {
    protected static final SecureRandom sr = new SecureRandom();
    private static LocatorCreator locatorCreator;
    protected final Access access;

    protected final double latitude;
    protected final double longitude;
    protected List<EP> epList;
    protected final String name, version;
    private String pathInfo = null;
    private String query = null;
    private String fragment = null;
    private boolean additional = false;
    protected String myhostname;
    protected int myport;
    protected final String aaf_locator_host;
    protected URI aaf_locator_uri;
    private long earliest;
    private final long refreshWait;


    public AbsAAFLocator(Access access, String name, final long refreshMin) throws LocatorException {
        RegistrationPropHolder rph;
        try {
            rph = new RegistrationPropHolder(access, 0);
        } catch (UnknownHostException | CadiException e1) {
            throw new LocatorException(e1);
        }
        URI aaf_locator_uri;
        try {
            aaf_locator_host = rph.replacements(getClass().getSimpleName(),"https://"+Config.AAF_LOCATE_URL_TAG,null,null);
            if(aaf_locator_host.endsWith("/locate")) {
                aaf_locator_uri = new URI(aaf_locator_host);
            } else {
                aaf_locator_uri = new URI(aaf_locator_host+"/locate");
            }
            access.printf(Level.INFO, "AbsAAFLocator AAF URI is %s",aaf_locator_uri);
        } catch (URISyntaxException e) {
            throw new LocatorException(e);
        }

        name = rph.replacements(getClass().getSimpleName(),name, null,null);
        access.printf(Level.INFO, "AbsAAFLocator name is %s",aaf_locator_uri);

        epList = new LinkedList<>();
        refreshWait = refreshMin;

        this.access = access;
        String lat = access.getProperty(Config.CADI_LATITUDE,null);
        String lng = access.getProperty(Config.CADI_LONGITUDE,null);
        if (lat==null || lng==null) {
            throw new LocatorException(Config.CADI_LATITUDE + " and " + Config.CADI_LONGITUDE + " properties are required.");
        } else {
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
        }


        if (name.startsWith("http")) { // simple URL
            this.name = name;
            this.version = access.getProperty(Config.AAF_API_VERSION,Config.AAF_DEFAULT_API_VERSION);
        } else {
            String[] split = Split.split(':', name);
            this.name = split[0];
            this.version = (split.length > 1) ? split[1] : access.getProperty(Config.AAF_API_VERSION,Config.AAF_DEFAULT_API_VERSION);
        }
    }

    /**
     * This is the way to setup specialized AAFLocators ahead of time.
     * @param preload
     */
    public static void setCreator(LocatorCreator lc) {
        locatorCreator = lc;
    }

    public static Locator<URI> create(final String name, final String version) throws LocatorException {
        if(locatorCreator==null) {
            throw new LocatorException("LocatorCreator is not set");
        }
        return locatorCreator.create(name, version);
    }

    public interface LocatorCreator {
        public AbsAAFLocator<?> create(String key, String version) throws LocatorException;
        public void setSelf(String hostname, int port);
    }

    protected static String nameFromLocatorURI(URI locatorURI) {
        String[] path = Split.split('/', locatorURI.getPath());
        if (path.length>1 && "locate".equals(path[1])) {
           return path[2];
        } else if(path.length>1) {
             return path[1];
        } else {
            return locatorURI.toString();
        }
    }

    /**
     * Setting "self" excludes this service from the list.  Critical for contacting peers.
     */
    public void setSelf(final String hostname, final int port) {
        myhostname=hostname;
        myport=port;
    }


    public static void setCreatorSelf(final String hostname, final int port) {
        if (locatorCreator!=null) {
            locatorCreator.setSelf(hostname,port);
        }
    }

    protected final synchronized void replace(List<EP> list) {
        epList = list;
    }

    /**
     * Call _refresh as needed during calls, but actual refresh will not occur if there
     * are existing entities or if it has been called in the last 10 (settable) seconds.
     * Timed Refreshes happen by Scheduled Thread
     */
    private final boolean _refresh() {
        boolean rv = false;
        long now=System.currentTimeMillis();
        if (noEntries()) {
            if (earliest<now) {
                synchronized(epList) {
                    rv = refresh();
                    earliest = now + refreshWait; // call only up to 10 seconds.
                }
            } else {
                access.log(Level.ERROR, "Must wait at least " + refreshWait/1000 + " seconds for Locator Refresh");
            }
        }
        return rv;
    }

    private boolean noEntries() {
        return epList.isEmpty();
    }

    @Override
    public URI get(Item item) throws LocatorException {
        if (item==null) {
            return null;
        } else if (item instanceof AAFLItem) {
            return getURI(((AAFLItem)item).uri);
        } else {
            throw new LocatorException(item.getClass().getName() + " does not belong to AAFLocator");
        }
    }

    @Override
    public boolean hasItems() {
        boolean isEmpty = epList.isEmpty();
        if (!isEmpty) {
            for (Iterator<EP> iter = epList.iterator(); iter.hasNext(); ) {
                EP ep = iter.next();
                if (ep.valid) {
                    return true;
                }
            }
            isEmpty = true;
        }
        if (_refresh()) { // is refreshed... check again
            isEmpty = epList.isEmpty();
        }
        return !isEmpty;
    }

    @Override
    public void invalidate(Item item) throws LocatorException {
        if (item!=null) {
            if (item instanceof AAFLItem) {
                AAFLItem ali =(AAFLItem)item;
                EP ep = ali.ep;
                synchronized(epList) {
                    epList.remove(ep);
                }
                ep.invalid();
                ali.iter = getIterator(); // for next guy... fresh iterator
            } else {
                throw new LocatorException(item.getClass().getName() + " does not belong to AAFLocator");
            }
        }
    }

    @Override
    public Item best() throws LocatorException {
        if (!hasItems()) {
            throw new LocatorException(String.format("No Entries found for '%s/%s:%s'",
                    (aaf_locator_uri==null?(aaf_locator_host+"/locate"):aaf_locator_uri.toString()),
                    name,
                    version));
        }
        List<EP> lep = new ArrayList<>();
        EP first = null;
        // Note: Deque is sorted on the way by closest distance
        Iterator<EP> iter = getIterator();
        EP ep;
        while (iter.hasNext()) {
            ep = iter.next();
            if (ep.valid) {
                if (first==null) {
                    first = ep;
                    lep.add(first);
                } else {
                    if (Math.abs(ep.distance-first.distance)<.1) { // allow for nearby/precision issues.
                        lep.add(ep);
                    } else {
                        break;
                    }
                }
            }
        }
        switch(lep.size()) {
            case 0:
                return null;
            case 1:
                return new AAFLItem(iter,first);
            default:
                int rand = sr.nextInt(); // Sonar chokes without.
                int i = Math.abs(rand)%lep.size();
                if (i<0) {
                    return null;
                } else {
                    return new AAFLItem(iter,lep.get(i));
                }

        }
    }

    private Iterator<EP> getIterator() {
        Object[] epa = epList.toArray();
        if (epa.length==0) {
            _refresh();
            epa = epList.toArray();
        }
        return new EPIterator(epa, epList);
    }

    public class EPIterator implements Iterator<EP> {
        private final Object[] epa;
        private final List<EP> epList;
        private int idx;

        public EPIterator(Object[] epa, List<EP> epList) {
            this.epa = epa;
            this.epList = epList;
            idx = epa.length>0?0:-1;
        }

        @Override
        public boolean hasNext() {
            if (idx<0) {
                return false;
            } else {
                Object obj;
                while (idx<epa.length) {
                    if ((obj=epa[idx])==null || !((EP)obj).valid) {
                        ++idx;
                        continue;
                    }
                    break;
                }
                return idx<epa.length;
            }
        }

        @Override
        public EP next() {
            if (!hasNext() ) {
                throw new NoSuchElementException();
            }
            return (EP)epa[idx++];
        }

        @Override
        public void remove() {
            if (idx>=0 && idx<epa.length) {
                synchronized(epList) {
                    epList.remove(epa[idx]);
                }
            }
        }
    }

    @Override
    public Item first()  {
        Iterator<EP> iter = getIterator();
        EP ep = AAFLItem.next(iter);
        if (ep==null) {
            return null;
        }
        return new AAFLItem(iter,ep);
    }

    @Override
    public Item next(Item prev) throws LocatorException {
        if (prev==null) {
            StringBuilder sb = new StringBuilder("Locator Item passed in next(item) is null.");
            int lines = 0;
            for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                sb.append("\n\t");
                sb.append(st.toString());
                if (++lines > 5) {
                    sb.append("\n\t...");
                    break;
                }
            }
            access.log(Level.ERROR, sb);
        } else {
            if (prev instanceof AAFLItem) {
                AAFLItem ali = (AAFLItem)prev;
                EP ep = AAFLItem.next(ali.iter);
                if (ep!=null) {
                    return new AAFLItem(ali.iter,ep);
                }
            } else {
                throw new LocatorException(prev.getClass().getName() + " does not belong to AAFLocator");
            }
        }
        return null;
    }

    protected static class AAFLItem implements Item {
            private Iterator<EP> iter;
            private URI uri;
            private EP ep;

            public AAFLItem(Iterator<EP> iter, EP ep) {
                this.iter = iter;
                this.ep = ep;
                uri = ep.uri;
            }

            private static EP next(Iterator<EP> iter) {
                EP ep=null;
                while (iter.hasNext() && (ep==null || !ep.valid)) {
                    ep = iter.next();
                }
                return ep;
            }

            public String toString() {
                return ep==null?"Locator Item Invalid":ep.toString();
            }
        }

    protected static class EP implements Comparable<EP> {
        private URI uri;
        private final double distance;
        private boolean valid;

        public EP(final Endpoint ep, double latitude, double longitude) throws URISyntaxException {
            uri = new URI(ep.getProtocol(),null,ep.getHostname(),ep.getPort(),null,null,null);
            distance = GreatCircle.calc(latitude, longitude, ep.getLatitude(), ep.getLongitude());
            valid = true;
        }

        public void invalid() {
            valid = false;
        }

        @Override
        public int compareTo(EP o) {
            if (distance<o.distance) {
                return -1;
            } else if (distance>o.distance) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return distance + ": " + uri + (valid?" valid":" invalidate");
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Locator#destroy()
     */
    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public String toString() {
        return "AAFLocator for " + name + " on " + getURI();
    }

    public AbsAAFLocator<TRANS> setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
        additional=true;
        return this;
    }

    public AbsAAFLocator<TRANS> setQuery(String query) {
        this.query = query;
        additional=true;
        return this;
    }

    public AbsAAFLocator<TRANS>  setFragment(String fragment) {
        this.fragment = fragment;
        additional=true;
        return this;
    }

    // Core URI, for reporting purposes
    protected abstract URI getURI();

    protected URI getURI(URI rv) throws LocatorException {
        if (additional) {
            try {
                return new URI(rv.getScheme(),rv.getUserInfo(),rv.getHost(),rv.getPort(),pathInfo,query,fragment);
            } catch (URISyntaxException e) {
                throw new LocatorException("Error copying URL", e);
            }
        }
        return rv;
    }

    protected void clear() {
        epList.clear();
        earliest=0L;
    }


}
