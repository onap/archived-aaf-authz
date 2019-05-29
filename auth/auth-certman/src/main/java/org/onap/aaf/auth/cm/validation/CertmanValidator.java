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

package org.onap.aaf.auth.cm.validation;

import java.util.List;

import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.dao.cass.ArtiDAO.Data;
import org.onap.aaf.auth.validation.Validator;

/**
 * Validator
 * Consistently apply content rules for content (incoming)
 * 
 * Note: We restrict content for usability in URLs (because RESTful service), and avoid 
 * issues with Regular Expressions, and other enabling technologies. 
 * @author Jonathan
 *
 */
public class CertmanValidator extends Validator{
    // Repeated Msg fragments
    private static final String MECHID = "mechid";
    private static final String MACHINE = "machine";
    private static final String ARTIFACT_LIST_IS_NULL = "Artifact List is null.";
    private static final String Y = "y.";
    private static final String IES = "ies.";
    private static final String ENTR = " entr";
    private static final String MUST_HAVE_AT_LEAST = " must have at least ";
    private static final String IS_NULL = " is null.";
    private static final String ARTIFACTS_MUST_HAVE_AT_LEAST = "Artifacts must have at least ";

    public CertmanValidator nullBlankMin(String name, List<String> list, int min) {
        if (list==null) {
            msg(name + IS_NULL);
        } else {
            if (list.size()<min) {
                msg(name + MUST_HAVE_AT_LEAST + min + ENTR + (min==1?Y:IES));
            } else {
                for (String s : list) {
                    nullOrBlank("List Item",s);
                }
            }
        }
        return this;
    }

    public CertmanValidator artisRequired(List<ArtiDAO.Data> list, int min) {
        if (list==null) {
            msg(ARTIFACT_LIST_IS_NULL);
        } else {
            if (list.size()<min) {
                msg(ARTIFACTS_MUST_HAVE_AT_LEAST + min + ENTR + (min==1?Y:IES));
            } else {
                for (ArtiDAO.Data a : list) {
                    allRequired(a);
                    if(a.dir!=null && a.dir.startsWith("/tmp")) {
                    	msg("Certificates may not be deployed into /tmp directory (they will be removed at a random time by O/S)");
                    }
                }
            }
        }
        return this;
    }

    public CertmanValidator keys(ArtiDAO.Data add) {
        if (add==null) {
            msg("Artifact is null.");
        } else {
            nullOrBlank(MECHID, add.mechid);
            nullOrBlank(MACHINE, add.machine);
        }
        return this;
    }
    
    private CertmanValidator allRequired(Data a) {
        if (a==null) {
            msg("Artifact is null.");
        } else {
            nullOrBlank(MECHID, a.mechid);
            nullOrBlank(MACHINE, a.machine);
            nullOrBlank("ca",a.ca);
            nullOrBlank("dir",a.dir);
            nullOrBlank("os_user",a.os_user);
            // Note: AppName, Notify & Sponsor are currently not required
        }
        return this;
    }

}
