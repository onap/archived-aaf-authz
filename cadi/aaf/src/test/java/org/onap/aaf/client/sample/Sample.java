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

package org.onap.aaf.client.sample;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.principal.UnAuthPrincipal;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;

public class Sample {
    private static Sample singleton;
    final private AAFConHttp aafcon;
    final private AAFLurPerm aafLur;
    final private AAFAuthn<?> aafAuthn;

    /**
     * This method is to emphasize the importance of not creating the AAFObjects over and over again.
     * @return
     */
    public static Sample singleton() {
        return singleton;
    }

    public Sample(Access myAccess) throws APIException, CadiException, LocatorException {
        aafcon = new AAFConHttp(myAccess);
        aafLur = aafcon.newLur();
        aafAuthn = aafcon.newAuthn(aafLur);
    }

    /**
     * Checking credentials outside of HTTP/S presents fewer options initially. There is not, for instance,
     * the option of using 2-way TLS HTTP/S. 
     *  
     *  However, Password Checks are still useful, and, if the Client Certificate could be obtained in other ways, the 
     *  Interface can be expanded in the future to include Certificates.
     * @throws CadiException 
     * @throws IOException 
     */
    public Principal checkUserPass(String fqi, String pass) throws IOException, CadiException {
        String ok = aafAuthn.validate(fqi, pass);
        if (ok==null) {
            System.out.println("Success!");
            /*
             UnAuthPrincipal means that it is not coming from the official Authorization chain.
             This is useful for Security Plugins which don't use Principal as the tie between
             Authentication and Authorization
        
             You can also use this if you want to check Authorization without actually Authenticating, as may
             be the case with certain Onboarding Tooling.
            */
            return new UnAuthPrincipal(fqi);
        } else {
            System.out.printf("Failure: %s\n",ok);
            return null;
        }
    

    }

    /**
     * An example of looking for One Permission within all the permissions user has.  CADI does cache these,
     * so the call is not expensive.
     *
     * Note: If you are using "J2EE" (Servlets), CADI ties this function to the method: 
     *    HttpServletRequest.isUserInRole(String user)
     *
     *  The J2EE user can expect that his servlet will NOT be called without a Validated Principal, and that
     *  "isUserInRole()" will validate if the user has the Permission designated.
     *  
     */
    public boolean oneAuthorization(Principal fqi, Permission p) {
        return aafLur.fish(fqi, p);
    }

    public List<Permission> allAuthorization(Principal fqi) {
        List<Permission> pond = new ArrayList<>();
        aafLur.fishAll(fqi, pond);
        return pond;
    }


    public static void main(String[] args) {
        // Note: you can pick up Properties from Command line as well as VM Properties
        // Code "user_fqi=... user_pass=..." (where user_pass can be encrypted) in the command line for this sample.
        // Also code "perm=<perm type>|<instance>|<action>" to test a specific Permission
        PropAccess myAccess = new PropAccess(args); 
        try {
            /*
             * NOTE:  Do NOT CREATE new aafcon, aafLur and aafAuthn each transaction.  They are built to be
             * reused!
             *
             * This is why this code demonstrates "Sample" as a singleton.
             */
            singleton = new Sample(myAccess);
            String user = myAccess.getProperty("user_fqi");
            String pass= myAccess.getProperty("user_pass");
        
            if (user==null || pass==null) {
                System.err.println("This Sample class requires properties user_fqi and user_pass");
            } else {
                pass =  myAccess.decrypt(pass, false); // Note, with "false", decryption will only happen if starts with "enc:"
                // See the CODE for Java Methods used
                Principal fqi = Sample.singleton().checkUserPass(user,pass);
            
                if (fqi==null) {
                    System.out.println("OK, normally, you would cease processing for an "
                            + "unauthenticated user, but for the purpose of Sample, we'll keep going.\n");
                    fqi=new UnAuthPrincipal(user);
                }
            
                // AGAIN, NOTE: If your client fails Authentication, the right behavior 99.9%
                // of the time is to drop the transaction.  We continue for sample only.
            
                // note, default String for perm
                String permS = myAccess.getProperty("perm","org.osaaf.aaf.access|*|read");
                String[] permA = Split.splitTrim('|', permS);
                if (permA.length>2) {
                    final Permission perm = new AAFPermission(null, permA[0],permA[1],permA[2]);
                    // See the CODE for Java Methods used
                    if (singleton().oneAuthorization(fqi, perm)) {
                        System.out.printf("Success: %s has %s\n",fqi.getName(),permS);
                    } else {
                        System.out.printf("%s does NOT have %s\n",fqi.getName(),permS);
                    }
                }
            
            
                // Another form, you can get ALL permissions in a list
                // See the CODE for Java Methods used
                List<Permission> permL = singleton().allAuthorization(fqi);
                if (permL.size()==0) {
                    System.out.printf("User %s has no Permissions THAT THE CALLER CAN SEE\n",fqi.getName());
                } else {
                    System.out.print("Success:\n");
                    for (Permission p : permL) {
                        System.out.printf("\t%s has %s\n",fqi.getName(),p.getKey());
                    }
                }
            }
        } catch (APIException | CadiException | LocatorException | IOException e) {
            e.printStackTrace();
        }
    }
}
