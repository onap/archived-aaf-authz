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

package org.onap.aaf.cadi.oauth;

import java.security.Principal;
import java.util.List;

import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.lur.LocalPermission;
import org.onap.aaf.cadi.principal.BearerPrincipal;
import org.onap.aaf.misc.env.util.Split;

public class OAuth2Lur implements Lur {
	private TokenMgr tm;

	public OAuth2Lur(TokenMgr tm) {
		this.tm = tm;
	}
	
	@Override
	public Permission createPerm(String p) {
		String[] params = Split.split('|', p);
		if(params.length==3) {
			return new AAFPermission(params[0],params[1],params[2]);
		} else {
			return new LocalPermission(p);
		}
	}

	@Override
	public boolean fish(Principal bait, Permission pond) {
		AAFPermission apond = (AAFPermission)pond;
		OAuth2Principal oap;
		if(bait instanceof OAuth2Principal) {
			oap = (OAuth2Principal)bait; 
		} else {
			// Here is the spot to put in Principal Conversions
			return false;
		}

		TokenPerm tp = oap.tokenPerm();
		if(tp==null) {
		} else {
			for(Permission p : tp.perms()) {
				if(p.match(apond)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void fishAll(Principal bait, List<Permission> permissions) {
		OAuth2Principal oap = (OAuth2Principal)bait;
		TokenPerm tp = oap.tokenPerm();
		if(tp!=null) {
			for(AAFPermission p : tp.perms()) {
				permissions.add(p);
			}
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public boolean handlesExclusively(Permission pond) {
		return false;
	}

	@Override
	public boolean handles(Principal p) {
		if(p!=null && p instanceof BearerPrincipal) {
			return ((BearerPrincipal)p).getBearer()!=null;
		}
		return false;
	}

	@Override
	public void clear(Principal p, StringBuilder report) {
		tm.clear(p,report);
	}

}
