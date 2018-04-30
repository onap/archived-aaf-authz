package org.onap.aaf.auth.fs.test;

import static org.junit.Assert.*;

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.fs.AAF_FS;
import org.onap.aaf.auth.rserv.CachingFileAccess;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class JU_AAF_FS {
	AuthzEnv aEnv;
	AAF_FS aafFs;
	File fService;
	File fEtc;
	String value;
	File d;
	private static final String testDir = "src/test/resources/logs";
	
	@Before
	public void setUp() throws APIException, IOException, CadiException {
		value = System.setProperty(Config.CADI_LOGDIR, testDir);
		System.setProperty(Config.CADI_ETCDIR, testDir);
		System.out.println(ClassLoader.getSystemResource("org.osaaf.log4j.props"));
		d = new File(testDir);
		d.mkdirs();
		fService = new File(d +"/fs-serviceTEST.log");
		fService.createNewFile();
		fEtc = new File(d + "/org.osaaf.log4j.props");
		fEtc.createNewFile();
		
		aEnv = new AuthzEnv();
		aEnv.staticSlot("test");
		aEnv.access().setProperty("aaf_public_dir", "test");
		aEnv.access().setProperty(Config.AAF_COMPONENT, "aaf_com:po.nent");
		aafFs = new AAF_FS(aEnv);
		
	}

	@Test
	public void testMain() {
		String[] strArr = {"AAF_LOG4J_PREFIX"};
		
		aafFs.main(strArr);
	}
	
	@After
	public void cleanUp() {
		for(File f : d.listFiles()) {
			f.delete();
		}
		d.delete();
	}

}
