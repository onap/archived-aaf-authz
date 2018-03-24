package org.onap.aaf.auth.gui;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

public class OrgLookupFilter implements Filter {
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
		final AuthzTrans trans = (AuthzTrans) req.getAttribute(TransFilter.TRANS_TAG);
		if(req instanceof HttpServletRequest) {
			Principal p = ((HttpServletRequest)req).getUserPrincipal();
			if(p instanceof TaggedPrincipal) {
				((TaggedPrincipal)p).setTagLookup(new TaggedPrincipal.TagLookup() {
					@Override
					public String lookup() throws CadiException {
						Identity id;
						try {
							id = trans.org().getIdentity(trans, p.getName());
							if(id.isFound()) {
								return id.firstName();
							}
						} catch (OrganizationException e) {
							throw new CadiException(e);
						}
						return p.getName();
					}
				});
			}
			fc.doFilter(req, resp);
		}
		
	}


	@Override
	public void destroy() {
	}
}
