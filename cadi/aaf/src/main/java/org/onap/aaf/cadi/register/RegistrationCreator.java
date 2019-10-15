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
 */

package org.onap.aaf.cadi.register;

import java.net.UnknownHostException;
import java.util.List;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.Defaults;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.cadi.util.Split;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoint.SpecialPorts;
import locate.v1_0.MgmtEndpoints;

public class RegistrationCreator {
    private Access access;

    public RegistrationCreator(Access access) {
        this.access = access;
    }

    public MgmtEndpoints create(final int port) throws CadiException {
        MgmtEndpoints me = new MgmtEndpoints();
        List<MgmtEndpoint> lme = me.getMgmtEndpoint();
        MgmtEndpoint defData = null;
        MgmtEndpoint locate = null;

        try {
            String dot_le;
            String version=null;
            String defProtocol="https";

            RegistrationPropHolder ph = new RegistrationPropHolder(access, port);

            String firstPrivateHostname = null;
            // Now, loop through by Container
            for(String le : Split.splitTrim(',', ph.lcontainer)) {
                if(le.isEmpty()) {
                    dot_le = le;
                } else {
                    dot_le = "."+le;
                }

                for(String entry : Split.splitTrim(',', ph.lentries)) {
                    if(defData==null) {
                        defData = locate = new MgmtEndpoint();

                        version = access.getProperty(Config.AAF_LOCATOR_VERSION, Defaults.AAF_VERSION);
                        locate.setProtocol(defProtocol = access.getProperty(Config.AAF_LOCATOR_PROTOCOL,defProtocol));
                        List<String> ls = locate.getSubprotocol();
                        for(String sp : Split.splitTrim(',', access.getProperty(Config.AAF_LOCATOR_SUBPROTOCOL,""))) {
                            ls.add(sp);
                        }
                        locate.setLatitude(ph.latitude);
                        locate.setLongitude(ph.longitude);

                    } else {
                        locate = copy(defData);
                    }

                    locate.setName(ph.getEntryName(entry,dot_le));
                    /* Cover the situation where there is a Container, and multiple locator Entries,
                     * the first of which is the only real private FQDN
                     * example: oauth
                     *      aaf_locator_entries=oauth,token,introspect
                     *
                     *      Entries for token and introspect, but they point to oauth service.
                     */
                    String locateHostname;
                    if(le.isEmpty()) {
                        locateHostname=ph.getEntryFQDN(entry, dot_le);
                    } else if(firstPrivateHostname==null) {
                        firstPrivateHostname=locateHostname=ph.getEntryFQDN(entry, dot_le);
                    } else {
                        locateHostname=firstPrivateHostname;
                    }

                    locate.setHostname(locateHostname);
                    locate.setPort(ph.getEntryPort(dot_le));

                    String specificVersion = access.getProperty(Config.AAF_LOCATOR_VERSION + dot_le,null);
                    if(specificVersion == null && locate == defData) {
                        specificVersion = version;
                    }
                    if(specificVersion!=null) {
                        String split[] = Split.splitTrim('.', specificVersion);
                        String deply[]= Split.splitTrim('.', access.getProperty(Config.AAF_DEPLOYED_VERSION, ""));
                        locate.setMajor(best(split,deply,0));
                        locate.setMinor(best(split,deply,1));
                        locate.setPatch(best(split,deply,2));
                        locate.setPkg(best(split,deply,3));
                    }

                    String protocol = access.getProperty(Config.AAF_LOCATOR_PROTOCOL + dot_le, defProtocol);
                    if (protocol!=null) {
                        locate.setProtocol(protocol);
                        List<String> ls = locate.getSubprotocol();
                        // ls cannot be null, per generated getSubprotocol code
                        if(ls.isEmpty()) {
                            String subprotocols = access.getProperty(Config.AAF_LOCATOR_SUBPROTOCOL + dot_le, null);
                            if(subprotocols==null) {
                                subprotocols = access.getProperty(Config.CADI_PROTOCOLS, null);
                            }
                            if(subprotocols!=null) {
                                for (String s : Split.split(',', subprotocols)) {
                                    ls.add(s);
                                }
                            } else {
                                access.printf(Level.ERROR, "%s is required for Locator Registration of %s",
                                        Config.AAF_LOCATOR_SUBPROTOCOL,Config.AAF_LOCATOR_PROTOCOL);
                            }
                        }
                        lme.add(locate);
                    } else {
                        access.printf(Level.ERROR, "%s is required for Locator Registration",Config.AAF_LOCATOR_PROTOCOL);
                    }
                }
            }
        } catch (NumberFormatException | UnknownHostException e) {
            throw new CadiException("Error extracting Data from Properties for Registrar",e);
        }

        if(access.willLog(Level.INFO)) {
            access.log(Level.INFO, print(new StringBuilder(),me.getMgmtEndpoint()));
        }
        return me;
    }

    /*
     * Find the best version between Actual Interface and Deployed version
     */
    private int best(String[] split, String[] deploy, int i) {
        StringBuilder sb = new StringBuilder();
        char c;
        String s;
        if(split.length>i) {
            s=split[i];
            for(int j=0;j<s.length();++j) {
                if(Character.isDigit(c=s.charAt(j))) {
                    sb.append(c);
                } else {
                    break;
                }
            }
        }

        if(sb.length()==0 && deploy.length>i) {
            s=deploy[i];
            for(int j=0;j<s.length();++j) {
                if(Character.isDigit(c=s.charAt(j))) {
                    sb.append(c);
                } else {
                    break;
                }
            }
        }

        return sb.length()==0?0:Integer.parseInt(sb.toString());
    }

    private StringBuilder print(StringBuilder sb, List<MgmtEndpoint> lme) {
        int cnt = 0;
        for(MgmtEndpoint m : lme) {
            print(sb,cnt++,m);
        }
        return sb;
    }

    private void print(StringBuilder out, int cnt, MgmtEndpoint mep) {
        out.append("\nManagement Endpoint - ");
        out.append(cnt);
        out.append("\n\tName:       ");
        out.append(mep.getName());
        out.append("\n\tHostname:   ");
        out.append(mep.getHostname());
        out.append("\n\tLatitude:   ");
        out.append(mep.getLatitude());
        out.append("\n\tLongitude:  ");
        out.append(mep.getLongitude());
        out.append("\n\tVersion:    ");
        out.append(mep.getMajor());
        out.append('.');
        out.append(mep.getMinor());
        out.append('.');
        out.append(mep.getPatch());
        out.append('.');
        out.append(mep.getPkg());
        out.append("\n\tPort:       ");
        out.append(mep.getPort());
        out.append("\n\tProtocol:   ");
        out.append(mep.getProtocol());
        out.append("\n\tSpecial Ports:");
        for( SpecialPorts sp : mep.getSpecialPorts()) {
            out.append("\n\t\tName:       ");
            out.append(sp.getName());
            out.append("\n\t\tPort:       ");
            out.append(sp.getPort());
            out.append("\n\t\tProtocol:   ");
            out.append(sp.getProtocol());
            out.append("\n\t\t  Versions: ");
            boolean first = true;
            for(String s : sp.getProtocolVersions()) {
                if(first) {
                    first = false;
                } else {
                    out.append(',');
                }
                out.append(s);
            }
        }
        boolean first = true;
        out.append("\n\tSubProtocol: ");
        for(String s : mep.getSubprotocol()) {
            if(first) {
                first = false;
            } else {
                out.append(',');
            }
            out.append(s);
        }
    }

    private MgmtEndpoint copy(MgmtEndpoint mep) {
        MgmtEndpoint out = new MgmtEndpoint();
        out.setName(mep.getName());
        out.setHostname(mep.getHostname());
        out.setLatitude(mep.getLatitude());
        out.setLongitude(mep.getLongitude());
        out.setMajor(mep.getMajor());
        out.setMinor(mep.getMinor());
        out.setPatch(mep.getPatch());
        out.setPkg(mep.getPkg());
        out.setPort(mep.getPort());
        out.setProtocol(mep.getProtocol());
        out.getSpecialPorts().addAll(mep.getSpecialPorts());
        out.getSubprotocol().addAll(mep.getSubprotocol());
        return out;
    }
}
