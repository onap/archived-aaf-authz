.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Trusting Other Authentication Entities
==========================================

Few Transactions in an Organization of any size touch only one service.  This is even more true with MicroService Architecture.

In AAF, EVERY Tranasction must be Authenticated from the Caller, but in App-to-App situations, not every Authorization should be 
evaluated on the underlying caller.

SERVICE Configuration
----------------------
   1) Define the Permission this App will use for Trust, and add to Service's "Cadi Properties"
      Ex:
 
      cadi_trust_perm=org.onap.aai.user_chain|com.att|trust
	
   2) In the AAF Service, user the AAF CMDline interface to create Permission that matches above, and role

      Given that an App may trust "ONAP Portal" to have validated an end-user, and that ONAP Portal's Identity is portal@portal.onap.org,

      role create org.onap.aai.trusted portal@portal.onap.org
      perm create org.onap.aai.user_chain|org.onap|trust org.onap.aai.trusted
 
      Note: These instructions are for first Identity, which both creates Role and Perm, and adds User, Grants Perm.  Admin may separate the commands of "create" and "grant"/"add", see CUI help.

CLIENT Transaction
-------------------
  The CADI client, when used, will create USER_CHAIN property automatically, but not all CLIENTs are CADI.  For NON-CADI HTTP Clients, do the following:

  * Create an HTTP Header property called “USER_CHAIN”
    * The syntax for the value is:

      <AAF ID>:<service Reference>:<Authentication Type>[:AS][,<ID>:<reference>:<type>]*

      Where “:AS” is the indicator that you want the Service to treat the transaction as if it came from the end client.
            <AAF_ID> is the Identity of the Calling Client (End Client)
            <service Reference> should be the Service's AAF Namespace and microservice name, separated by '.'
            <Authentication Type> should be how the Client was Authenticated

		BAth - BasicAuth
                x509 - X509 Client Certificate

                <other Organization defined Types are acceptable, but should be 4 chars long for ease of use, and match any TAF Adapters used to validate>
 
  Example:
     USER_CHAIN: demo@people.osaaf.org:org.onap.aai:BAth:AS
 
  What Happens:
    Cadi Code (such as what is in CadiFilter) 
       * Reads the USER_CHAIN
       * IF the last USER_CHAIN Entry ends with ":AS"... 
          * Checks to see if the Identity is the same as Service (nice for Model-View-Controller and multi-MS applications) OR
          * if the incoming caller has the Permission specified in "cadi_trust_perm"

       * THEN a new "TrustPrincipal" is created, which takes on the identity of the ":AS" identified Identity for the purposes of Authentication.
      
