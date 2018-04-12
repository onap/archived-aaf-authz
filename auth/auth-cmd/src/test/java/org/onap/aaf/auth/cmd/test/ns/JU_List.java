package org.onap.aaf.auth.cmd.test.ns;

import static org.junit.Assert.*;

import java.io.Writer;
import java.net.URI;

import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Nss;

import org.onap.aaf.auth.cmd.AAFcli;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class JU_List {
	
	List list;

	@Before
	public void setUp() throws APIException, LocatorException {
		PropAccess prop = new PropAccess();
		AuthzEnv aEnv = new AuthzEnv();
		Writer wtr = mock(Writer.class);
		Locator loc = mock(Locator.class);
		HMangr hman = new HMangr(aEnv, loc);		
		AAFcli aafcli = new AAFcli(prop, aEnv, wtr, hman, null, null);
		NS ns = new NS(aafcli);
		
		list = new List(ns);
	}
	
	@Test
	public void testReport() {
		Future<Nss> fu = mock(Future.class);
		Nss.Ns nss = new Nss.Ns();
		Nss ns = new Nss();
		fu.value = ns;
		fu.value.getNs();
		System.out.print(fu.value.getNs());
		
		list.report(null, "test");
		list.report(fu, "test");
	}

}
