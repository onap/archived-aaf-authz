/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.authz.cm.validation;

import java.util.List;

import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.ArtiDAO;
import com.att.dao.aaf.cass.ArtiDAO.Data;

/**
 * Validator
 * Consistently apply content rules for content (incoming)
 * 
 * Note: We restrict content for usability in URLs (because RESTful service), and avoid 
 * issues with Regular Expressions, and other enabling technologies. 
 *
 */
public class Validator {
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
	private StringBuilder msgs;

	public Validator nullOrBlank(String name, String str) {
		if(str==null) {
			msg(name + IS_NULL);
		} else if(str.length()==0) {
			msg(name + " is blank.");
		}
		return this;
	}
	
	private void msg(String ... strs) {
		if(msgs==null) {
			msgs=new StringBuilder();
		}
		for(String str : strs) {
			msgs.append(str);
		}
		msgs.append('\n');
	}
	
	public boolean err() {
		return msgs!=null;
	}
	
	public String errs() {
		return msgs.toString();
	}

	public Validator notOK(Result<?> res) {
		if(res==null) {
			msgs.append("Result object is blank");
		} else if(res.notOK()) {
			msgs.append(res.getClass().getSimpleName() + " is not OK");
		}
		return this;
	}

	public Validator isNull(String name, Object obj) {
		if(obj==null) {
			msg(name + IS_NULL);
		} 
		return this;
	}

	public Validator nullBlankMin(String name, List<String> list, int min) {
		if(list==null) {
			msg(name + IS_NULL);
		} else {
			if(list.size()<min) {
				msg(name + MUST_HAVE_AT_LEAST + min + ENTR + (min==1?Y:IES));
			} else {
				for(String s : list) {
					nullOrBlank("List Item",s);
				}
			}
		}
		return this;
	}

	public Validator artisRequired(List<ArtiDAO.Data> list, int min) {
		if(list==null) {
			msg(ARTIFACT_LIST_IS_NULL);
		} else {
			if(list.size()<min) {
				msg(ARTIFACTS_MUST_HAVE_AT_LEAST + min + ENTR + (min==1?Y:IES));
			} else {
				for(ArtiDAO.Data a : list) {
					allRequired(a);
				}
			}
		}
		return this;
	}

	public Validator artisKeys(List<ArtiDAO.Data> list, int min) {
		if(list==null) {
			msg(ARTIFACT_LIST_IS_NULL);
		} else {
			if(list.size()<min) {
				msg(ARTIFACTS_MUST_HAVE_AT_LEAST + min + ENTR + (min==1?Y:IES));
			} else {
				for(ArtiDAO.Data a : list) {
					keys(a);
				}
			}
		}
		return this;
	}


	public Validator keys(ArtiDAO.Data add) {
		if(add==null) {
			msg("Artifact is null.");
		} else {
			nullOrBlank(MECHID, add.mechid);
			nullOrBlank(MACHINE, add.machine);
		}
		return this;
	}
	
	private Validator allRequired(Data a) {
		if(a==null) {
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
