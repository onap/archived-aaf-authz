.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

AAF in a Nutshell
=========================

This is a quick overview of some of the core structures of AAF. A more detailed narrative description can be found at The New Person's Guide to AAF

Parts of AAF - Basic Terms
-----------------------------
A namespace is the container (sometimes called a "security domain" by other security systems) assigned to an application; for instance, "com.att.test"namespaces contain 1 or more roles
roles contain permissions and users 

  #.	a role is where users and permissions meet; permissions are not granted directly to users, rather a perm is granted to a role and users are added to the role
  #.	a role contains 0 or more permissions
  #.	a role contains 0 or more users or APPID identities
  #.	note that role memberships have an expiration date.

     -  The owner of the namespace must re-approve all role memberships periodically.
     -  All approval requests, role renewal reviews, credential expiration, etc, emails will go to the namespace owner.
     -  If the namespace owner doesn't act upon these emails, users/appid’s will lose their permissions. Applications will break.
     -  Restoring lost permissions is the responsibility of the namespace admins, not any AAF support tier.

Namespaces contain 1 or more permissions
  #.	other than the access permissions discussed below, AAF does not care about permissions
  #.	AAF does not interpret application-specific permissions; in other words, it's up to the applications developers to create a permission scheme.

    -	the general usage pattern is that an application will ask for all permissions associated with a user
    -	locally, the application interprets what the presence or absence of a permissions means

By default, every namespace has 2 "access" permissions: 
    #.	a read/write permission, for instance "org.onap.test.access \* \*"
    #.	a read only permission, for instance "org.onap.test.access \* read"

By default, every namespace has an admin role, for instance "org.onap.test.admin"
    #.	the admin role contains the read/write permission for the namespace

       -  if you delete the admin role, or the read/write permission from the role, your admins will have no access to your namespace. This is bad.

see Documentation for Namespace Admins for commands related to namespaces, roles, permissions
	

AppID Identity
-----------------
To use a AppID in AAF, the AppID must be associated with a namespace 
  #.	The owner of the namespace MUST BE the sponsor of the AppID. 
  #.	 The owner of the namespace/appid is the ONLY PERSON who can add the AppID to the namespace. 
  #.	Once added to a namespace, you will now have a AppID identity. For example, namespace=org.onap.test, AppID=m99999, the AppID identity will be m99999@test.onap.org

      -	note that the domain portion (the part after the "@") is the namespace name reversed

AppID Identities must always be lowercase. Use "m91266@test.onap.org", not "M91266@test.onap.com"

AppID Credentials (passwords)
---------------------------------
Each AppID identity may have 1 or more credential records 
 - each record will have its own expiration date
 - each record may or may not be associated with the same password

Once the owner of the namespace/AppID has created the initial AppID identity & password, any admin can add new credentials as long as she/he knows a current password.
  
Here are some scenarios to illustrate some points about  AAF's credentials:
Scenario 1: an application already running in an Instance needs to do their yearly AppID password update

 - The AppID identity already has a credential, but it is expiring soon
 - The application's support team can create a new credential at any time
     - must enter an existing password to create a new one; store your passwords in a secure manner.
     - this new record will have an expiration date 1 year out
 - the password in the record will be a different password; this means the application's config files need to change
 - With a new password in place, there is no tight coordination required when the application's config files are updated. The old password continues to work until its expiration date. The new password is in place and will work as soon as the configuration is changed. 

Scenario 2:An AAF command to "extend" the current password. NOTE: extending a password is a temporary workaround; a new credential must be created as soon as possible. 
 - this does not modiify the existing credential record
 - this creates a new credential record with an expiration date 5 days in the future
 - an admin of the namespace must now:
 - using the appropriate GUI link for the environment, go to the Password Management tab and create a new credential
 - if using cadi, digest the new password to get an encrypted password string 
 - update cadi.properties
 - bounce application processes
 - if not using cadi,
 - update whatever config file is used to store the AppID identity's password
 - bounce application processes, if required to re-read config
 - to re-iterate: AAF never modifies an existing credential; AAF creates new credential records

