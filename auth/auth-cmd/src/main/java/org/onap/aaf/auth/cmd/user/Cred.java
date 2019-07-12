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

package org.onap.aaf.auth.cmd.user;

import java.util.List;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.aaf.client.ErrMessage;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.CredRequest;
import aaf.v2_0.Error;

public class Cred extends Cmd {
    public static final String ATTEMPT_FAILED_SPECIFICS_WITHELD = "Attempt Failed.  Specifics witheld.";
    private static final String CRED_PATH = "/authn/cred";
    private static final String[] options = {"add","del","reset","extend"/*,"clean"*/};
	private ErrMessage em;
//	private RosettaDF<Error> errDF;
    public Cred(User parent) throws APIException {
        super(parent,"cred",
                new Param(optionsToString(options),true),
                new Param("id",true),
                new Param("password (! D|E)",false),
                new Param("entry# (if multi)",false)
        );
        em = new ErrMessage(aafcli.env());
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException { 
        int idx = _idx;
        String key = args[idx++];
        final int option = whichOption(options,key);

        final CredRequest cr = new CredRequest();
        cr.setId(args[idx++]);
        if (option!=1 && option!=3) {
            if (idx>=args.length) throw new CadiException("Password Required");
            cr.setPassword(args[idx++]);
        }
        if (args.length>idx) {
            cr.setEntry(args[idx]);
        }
        
        // Set Start/End commands
        setStartEnd(cr);
        Integer ret = same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<CredRequest> fp=null;
                String verb =null;
                switch(option) {
                    case 0:
                        fp = client.create(
                            CRED_PATH, 
                            getDF(CredRequest.class), 
                            cr
                            );
                        verb = "Added Credential [";
                        break;
                    case 1:
                        setQueryParamsOn(client);
                        fp = client.delete(CRED_PATH,
                            getDF(CredRequest.class),
                            cr
                            );
                        verb = "Deleted Credential [";
                        break;
                    case 2:
                        fp = client.update(
                            CRED_PATH,
                            getDF(CredRequest.class),
                            cr
                            );
                        verb = "Reset Credential [";
                        break;
                    case 3:
                        fp = client.update(
                            CRED_PATH+"/5",
                            getDF(CredRequest.class),
                            cr
                            );
                        verb = "Extended Credential [";
                        break;
                    default:
                        break;
                }
                if (fp==null) {
                    return null; // get by Sonar check.
                }
                if (fp.get(AAFcli.timeout())) {
                    pw().print(verb);
                    pw().print(cr.getId());
                    pw().println(']');
                } else if (fp.code()==202) {
                        pw().println("Credential Action Accepted, but requires Approvals before actualizing");
                } else if (fp.code()==300 || fp.code()==406) {
                	Error err = em.getError(fp);
                	String text = err.getText();
                	List<String> vars = err.getVariables();
                	
                	// IMPORTANT! We do this backward, because it is looking for string
                	// %1 or %13.  If we replace %1 first, that messes up %13
                	for(int i=vars.size()-1;i>0;--i) {
                		text = text.replace("%"+(i+1), (i<10?" ":"") + i+") " + vars.get(i));
                	}

                	text = text.replace("%1",vars.get(0));
                	pw().println(text);
                } else if (fp.code()==406 && option==1) {
                        pw().println("You cannot delete this Credential");
                } else if (fp.code()==409 && option==0) {
                    pw().println("You cannot add two Passwords for same day");
                } else {
                    pw().println(ATTEMPT_FAILED_SPECIFICS_WITHELD);
                }
                return fp.code();
            }
        });
        if (ret==null)ret = -1;
        return ret;
    }
    
    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,"Add, Delete or Reset Credential");
        indent+=2;
        detailLine(sb,indent,"id       - the ID to create/delete/reset within AAF");
        detailLine(sb,indent,"password - Company Policy compliant Password (not required for Delete)");
        detailLine(sb,indent,"entry    - selected option when deleting/resetting a cred with multiple entries");
        sb.append('\n');
        detailLine(sb,indent,"The Domain can be related to any Namespace you have access to *");
        detailLine(sb,indent,"The Domain is in reverse order of Namespace, i.e. ");
        detailLine(sb,indent+2,"NS of com.att.myapp can create user of XY1234@myapp.att.com");
        sb.append('\n');
        detailLine(sb,indent,"NOTE: AAF does support multiple creds with the same ID. Check with your org if you");
        detailLine(sb,indent+2,"have this implemented. (For example, this is implemented for MechIDs at AT&T)");
        sb.append('\n');            
        detailLine(sb,indent,"*NOTE: com.att.csp is a reserved Domain for Global Sign On");

        detailLine(sb,indent,"Delegates can be listed by the User or by the Delegate");
        indent-=2;
        api(sb,indent,HttpMethods.POST,"authn/cred",CredRequest.class,true);
        api(sb,indent,HttpMethods.DELETE,"authn/cred",CredRequest.class,false);
        api(sb,indent,HttpMethods.PUT,"authn/cred",CredRequest.class,false);
    }
}
