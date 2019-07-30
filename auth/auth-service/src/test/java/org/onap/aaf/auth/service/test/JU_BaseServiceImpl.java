package org.onap.aaf.auth.service.test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.service.AuthzCassServiceImpl;
import org.onap.aaf.auth.service.mapper.Mapper_2_0;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.org.DefaultOrg;
import org.onap.aaf.org.DefaultOrgIdentity;

import aaf.v2_0.Approvals;
import aaf.v2_0.Certs;
import aaf.v2_0.Delgs;
import aaf.v2_0.Error;
import aaf.v2_0.History;
import aaf.v2_0.Keys;
import aaf.v2_0.Nss;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Request;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;

@RunWith(MockitoJUnitRunner.class)
public abstract class JU_BaseServiceImpl {
	protected AuthzCassServiceImpl<Nss, Perms, Pkey, Roles, Users, UserRoles, Delgs, Certs, Keys, Request, History, Error, Approvals> 
		acsi;
	protected Mapper_2_0 mapper;
	
    @Mock
    protected DefaultOrg org;
    @Mock
    protected DefaultOrgIdentity orgIdentity;
	
    protected HistoryDAO historyDAO = mock(HistoryDAO.class);
    protected CacheInfoDAO cacheInfoDAO = mock(CacheInfoDAO.class);
    protected CachedNSDAO nsDAO = mock(CachedNSDAO.class);
    protected CachedPermDAO permDAO = mock(CachedPermDAO.class);
    protected CachedRoleDAO roleDAO = mock(CachedRoleDAO.class);
    protected CachedUserRoleDAO userRoleDAO = mock(CachedUserRoleDAO.class);
    protected CachedCredDAO credDAO = mock(CachedCredDAO.class);
    protected CachedCertDAO certDAO = mock(CachedCertDAO.class);
    protected LocateDAO locateDAO = mock(LocateDAO.class);
    protected FutureDAO futureDAO = mock(FutureDAO.class);
    protected DelegateDAO delegateDAO = mock(DelegateDAO.class);
    protected ApprovalDAO approvalDAO = mock(ApprovalDAO.class);
	
    @Spy
    protected static PropAccess access = new PropAccess();
    
    @Spy
	protected static AuthzEnv env = new AuthzEnv(access);
	
    @Spy
    protected static AuthzTrans trans = env.newTransNoAvg();
    

    @Spy
    protected Question question = new Question(trans,historyDAO,cacheInfoDAO,nsDAO,permDAO,roleDAO,userRoleDAO,
    		credDAO,certDAO,locateDAO,futureDAO,delegateDAO,approvalDAO);
    
	public void setUp() throws Exception {
	    when(trans.org()).thenReturn(org);
	    when(org.getDomain()).thenReturn("org.onap");
	    Define.set(access);
		access.setProperty(Config.CADI_LATITUDE, "38.0");
		access.setProperty(Config.CADI_LONGITUDE, "-72.0");

	    mapper = new Mapper_2_0(question);
		acsi = new AuthzCassServiceImpl<>(trans, mapper, question);
	}
	
	//////////
	//  Common Data Objects
	/////////
    protected List<NsDAO.Data> nsData(String name) {
    	NsDAO.Data ndd = new NsDAO.Data();
    	ndd.name=name;
    	int dot = name.lastIndexOf('.');
    	if(dot<0) {
    		ndd.parent=".";
    	} else {
    		ndd.parent=name.substring(0,dot);
    	}
    	List<NsDAO.Data> rv = new ArrayList<NsDAO.Data>();
    	rv.add(ndd);
    	return rv;
    }
    
    protected UserRoleDAO.Data urData(String user, String ns, String rname, int days) {
    	UserRoleDAO.Data urdd = new UserRoleDAO.Data();
    	urdd.user = user;
    	urdd.ns = ns;
    	urdd.rname = rname;
    	urdd.role = ns + '.' + rname;
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.add(GregorianCalendar.DAY_OF_YEAR, days);
    	urdd.expires = gc.getTime();
    	return urdd;
    }


    protected <T> List<T> listOf(T t) {
    	List<T> list = new ArrayList<>();
    	list.add(t);
    	return list;
    }
    
    protected <T> List<T> emptyList(Class<T> cls) {
    	return new ArrayList<>();
    }

}
