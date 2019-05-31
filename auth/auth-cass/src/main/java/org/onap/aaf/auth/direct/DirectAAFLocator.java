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

package org.onap.aaf.auth.direct;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.misc.env.util.Split;

import locate.v1_0.Endpoint;

public class DirectAAFLocator extends AbsAAFLocator<AuthzTrans> {
    private LocateDAO ldao;
    private int major=-1, minor=-1, patch=-1, pkg=-1;
    private AuthzEnv env;
    private final URI uri;

    /**
     * 
     * @param env
     * @param ldao
     * @param key  must be one or more of service, version, other in that order
     * @throws LocatorException 
     */
    public DirectAAFLocator(AuthzEnv env, LocateDAO ldao, String name, String version) throws LocatorException {
        super(env.access(), name, 1000L /* Don't hit DB more than once a second */); 
        this.env = env;
        this.ldao = ldao;
        if (version!=null) {
            try { 
                String[] v = Split.split('.',version);
                if (v.length>0) {major = Integer.parseInt(v[0]);}
                if (v.length>1) {minor = Integer.parseInt(v[1]);}
                if (v.length>2) {patch = Integer.parseInt(v[2]);}
                if (v.length>3) {pkg   = Integer.parseInt(v[3]);}
            } catch (NumberFormatException e) {
                throw new LocatorException("Invalid Version String: " + version);
            }
        }
        
        try {
        	String aaf_url = access.getProperty(Config.AAF_URL, null);
        	if(aaf_url==null) {
        		aaf_url = "https://"+Config.AAF_LOCATE_URL_TAG+"/%NS."+name;
        	}
    		RegistrationPropHolder rph = new RegistrationPropHolder(access,0);
        	aaf_url = rph.replacements(getClass().getSimpleName(),aaf_url, null,null);
        	access.printf(Level.INIT,"Creating DirectAAFLocator to %s",aaf_url);
            uri = new URI(aaf_url);
        } catch (URISyntaxException | UnknownHostException | CadiException e) {
            throw new LocatorException(e);
        }
        myhostname=null;
        myport = 0; 
    }
    
    
    @Override
    public boolean refresh() {
        AuthzTrans trans = env.newTransNoAvg();
        Result<List<Data>> rl = ldao.readByName(trans, name);
        if (rl.isOK()) {
            LinkedList<EP> epl = new LinkedList<>();
            for (Data d : rl.value) {
//                if (myhostname!=null && d.port==myport && d.hostname.equals(myhostname)) {
//                    continue;
//                }
                if ((major<0 || major==d.major) &&
                   (minor<0 || minor<=d.minor) &&
                   (patch<0 || patch==d.patch) &&
                   (pkg<0   || pkg  ==d.pkg)) {
                    Endpoint endpoint = new Endpoint();
                    endpoint.setName(d.name);
                    endpoint.setHostname(d.hostname);
                    endpoint.setPort(d.port);
                    endpoint.setMajor(d.major);
                    endpoint.setMinor(d.minor);
                    endpoint.setPatch(d.patch);
                    endpoint.setPkg(d.pkg);
                    endpoint.setLatitude(d.latitude);
                    endpoint.setLongitude(d.longitude);
                    endpoint.setProtocol(d.protocol);
                    for (String s : d.subprotocol(false)) {
                        endpoint.getSubprotocol().add(s);
                    }
                    
                    try {
                        epl.add(new EP(endpoint,latitude,longitude));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
            Collections.sort(epl);
            replace(epl);
            return true;
        } else {
            access.log(Level.ERROR, rl.errorString());
        }
        return false;
    }

    @Override
    protected URI getURI() {
        return uri;
    }

}
