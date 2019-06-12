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

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.GetCred;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Taf.LifeForm;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon.GetSetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.filter.MapBathConverter;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.CachedBasicPrincipal;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.cadi.taf.basic.BasicHttpTafResp;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;

public class AAFTaf<CLIENT> extends AbsUserCache<AAFPermission> implements HttpTaf {
    private AAFCon<CLIENT> aaf;
    private boolean warn;
    private MapBathConverter mapIds;
    
    public AAFTaf(AAFCon<CLIENT> con, boolean turnOnWarning) {
        super(con.access,con.cleanInterval,con.highCount, con.usageRefreshTriggerCount);
        aaf = con;
        warn = turnOnWarning;
        initMapBathConverter();
    }

    public AAFTaf(AAFCon<CLIENT> con, boolean turnOnWarning, AbsUserCache<AAFPermission> other) {
        super(other);
        aaf = con;
        warn = turnOnWarning;
        initMapBathConverter();

    }
    
    // Note: Needed for Creation of this Object with Generics
    @SuppressWarnings("unchecked")
    public AAFTaf(Connector mustBeAAFCon, boolean turnOnWarning, AbsUserCache<AAFPermission> other) {
        this((AAFCon<CLIENT>)mustBeAAFCon,turnOnWarning,other);
    }

    // Note: Needed for Creation of this Object with Generics
    @SuppressWarnings("unchecked")
    public AAFTaf(Connector mustBeAAFCon, boolean turnOnWarning) {
        this((AAFCon<CLIENT>)mustBeAAFCon,turnOnWarning);
    }

    private void initMapBathConverter() {
        String csvFile = access.getProperty(Config.CADI_BATH_CONVERT, null);
        if(csvFile==null) {
        	mapIds=null;
        } else {
        	try {
				mapIds = new MapBathConverter(access, new CSV(access,csvFile));
				access.log(Level.INIT,"Basic Auth Conversion using",csvFile,"enabled" );
			} catch (IOException | CadiException e) {
				access.log(e,"Bath Map Conversion is not initialized (non fatal)");
			}
        }

    }

    public TafResp validate(final LifeForm reading, final HttpServletRequest req, final HttpServletResponse resp) {
        //TODO Do we allow just anybody to validate?

        // Note: Either Carbon or Silicon based LifeForms ok
        String authz = req.getHeader("Authorization");
        String target = "invalid";
        if (authz != null && authz.startsWith("Basic ")) {
            if (warn&&!req.isSecure()) {
                aaf.access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
            }
            if(mapIds != null) {
            	authz = mapIds.convert(access, authz);
            }

            try {
                final CachedBasicPrincipal bp;
                if (req.getUserPrincipal() instanceof CachedBasicPrincipal) {
                    bp = (CachedBasicPrincipal)req.getUserPrincipal();
                } else {
                    bp = new CachedBasicPrincipal(this,authz,aaf.getRealm(),aaf.userExpires);
                }
                // First try Cache
                final User<AAFPermission> usr = getUser(bp);
                if (usr != null
                    && usr.principal instanceof GetCred
                    && Hash.isEqual(bp.getCred(),((GetCred)usr.principal).getCred())) {
                    return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by cached AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
                }

                Miss miss = missed(bp.getName(), bp.getCred());
                if (miss!=null && !miss.mayContinue()) {
                    return new BasicHttpTafResp(aaf.access,bp.getName(),buildMsg(bp,req,
                            "User/Pass Retry limit exceeded"), 
                            RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),true);
                }
                
                return aaf.bestForUser(
                    new GetSetter() {
                        @Override
                        public <CL> SecuritySetter<CL> get(AAFCon<CL> con) throws CadiException {
                            return con.basicAuthSS(bp);
                        }
                    },new Retryable<BasicHttpTafResp>() {
                        @Override
                        public BasicHttpTafResp code(Rcli<?> client) throws CadiException, APIException {
                            Future<String> fp = client.read("/authn/basicAuth", "text/plain");
                            if (fp.get(aaf.timeout)) {
                                if (usr!=null) {
                                    usr.principal = bp;
                                } else {
                                    addUser(new User<AAFPermission>(bp,aaf.userExpires));
                                }
                                return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
                            } else {
                                // Note: AddMiss checks for miss==null, and is part of logic
                                boolean rv= addMiss(bp.getName(),bp.getCred());
                                if (rv) {
                                    return new BasicHttpTafResp(aaf.access,bp.getName(),buildMsg(bp,req,
                                            "user/pass combo invalid via AAF from " + req.getRemoteAddr()), 
                                            RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),true);
                                } else {
                                    return new BasicHttpTafResp(aaf.access,bp.getName(),buildMsg(bp,req,
                                            "user/pass combo invalid via AAF from " + req.getRemoteAddr() + " - Retry limit exceeded"), 
                                            RESP.FAIL,resp,aaf.getRealm(),true);
                                }
                            }
                        }
                    }
                );
            } catch (IOException e) {
                String msg = buildMsg(null,req,"Invalid Auth Token");
                aaf.access.log(Level.WARN,msg,'(', e.getMessage(), ')');
                return new BasicHttpTafResp(aaf.access,target,msg, RESP.TRY_AUTHENTICATING, resp, aaf.getRealm(),true);
            } catch (Exception e) {
                String msg = buildMsg(null,req,"Authenticating Service unavailable");
                try {
                    aaf.invalidate();
                } catch (CadiException e1) {
                    aaf.access.log(e1, "Error Invalidating Client");
                }
                aaf.access.log(Level.WARN,msg,'(', e.getMessage(), ')');
                return new BasicHttpTafResp(aaf.access,target,msg, RESP.FAIL, resp, aaf.getRealm(),false);
            }
        }
        return new BasicHttpTafResp(aaf.access,target,"Requesting HTTP Basic Authorization",RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),false);
    }
    
    private String buildMsg(Principal pr, HttpServletRequest req, Object... msg) {
        StringBuilder sb = new StringBuilder();
        for (Object s : msg) {
            sb.append(s.toString());
        }
        if (pr!=null) {
            sb.append(" for ");
            sb.append(pr.getName());
        }
        sb.append(" from ");
        sb.append(req.getRemoteAddr());
        sb.append(':');
        sb.append(req.getRemotePort());
        return sb.toString();
    }


    
    public Resp revalidate(CachedPrincipal prin, Object state) {
        //  !!!! TEST THIS.. Things may not be revalidated, if not BasicPrincipal
        if (prin instanceof BasicPrincipal) {
            Future<String> fp;
            try {
                Rcli<CLIENT> userAAF = aaf.client().forUser(aaf.transferSS((BasicPrincipal)prin));
                fp = userAAF.read("/authn/basicAuth", "text/plain");
                return fp.get(aaf.timeout)?Resp.REVALIDATED:Resp.UNVALIDATED;
            } catch (Exception e) {
                aaf.access.log(e, "Cannot Revalidate",prin.getName());
                return Resp.INACCESSIBLE;
            }
        }
        return Resp.NOT_MINE;
    }

}
