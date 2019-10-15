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

package org.onap.aaf.auth.locate.validation;

import org.onap.aaf.auth.validation.Validator;

import locate.v1_0.Endpoint;
import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoint.SpecialPorts;
import locate.v1_0.MgmtEndpoints;

/**
 * Validator
 * Consistently apply content rules for content (incoming)
 *
 * Note: We restrict content for usability in URLs (because RESTful service), and avoid 
 * issues with Regular Expressions, and other enabling technologies. 
 * @author Jonathan
 *
 */
public class LocateValidator extends Validator {
    private LocateValidator endpoint_key(Endpoint e) {
        if (e==null) {
            msg("Endpoint Data is null.");
        } else {
            nullOrBlank("Endpoint Name", e.getName());
            if (e.getName()!=null) {
                int idx = e.getName().indexOf('.');
                if (idx<=0) {
                    msg("Endpoint Name (" + e.getName() + ") must prefixed by Namespace");
                }
            }
            nullOrBlank("Endpoint Hostname", e.getHostname());
            intRange("Endpoint Port",e.getPort(),0,1000000);
        }
        return this;
    }


    public LocateValidator endpoint(Endpoint e) {
        endpoint_key(e);
        if (e!=null) {
            intRange("Endpoint Major Version",e.getMajor(),0,2000);
            intRange("Endpoint Minor Version",e.getMinor(),0,2000);
            intRange("Endpoint Patch Version",e.getPatch(),0,2000);
            intRange("Endpoint Pkg Version",e.getPkg(),0,2000);
            floatRange("Endpoint Latitude",e.getLatitude(),-90f,90f);
            floatRange("Endpoint Longitude",e.getLongitude(),-180f,180f);
            nullOrBlank("Endpoint Protocol", e.getProtocol());
            for (String s : e.getSubprotocol()) {
                nullOrBlank("Endpoint Subprotocol", s);
            }
        }
        return this;
    }

    public LocateValidator endpoints(Endpoints e, boolean emptyNotOK) {
        if (e==null) {
            msg("Endpoints Data is null.");
        } else {
            if (emptyNotOK && e.getEndpoint().size()==0) {
                msg("Endpoints contains no endpoints");
            } else {
                for (Endpoint ep : e.getEndpoint()) {
                    endpoint(ep);
                }
            }
        }
        return this;
    }

    public LocateValidator mgmt_endpoint_key(MgmtEndpoints meps) {
        if (meps==null) {
            msg("MgmtEndpoints Data is null.");
        } else {
            for (MgmtEndpoint ep : meps.getMgmtEndpoint()) {
                endpoint_key(ep);
            }
        }
        return this;
    }

    public LocateValidator mgmt_endpoints(MgmtEndpoints me, boolean emptyOK) {
        if (me==null) {
            msg("MgmtEndpoints Data is null.");
        } else {
            if (!emptyOK && me.getMgmtEndpoint().size()==0) {
                msg("MgmtEndpoints contains no data");
            } else {
                for (MgmtEndpoint ep : me.getMgmtEndpoint()) {
                    mgmt_endpoint(ep);
                }
            }
        }
        return this;
    }

    private LocateValidator mgmt_endpoint(MgmtEndpoint ep) {
        endpoint(ep);
        for (SpecialPorts sp : ep.getSpecialPorts()) {
            specialPorts(sp);
        }
        return this;
    }

    private LocateValidator specialPorts(SpecialPorts sp) {
        if (sp==null) {
            msg("Special Ports is null.");
        } else {
            nullOrBlank("Special Port Name",sp.getName());
            nullOrBlank("Special Port Protocol",sp.getProtocol());
            intRange("Special Port",sp.getPort(),0,1000000);
        
            for (String s : sp.getProtocolVersions()) {
                nullOrBlank("Special Port Protocol Version", s);
            }
        }
        return this;
    }

}
