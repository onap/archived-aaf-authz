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

package org.onap.aaf.auth.org;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.env.AuthzTrans;

/**
 * Organization
 *
 * There is Organizational specific information required which we have extracted to a plugin
 *
 * It supports using Company Specific User Directory lookups, as well as supporting an
 * Approval/Validation Process to simplify control of Roles and Permissions for large organizations
 * in lieu of direct manipulation by a set of Admins.
 *
 * @author Jonathan
 *
 */
public interface Organization {
    public static final String N_A = "n/a";

    public interface Identity {
        public String id();
        public String fullID() throws OrganizationException; // Fully Qualified ID (includes Domain of Organization)
        public String type();                 // Must be one of "IdentityTypes", see below
        public Identity responsibleTo() throws OrganizationException;         // Chain of Command, or Application ID Sponsor
        public List<String> delegate();         // Someone who has authority to act on behalf of Identity
        public String email();
        public String fullName();
        public String firstName();
        /**
         * If Responsible entity, then String returned is "null"  meaning "no Objection".
         * If String exists, it is the Policy objection text setup by the entity.
         * @return
         */
        public String mayOwn();            // Is id passed belong to a person suitable to be Responsible for content Management
        public boolean isFound();                // Is Identity found in Identity stores
        public boolean isPerson();                // Whether a Person or a Machine (App)
        public Organization org();                 // Organization of Identity


        public static String mixedCase(String in) {
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<in.length();++i) {
                if(i==0) {
                    sb.append(Character.toUpperCase(in.charAt(i)));
                } else {
                    sb.append(Character.toLowerCase(in.charAt(i)));
                }
            }
            return sb.toString();
        }
    }


    /**
     * Name of Organization, suitable for Logging
     * @return
     */
    public String getName();

    /**
     * Realm, for use in distinguishing IDs from different systems/Companies
     * @return
     */
    public String getRealm();

    public boolean supportsRealm(String user);

    public void addSupportedRealm(String r);

    /**
     * If Supported, returns Realm, ex: org.onap
     * ELSE returns null
     * 
     * @param user
     * @return
     */
    public String supportedDomain(String user);

	public String getDomain();

    /**
     * Get Identity information based on userID
     *
     * @param id
     * @return
     */
    public Identity getIdentity(AuthzTrans trans, String id) throws OrganizationException;

    /**
     * Is Revoked
     *
     * Deletion of an Identity that has been removed from an Organization can be dangerous.  Mistakes may have been made
     * in the Organization side, a Feed might be corrupted, an API might not be quite right.
     *
     * The implementation of this method can use a double check of some sort, such as comparison of missing ID in Organization
     * feed with a "Deleted ID" feed.
     *
     */
    public Date isRevoked(AuthzTrans trans, String id);


    /**
     * Does the ID pass Organization Standards
     *
     * Return a Blank (empty) String if empty, otherwise, return a "\n" separated list of
     * reasons why it fails
     *
     * @param id
     * @return
     */
    public String isValidID(AuthzTrans trans, String id);

    /**
     * Return a Blank (empty) String if empty, otherwise, return a "\n" separated list of
     * reasons why it fails
     *
     *  Identity is passed in to allow policies regarding passwords that are the same as user ID
     *
     *  any entries for "prev" imply a reset
     *
     * @param id
     * @param password
     * @return
     */
    public String isValidPassword(final AuthzTrans trans, final String id, final String password, final String ... prev);

    /**
     * Return a list of Strings denoting Organization Password Rules, suitable for posting on a WebPage with <p>
     */
    public String[] getPasswordRules();

    /**
     *
     * @param id
     * @return
     */
    public boolean isValidCred(final AuthzTrans trans, final String id);

    /**
     * If response is Null, then it is valid.  Otherwise, the Organization specific reason is returned.
     *
     * @param trans
     * @param policy
     * @param executor
     * @param vars
     * @return
     * @throws OrganizationException
     */
    public String validate(AuthzTrans trans, Policy policy, Executor executor, String ... vars) throws OrganizationException;

    /**
     * Does your Company distinguish essential permission structures by kind of Identity?
     * i.e. Employee, Contractor, Vendor
     * @return
     */
    public Set<String> getIdentityTypes();

    public enum Notify {
        Approval(1),
        PasswordExpiration(2),
        RoleExpiration(3);

        final int id;
        Notify(int id) {this.id = id;}
        public int getValue() {return id;}
        public static Notify from(int type) {
            for (Notify t : Notify.values()) {
                if (t.id==type) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum Response{
        OK,
        ERR_NotImplemented,
        ERR_UserNotExist,
        ERR_NotificationFailure,
        };

    public enum Expiration {
        Password,
        TempPassword,
        Future,
        UserInRole,
        UserDelegate,
        ExtendPassword,
        RevokedGracePeriodEnds
    }

    public enum Policy {
        CHANGE_JOB,
        LEFT_COMPANY,
        CREATE_MECHID,
        CREATE_MECHID_BY_PERM_ONLY,
        OWNS_MECHID,
        AS_RESPONSIBLE,
        MAY_EXTEND_CRED_EXPIRES,
        MAY_APPLY_DEFAULT_REALM
    }

    /**
     * Notify a User of Action or Info
     *
     * @param type
     * @param url
     * @param users (separated by commas)
     * @param ccs (separated by commas)
     * @param summary
     */

    public Response notify(AuthzTrans trans, Notify type, String url, String ids[], String ccs[], String summary, Boolean urgent);

    /**
     * (more) generic way to send an email
     *
     * @param toList
     * @param ccList
     * @param subject
     * @param body
     * @param urgent
     */

    public int sendEmail(AuthzTrans trans, List<String> toList, List<String> ccList, String subject, String body, Boolean urgent) throws OrganizationException;

    /**
     * whenToValidate
     *
     * Authz support services will ask the Organization Object at startup when it should
     * kickoff Validation processes given particular types.
     *
     * This allows the Organization to express Policy
     *
     * Turn off Validation behavior by returning "null"
     *
     */
    public Date whenToValidate(Notify type, Date lastValidated);


    /**
     * Expiration
     *
     * Given a Calendar item of Start (or now), set the Expiration Date based on the Policy
     * based on type.
     *
     * For instance, "Passwords expire in 3 months"
     *
     * The Extra Parameter is used by certain Orgs.
     *
     * For Password, the extra is UserID, so it can check the User Type
     *
     * @param gc
     * @param exp
     * @return
     */
    public GregorianCalendar expiration(GregorianCalendar gc, Expiration exp, String ... extra);

    /**
     * Get Email Warning timing policies
     * @return
     */
    public EmailWarnings emailWarningPolicy();

    /**
     *
     * @param trans
     * @param user
     * @return
     */
    public List<Identity> getApprovers(AuthzTrans trans, String user) throws OrganizationException ;

    /**
     * Get Identities for Escalation Level
     * 1 = self
     * 2 = expects both self and immediate responsible party
     * 3 = expects self, immediate report and any higher that the Organization wants to escalate to in the
     *     hierarchy.
     *
     * Note: this is used to notify of imminent danger of Application's Cred or Role expirations.
     */
    public List<Identity> getIDs(AuthzTrans trans, String user, int escalate) throws OrganizationException ;


    /*
     *
     * @param user
     * @param type
     * @param users
     * @return
    public Response notifyRequest(AuthzTrans trans, String user, Approval type, List<User> approvers);
    */

    /**
     *
     * @return
     */
    public String getApproverType();

    /*
     * startOfDay - define for company what hour of day business starts (specifically for password and other expiration which
     *   were set by Date only.)
     *
     * @return
     */
    public int startOfDay();

    /**
     * implement this method to support any IDs that can have multiple entries in the cred table
     * NOTE: the combination of ID/expiration date/(encryption type when implemented) must be unique.
     *          Since expiration date is based on startOfDay for your company, you cannot create many
     *          creds for the same ID in the same day.
     * @param id
     * @return
     */
    public boolean canHaveMultipleCreds(String id);

    boolean isTestEnv();

    public void setTestMode(boolean dryRun);

    public static final Organization NULL = new Organization()
    {
        private final GregorianCalendar gc = new GregorianCalendar(1900, 1, 1);
        private final List<Identity> nullList = new ArrayList<>();
        private final Set<String> nullStringSet = new HashSet<>();
        private String[] nullStringArray = new String[0];
        private final Identity nullIdentity = new Identity() {
            List<String> nullUser = new ArrayList<>();
            @Override
            public String type() {
                return N_A;
            }

            @Override
            public String mayOwn() {
                return N_A; // negative case
            }

            @Override
            public boolean isFound() {
                return false;
            }

            @Override
            public String id() {
                return N_A;
            }

            @Override
            public String fullID() {
                return N_A;
            }

            @Override
            public String email() {
                return N_A;
            }

            @Override
            public List<String> delegate() {
                return nullUser;
            }
            @Override
            public String fullName() {
                return N_A;
            }
            @Override
            public Organization org() {
                return NULL;
            }
            @Override
            public String firstName() {
                return N_A;
            }
            @Override
            public boolean isPerson() {
                return false;
            }

            @Override
            public Identity responsibleTo() {
                return null;
            }
        };
        @Override
        public String getName() {
            return N_A;
        }

        @Override
        public String getRealm() {
            return N_A;
        }

        @Override
        public boolean supportsRealm(String r) {
            return false;
        }

        @Override
        public void addSupportedRealm(String r) {
        }
        
        @Override
        public String supportedDomain(String r) {
        	return null;
        }

        @Override
        public String getDomain() {
            return N_A;
        }

        @Override
        public Identity getIdentity(AuthzTrans trans, String id) {
            return nullIdentity;
        }

        @Override
        public String isValidID(final AuthzTrans trans, String id) {
            return N_A;
        }

        @Override
        public String isValidPassword(final AuthzTrans trans, final String user, final String password, final String... prev) {
            return N_A;
        }

        @Override
        public Set<String> getIdentityTypes() {
            return nullStringSet;
        }

        @Override
        public Response notify(AuthzTrans trans, Notify type, String url,
                String[] users, String[] ccs, String summary, Boolean urgent) {
            return Response.ERR_NotImplemented;
        }

        @Override
        public int sendEmail(AuthzTrans trans, List<String> toList, List<String> ccList,
                String subject, String body, Boolean urgent) throws OrganizationException {
            return 0;
        }

        @Override
        public Date whenToValidate(Notify type, Date lastValidated) {
            return gc.getTime();
        }

        @Override
        public GregorianCalendar expiration(GregorianCalendar gc,
                Expiration exp, String... extra) {
            return gc;
        }

        @Override
        public List<Identity> getApprovers(AuthzTrans trans, String user)
                throws OrganizationException {
            return nullList;
        }

        @Override
        public String getApproverType() {
            return "";
        }

        @Override
        public int startOfDay() {
            return 0;
        }

        @Override
        public boolean canHaveMultipleCreds(String id) {
            return false;
        }

        @Override
        public boolean isValidCred(final AuthzTrans trans, final String id) {
            return false;
        }

        @Override
        public String validate(AuthzTrans trans, Policy policy, Executor executor, String ... vars)
                throws OrganizationException {
            return "Null Organization rejects all Policies";
        }

        @Override
        public boolean isTestEnv() {
            return false;
        }

        @Override
        public void setTestMode(boolean dryRun) {
        }

        @Override
        public EmailWarnings emailWarningPolicy() {
            return new EmailWarnings() {

                @Override
                public long credEmailInterval()
                {
                    return 604800000L; // 7 days in millis 1000 * 86400 * 7
                }

                @Override
                public long roleEmailInterval()
                {
                    return 604800000L; // 7 days in millis 1000 * 86400 * 7
                }

                @Override
                public long apprEmailInterval() {
                    return 259200000L; // 3 days in millis 1000 * 86400 * 3
                }

                @Override
                public long  credExpirationWarning()
                {
                    return( 2592000000L ); // One month, in milliseconds 1000 * 86400 * 30  in milliseconds
                }

                @Override
                public long roleExpirationWarning()
                {
                    return( 2592000000L ); // One month, in milliseconds 1000 * 86400 * 30  in milliseconds
                }

                @Override
                public long emailUrgentWarning()
                {
                    return( 1209600000L ); // Two weeks, in milliseconds 1000 * 86400 * 14  in milliseconds
                }

            };


        }

        @Override
        public String[] getPasswordRules() {
            return nullStringArray;
        }

        @Override
        public Date isRevoked(AuthzTrans trans, String id) {
            // provide a corresponding feed that indicates that an ID has been intentionally removed from identities.dat table.
            return null;
        }

        @Override
        public List<Identity> getIDs(AuthzTrans trans, String user, int escalate) throws OrganizationException {
            // TODO Auto-generated method stub
            return null;
        }

    };
}


