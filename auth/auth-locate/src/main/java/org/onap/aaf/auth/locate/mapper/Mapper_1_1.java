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

package org.onap.aaf.auth.locate.mapper;

import java.util.List;

import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.util.Vars;
import org.onap.aaf.misc.env.util.Split;

import locate.v1_0.Endpoint;
import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;
import locate.v1_1.Configuration;
import locate_local.v1_0.Error;
import locate_local.v1_0.InRequest;
import locate_local.v1_0.Out;

public class Mapper_1_1 implements Mapper<InRequest,Out,Endpoints,MgmtEndpoints,Configuration,Error> {
    
    @Override
    public Class<?> getClass(API api) {
        switch(api) {
            case IN_REQ: return InRequest.class;
            case OUT: return Out.class;
            case ERROR: return Error.class;
            case VOID: return Void.class;
            case ENDPOINTS: return Endpoints.class;
            case MGMT_ENDPOINTS: return MgmtEndpoints.class;
            case CONFIG: return Configuration.class;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A> A newInstance(API api) {
        switch(api) {
            case IN_REQ: return (A) new InRequest();
            case OUT: return (A) new Out();
            case ERROR: return (A)new Error();
            case ENDPOINTS: return (A) new Endpoints();
            case MGMT_ENDPOINTS: return (A) new MgmtEndpoints();
            case CONFIG: return (A) new Configuration();
            case VOID: return null;
        }
        return null;
    }

    //////////////  Mapping Functions /////////////
    @Override
    public locate_local.v1_0.Error errorFromMessage(StringBuilder holder, String msgID, String text,String... var) {
        Error err = new Error();
        err.setMessageId(msgID);
        // AT&T Restful Error Format requires numbers "%" placements
        err.setText(Vars.convert(holder, text, (Object[])var));
        for (String s : var) {
            err.getVariables().add(s);
        }
        return err;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.locate.mapper.Mapper#endpoints(org.onap.aaf.auth.layer.test.Result, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Endpoints> endpoints(Result<List<Data>> resultDB, String version, String other) {
        if (resultDB.notOK()) {
            return Result.err(resultDB);
        }
        int major=-1, minor=-1, patch=-1, pkg=-1;
        if (version!=null) {
            try { 
                String[] v = Split.split('.',version);
                if (v.length>0) {major = Integer.parseInt(v[0]);}
                if (v.length>1) {minor = Integer.parseInt(v[1]);}
                if (v.length>2) {patch = Integer.parseInt(v[2]);}
                if (v.length>3) {pkg   = Integer.parseInt(v[3]);}
            } catch (NumberFormatException e) {
                return Result.err(Result.ERR_BadData,"Invalid Version String " + version);
            }
        }
        Endpoints eps = new Endpoints();
        List<Endpoint> leps = eps.getEndpoint();
        for (Data d : resultDB.value) {
            if ((major<0 || major==d.major) &&
               (minor<0 || minor<=d.minor) &&
               (patch<0 || patch==d.patch) &&
               (pkg<0   || pkg  ==d.pkg)) {
                Endpoint ep = new Endpoint();
                ep.setName(d.name);
                ep.setHostname(d.hostname);
                ep.setPort(d.port);
                ep.setMajor(d.major);
                ep.setMinor(d.minor);
                ep.setPatch(d.patch);
                ep.setPkg(d.pkg);
                ep.setLatitude(d.latitude);
                ep.setLongitude(d.longitude);
                ep.setProtocol(d.protocol);
                for (String s : d.subprotocol(false)) {
                    ep.getSubprotocol().add(s);
                }
                leps.add(ep);
            }
        }
        return Result.ok(eps);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.locate.mapper.Mapper#locateData(locate.v1_0.MgmtEndpoint)
     */
    @Override
    public Data locateData(MgmtEndpoint me) {
        Data data = new Data();
        data.name = me.getName();
        data.port = me.getPort();
        data.hostname = me.getHostname();
        data.major = me.getMajor();
        data.minor = me.getMinor();
        data.patch = me.getPatch();
        data.pkg   = me.getPkg();
        data.latitude = me.getLatitude();
        data.longitude = me.getLongitude();
        data.protocol = me.getProtocol();
        for (String s : me.getSubprotocol()) {
            data.subprotocol(true).add(s);
        }
        return data;
    }

}