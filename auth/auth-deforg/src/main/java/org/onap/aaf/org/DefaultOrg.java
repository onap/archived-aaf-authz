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
 * *
 ******************************************************************************/
package org.onap.aaf.org;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.auth.org.EmailWarnings;
import org.onap.aaf.auth.org.Executor;
import org.onap.aaf.auth.org.Mailer;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.org.Identities.Data;

public class DefaultOrg implements Organization {
    private static final String AAF_DATA_DIR = "aaf_data_dir";
    // Package on Purpose
    final String domain;
    final String atDomain;
    final String realm;

    private final String root_ns;

    private final String NAME;
    private final Set<String> supportedRealms;



    public DefaultOrg(Env env, String realm) throws OrganizationException {

        this.realm = realm;
        supportedRealms=new HashSet<>();
        supportedRealms.add(realm);
        domain=FQI.reverseDomain(realm);
        atDomain = '@'+domain;
        NAME=env.getProperty(realm + ".name","Default Organization");
        root_ns = env.getProperty(Config.AAF_ROOT_NS,Config.AAF_ROOT_NS_DEF);

        try {
            String temp=env.getProperty(realm +".file");
            File fIdentities=null;
            if (temp==null) {
                temp = env.getProperty(AAF_DATA_DIR);
                if (temp!=null) {
                    env.warn().log("Datafile for " + realm + " is not defined. Using default: ",temp+"/identities.dat");
                    File dir = new File(temp);
                    fIdentities=new File(dir,"identities.dat");

                    if (!fIdentities.exists()) {
                        env.warn().log("No",fIdentities.getCanonicalPath(),"exists.  Creating.");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        fIdentities.createNewFile();
                    }

                }
            } else {
                fIdentities = new File(temp);
                if (!fIdentities.exists()) {
                    String dataDir = env.getProperty(AAF_DATA_DIR);
                    if (dataDir!=null) {
                        fIdentities = new File(dataDir,temp);
                    }
                }
            }

            if (fIdentities!=null && fIdentities.exists()) {
                identities = new Identities(fIdentities);
            } else {
                if (fIdentities==null) {
                    throw new OrganizationException("No Identities: set \"" + AAF_DATA_DIR + '"');
                } else {
                    throw new OrganizationException(fIdentities.getCanonicalPath() + " does not exist.");
                }
            }

            File fRevoked=null;
            temp=env.getProperty(getClass().getName()+".file.revoked");
            if(temp==null) {
                temp = env.getProperty(AAF_DATA_DIR);
                if (temp!=null) {
                    File dir = new File(temp);
                    fRevoked=new File(dir,"revoked.dat");
                }
            } else {
                fRevoked = new File(temp);
            }
            if (fRevoked!=null && fRevoked.exists()) {
                revoked = new Identities(fRevoked);
            } else {
                revoked = null;
            }

        } catch (IOException e) {
            throw new OrganizationException(e);
        }
    }

    // Implement your own Delegation System
    static final List<String> NULL_DELEGATES = new ArrayList<>();

    public Identities identities;
    public Identities revoked;
    private boolean dryRun;
    private Mailer mailer;
    public enum Types {Employee, Contractor, Application, NotActive};
    private final static Set<String> typeSet;

    static {
        typeSet = new HashSet<>();
        for (Types t : Types.values()) {
            typeSet.add(t.name());
        }
    }

    private static final EmailWarnings emailWarnings = new DefaultOrgWarnings();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public DefaultOrgIdentity getIdentity(AuthzTrans trans, String id) throws OrganizationException {
        int at = id.indexOf('@');
        return new DefaultOrgIdentity(trans,at<0?id:id.substring(0, at),this);
    }

    /**
     * If the ID isn't in the revoked file, if it exists, it is revoked.
     */
    @Override
    public Date isRevoked(AuthzTrans trans, String key) {
        if(revoked!=null) {
            try {
                revoked.open(trans, DefaultOrgIdentity.TIMEOUT);
                try {
                    Reuse r = revoked.reuse();
                    int at = key.indexOf(domain);
                    String search;
                    if (at>=0) {
                        search = key.substring(0,at);
                    } else {
                        search = key;
                    }
                    Data revokedData = revoked.find(search, r);
                    return revokedData==null?null:new Date();
                } finally {
                    revoked.close(trans);
                }
            } catch (IOException e) {
                trans.error().log(e);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.org.Organization#getEsclaations(org.onap.aaf.auth.env.AuthzTrans, java.lang.String, int)
     */
    @Override
    public List<Identity> getIDs(AuthzTrans trans, String user, int escalate) throws OrganizationException {
        List<Identity> rv = new ArrayList<>();
        int end = Math.min(3,Math.abs(escalate));
        Identity id = null;
        for(int i=0;i<end;++i) {
            if(id==null) {
                id = getIdentity(trans,user);
            } else {
                id = id.responsibleTo();
            }
            if(id==null) {
                break;
            } else {
                rv.add(id);
            }
        }
        return rv;
    }

    // Note: Return a null if found; return a String Message explaining why not found.
    @Override
    public String isValidID(final AuthzTrans trans, final String id) {
        try {
            DefaultOrgIdentity u = getIdentity(trans,id);
            return (u==null||!u.isFound())?id + "is not an Identity in " + getName():null;
        } catch (OrganizationException e) {
            return getName() + " could not lookup " + id + ": " + e.getLocalizedMessage();
        }
    }
    // Possible ID Pattern
    //    private static final Pattern ID_PATTERN=Pattern.compile("([\\w.-]+@[\\w.-]+).{4-13}");
    // Another one: ID_PATTERN = "(a-z[a-z0-9]{5-8}@.*).{4-13}";

    @Override
    public boolean isValidCred(final AuthzTrans trans, final String id) {
        // have domain?
        int at = id.indexOf('@');
        String sid;
        if (at > 0) {
            // Use this to prevent passwords to any but THIS domain.
//            if (!id.regionMatches(at+1, domain, 0, id.length()-at-1)) {
//                return false;
//            }
            sid = id.substring(0,at);
        } else {
            sid = id;
        }
        // We'll validate that it exists, rather than check patterns.

        return isValidID(trans, sid)==null;
        // Check Pattern (if checking existing is too long)
        //        if (id.endsWith(SUFFIX) && ID_PATTERN.matcher(id).matches()) {
        //            return true;
        //        }
        //        return false;
    }

    private static final String SPEC_CHARS = "!@#$%^*-+?/,:;.";
    private static final Pattern PASS_PATTERN=Pattern.compile("(((?=.*[a-z,A-Z])(((?=.*\\d))|(?=.*[" + SPEC_CHARS +"]))).{6,20})");
    /**
     *  (                # Start of group
     *  (?=.*[a-z,A-Z])    #   must contain one character
     *
     *  (?=.*\d)        #   must contain one digit from 0-9
     *        OR
     *  (?=.*[@#$%])    #   must contain one special symbols in the list SPEC_CHARS
     *
     *            .        #     match anything with previous condition checking
     *          {6,20}    #        length at least 6 characters and maximum of 20
     *  )                # End of group
     *
     * Another example, more stringent pattern
     private static final Pattern PASS_PATTERN=Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[" + SPEC_CHARS +"]).{6,20})");
     *  Attribution: from mkyong.com
     *  (                # Start of group
     *  (?=.*\d)        #   must contain one digit from 0-9
     *  (?=.*[a-z])        #   must contain one lowercase characters
     *  (?=.*[A-Z])        #   must contain one uppercase characters
     *  (?=.*[@#$%])    #   must contain one special symbols in the list SPEC_CHARS
     *            .        #     match anything with previous condition checking
     *          {6,20}    #        length at least 6 characters and maximum of 20
     *  )                # End of group
     */
    @Override
    public String isValidPassword(final AuthzTrans trans, final String user, final String password, final String... prev) {
        for (String p : prev) {
            if (password.contains(p)) { // A more sophisticated algorithm might be better.
                return "Password too similar to previous passwords";
            }
        }
        // If you have an Organization user/Password scheme, replace the following
        if (PASS_PATTERN.matcher(password).matches()) {
            return "";
        }
        return "Password does not match " + NAME + " Password Standards";
    }

    private static final String[] rules = new String[] {
            "Passwords must contain letters",
            "Passwords must contain one of the following:",
            "  Number",
            "  One special symbols in the list \""+ SPEC_CHARS + '"',
            "Passwords must be between 6 and 20 chars in length",
    };

    @Override
    public String[] getPasswordRules() {
        return rules;
    }

    @Override
    public Set<String> getIdentityTypes() {
        return typeSet;
    }

    @Override
    public Response notify(AuthzTrans trans, Notify type, String url, String[] identities, String[] ccs, String summary, Boolean urgent) {
        String system = trans.getProperty("CASS_ENV", "");

        ArrayList<String> toList = new ArrayList<>();
        Identity identity;
        if (identities != null) {
            for (String user : identities) {
                try {
                    identity = getIdentity(trans, user);
                    if (identity == null) {
                        trans.error().log(
                                "Failure to obtain User " + user + " for "
                                        + getName());
                    } else {
                        toList.add(identity.email());
                    }
                } catch (Exception e) {
                    trans.error().log(
                            e,
                            "Failure to obtain User " + user + " for "
                                    + getName());
                }
            }
        }

        if (toList.isEmpty()) {
            trans.error().log("No Users listed to email");
            return Response.ERR_NotificationFailure;
        }

        ArrayList<String> ccList = new ArrayList<>();

        // If we're sending an urgent email, CC the user's supervisor
        //
        if (urgent) {
            trans.info().log("urgent msg for: " + identities[0]);
            try {
                List<Identity> supervisors = getApprovers(trans, identities[0]);
                for (Identity us : supervisors) {
                    trans.info().log("supervisor: " + us.email());
                    ccList.add(us.email());
                }
            } catch (Exception e) {
                trans.error().log(e,
                        "Failed to find supervisor for  " + identities[0]);
            }
        }

        if (ccs != null) {
            for (String user : ccs) {
                try {
                    identity = getIdentity(trans, user);
                    ccList.add(identity.email());
                } catch (Exception e) {
                    trans.error().log(
                            e,
                            "Failure to obtain User " + user + " for "
                                    + getName());
                }
            }
        }

        if (summary == null) {
            summary = "";
        }

        switch (type) {
        case Approval:
            try {
                sendEmail(trans, toList, ccList,
                        "AAF Approval Notification "
                                + (system.length() == 0 ? "" : "(ENV: "
                                        + system + ")"),
                        "AAF is the "
                        + NAME
                        + "System for Fine-Grained Authorizations.  You are being asked to Approve"
                                + (system.length() == 0 ? "" : " in the "
                                        + system + " environment")
                                + " before AAF Actions can be taken.\n\n"
                                + "Please follow this link: \n\n\t" + url
                                + "\n\n" + summary, urgent);
            } catch (Exception e) {

                trans.error().log(e, "Failure to send Email");
                return Response.ERR_NotificationFailure;
            }
            break;
        case PasswordExpiration:
            try {
                sendEmail(trans,
                        toList,
                        ccList,
                        "AAF Password Expiration Warning "
                                + (system.length() == 0 ? "" : "(ENV: "
                                        + system + ")"),
                        "AAF is the "
                        + NAME
                        + " System for Authorizations.\n\nOne or more passwords will expire soon or have expired"
                                + (system.length() == 0 ? "" : " in the "
                                        + system + " environment")
                                + ".\n\nPasswords expired for more than 30 days without action are subject to deletion.\n\n"
                                + "Please follow each link to add a New Password with Expiration Date. Either are valid until expiration. "
                                + "Use this time to change the passwords on your system. If issues, reply to this email.\n\n"
                                + summary, urgent);
            } catch (Exception e) {
                trans.error().log(e, "Failure to send Email");
                return Response.ERR_NotificationFailure;
            }
            break;

        case RoleExpiration:
            try {
                sendEmail(
                        trans,
                        toList,
                        ccList,
                        "AAF Role Expiration Warning "
                                + (system.length() == 0 ? "" : "(ENV: "
                                        + system + ")"),
                        "AAF is the "
                        + NAME
                        + " System for Authorizations. One or more roles will expire soon"
                                + (system.length() == 0 ? "" : " in the "
                                        + system + " environment")
                                + ".\n\nRoles expired for more than 30 days are subject to deletion."
                                + "Please follow this link the GUI Command line, and either 'extend' or 'del' the user in the role.\n"
                                + "If issues, reply to this email.\n\n\t" + url
                                + "\n\n" + summary, urgent);
            } catch (Exception e) {
                trans.error().log(e, "Failure to send Email");
                return Response.ERR_NotificationFailure;
            }
            break;
        default:
            return Response.ERR_NotImplemented;
        }
        return Response.OK;
    }


    /**
     * Default Policy is to set to 6 Months for Notification Types.
     * add others/change as required
     */
    @Override
    public Date whenToValidate(Notify type, Date lastValidated) {
        switch(type) {
            case Approval:
            case PasswordExpiration:
                return null;
            default:
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(lastValidated);
                gc.add(GregorianCalendar.MONTH, 6);  // 6 month policy
                return gc.getTime();
        }
    }

    @Override
    public GregorianCalendar expiration(GregorianCalendar gc, Expiration exp, String... extra) {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar rv = gc==null?now:(GregorianCalendar)gc.clone();
        switch (exp) {
            case ExtendPassword:
                // Extending Password give 5 extra days, max 8 days from now
                rv.add(GregorianCalendar.DATE, 5);
                now.add(GregorianCalendar.DATE, 8);
                if (rv.after(now)) {
                    rv = now;
                }
                break;
            case Future:
                // Future requests last 15 days.
                now.add(GregorianCalendar.DATE, 15);
                rv = now;
                break;
            case Password:
                // Passwords expire in 90 days
                now.add(GregorianCalendar.DATE, 90);
                rv = now;
                break;
            case TempPassword:
                // Temporary Passwords last for 12 hours.
                now.add(GregorianCalendar.DATE, 90);
                rv = now;
                break;
            case UserDelegate:
                // Delegations expire max in 2 months, renewable to 3
                rv.add(GregorianCalendar.MONTH, 2);
                now.add(GregorianCalendar.MONTH, 3);
                if (rv.after(now)) {
                    rv = now;
                }
                break;
            case UserInRole:
                // Roles expire in 6 months
                now.add(GregorianCalendar.MONTH, 6);
                rv = now;
                break;
            case RevokedGracePeriodEnds:
            	now.add(GregorianCalendar.DATE, 3);
            	rv = now;
            	break;
            default:
                // Unless other wise set, 6 months is default
                now.add(GregorianCalendar.MONTH, 6);
                rv = now;
                break;
        }
        return rv;
    }

    @Override
    public EmailWarnings emailWarningPolicy() {
        return emailWarnings;
    }

    /**
     * Assume the Supervisor is the Approver.
     */
    @Override
    public List<Identity> getApprovers(AuthzTrans trans, String user) throws OrganizationException {
        Identity orgIdentity = getIdentity(trans, user);
        List<Identity> orgIdentitys = new ArrayList<>();
        if (orgIdentity!=null) {
            Identity supervisor = orgIdentity.responsibleTo();
            if (supervisor!=null) {
                orgIdentitys.add(supervisor);
            }
        }
        return orgIdentitys;
    }

    @Override
    public String getApproverType() {
        return "supervisor";
    }

    @Override
    public int startOfDay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean canHaveMultipleCreds(String id) {
        // External entities are likely mono-password... if you change it, it is a global change.
        // This is great for people, but horrible for Applications.
        //
        // AAF's Password can have multiple Passwords, each with their own Expiration Date.
        // For Default Org, we'll assume true for all, but when you add your external
        // Identity stores, you need to return "false" if they cannot support multiple Passwords like AAF
        return true;
    }

    @Override
    public String validate(AuthzTrans trans, Policy policy, Executor executor, String... vars) throws OrganizationException {
        String user;
        switch(policy) {
            case OWNS_MECHID:
            case CREATE_MECHID:
                if (vars.length>0) {
                    DefaultOrgIdentity thisID = getIdentity(trans,vars[0]);
                    if ("a".equals(thisID.identity.status)) { // MechID
                        DefaultOrgIdentity requestor = getIdentity(trans, trans.user());
                        if (requestor!=null) {
                            Identity mechid = getIdentity(trans, vars[0]);
                            if (mechid!=null) {
                                Identity sponsor = mechid.responsibleTo();
                                if (sponsor!=null && requestor.fullID().equals(sponsor.fullID())) {
                                    return null;
                                } else {
                                    return trans.user() + " is not the Sponsor of MechID " + vars[0];
                                }
                            }
                        }
                    }
                }
                return null;

            case CREATE_MECHID_BY_PERM_ONLY:
                return getName() + " only allows sponsors to create MechIDs";

            case MAY_EXTEND_CRED_EXPIRES:
                // If parm, use it, otherwise, trans
                user = vars.length>1?vars[1]:trans.user();
                return executor.hasPermission(user, root_ns,"password", root_ns , "extend")
                        ?null:user + " does not have permission to extend passwords at " + getName();

            default:
                return policy.name() + " is unsupported at " + getName();
        }
    }

    @Override
    public boolean isTestEnv() {
        return false;
    }

    @Override
    public void setTestMode(boolean dryRun) {
        this.dryRun = dryRun;
    }

    private String extractRealm(final String r) {
        int at;
        if ((at=r.indexOf('@'))>=0) {
            return FQI.reverseDomain(r.substring(at+1));
        }
        return r;
    }
    @Override
    public boolean supportsRealm(final String r) {
        if (r.endsWith(realm)) {
            return true;
        } else {
            String erealm = extractRealm(r);
            for (String sr : supportedRealms) {
                if (erealm.startsWith(sr)) {
                    return true;
                }
            }
        }
        return false;
    }
    
	@Override
	public String supportedDomain(String user) {
		if(user!=null) {
			int after_at = user.indexOf('@')+1;
			if(after_at<user.length()) {
				String ud = FQI.reverseDomain(user);
				if(ud.startsWith(getDomain())) {
					return getDomain();
				}
				for(String s : supportedRealms) {
					if(ud.startsWith(s)) {
						return FQI.reverseDomain(s);
					}
				}
			}
		}
		return null;
	}

    @Override
    public synchronized void addSupportedRealm(final String r) {
        supportedRealms.add(extractRealm(r));
    }

    @Override
    public int sendEmail(AuthzTrans trans, List<String> toList, List<String> ccList, String subject, String body,
            Boolean urgent) throws OrganizationException {
        if (mailer!=null) {
            String mailFrom = mailer.mailFrom();
            List<String> to = new ArrayList<>();
            for (String em : toList) {
                if (em.indexOf('@')<0) {
                    to.add(new DefaultOrgIdentity(trans, em, this).email());
                } else {
                    to.add(em);
                }
            }

            List<String> cc = new ArrayList<>();
            if (ccList!=null) {
                if (!ccList.isEmpty()) {

                    for (String em : ccList) {
                        if (em.indexOf('@')<0) {
                            cc.add(new DefaultOrgIdentity(trans, em, this).email());
                        } else {
                            cc.add(em);
                        }
                    }
                }

                // for now, I want all emails so we can see what goes out. Remove later
                if (!ccList.contains(mailFrom)) {
                    ccList.add(mailFrom);
                }
            }

            return mailer.sendEmail(trans,dryRun?"DefaultOrg":null,to,cc,subject,body,urgent)?0:1;
        } else {
            return 0;
        }
    }
}
