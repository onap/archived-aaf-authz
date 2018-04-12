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

package org.onap.aaf.cadi.aaf;

import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.Permission;

/**
 * A Class that understands the AAF format of Permission (name/type/action)
 *  or String "name|type|action"
 * 
 * @author Jonathan
 *
 */
public class AAFPermission implements Permission {
	private static final List<String> NO_ROLES;
	protected String type,instance,action,key;
	private List<String> roles;
	
	static {
		NO_ROLES = new ArrayList<String>();
	}

	protected AAFPermission() {roles=NO_ROLES;}

	public AAFPermission(String type, String instance, String action) {
		this.type = type;
		this.instance = instance;
		this.action = action;
		key = type + '|' + instance + '|' + action;
		this.roles = NO_ROLES;

	}
	public AAFPermission(String type, String instance, String action, List<String> roles) {
		this.type = type;
		this.instance = instance;
		this.action = action;
		key = type + '|' + instance + '|' + action;
		this.roles = roles==null?NO_ROLES:roles;
	}
	
	/**
	 * Match a Permission
	 * if Permission is Fielded type "Permission", we use the fields
	 * otherwise, we split the Permission with '|'
	 * 
	 * when the type or action starts with REGEX indicator character ( ! ),
	 * then it is evaluated as a regular expression.
	 * 
	 * If you want a simple field comparison, it is faster without REGEX
	 */
	public boolean match(Permission p) {
		boolean rv;
		String aafType;
		String aafInstance;
		String aafAction;
		if(p instanceof AAFPermission) {
			AAFPermission ap = (AAFPermission)p;
			// Note: In AAF > 1.0, Accepting "*" from name would violate multi-tenancy
			// Current solution is only allow direct match on Type.
			// 8/28/2014 Jonathan - added REGEX ability
			aafType = ap.getName();
			aafInstance = ap.getInstance();
			aafAction = ap.getAction();
		} else {
			// Permission is concatenated together: separated by |
			String[] aaf = p.getKey().split("[\\s]*\\|[\\s]*",3);
			aafType = aaf[0];
			aafInstance = (aaf.length > 1) ? aaf[1] : "*";
			aafAction = (aaf.length > 2) ? aaf[2] : "*";
		}
		return ((type.equals(aafType)) &&
				(PermEval.evalInstance(instance, aafInstance)) &&
				(PermEval.evalAction(action, aafAction)));
	}

	public String getName() {
		return type;
	}
	
	public String getInstance() {
		return instance;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.cadi.Permission#permType()
	 */
	public String permType() {
		return "AAF";
	}

	public List<String> roles() {
		return roles;
	}
	public String toString() {
		return "AAFPermission:\n\tType: " + type + 
				"\n\tInstance: " + instance +
				"\n\tAction: " + action +
				"\n\tKey: " + key;
	}
}
