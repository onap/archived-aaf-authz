.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

Client Configuration
====================

TEST version of "cadi.properties"
---------------------------------
These properties point you to the ONAP TEST environment.  

Properties are separated into

 * etc
    * main Property file which provides Client specific info.  As a client, this could be put in container, or placed on Host Box
    * The important thing is to LINK the property with Location and Certificate Properties, see "local"
 * local
   * where there is Machine specific information (i.e. GEO Location (Latitude/Longitude)
   * where this is Machine specific Certificates (for running services)
       * This is because the certificates used must match the Endpoint that the Container is running on
       * Note Certificate Manager can Place all these components together in one place.
           * For April, 2018, please write Jonathan.gathman@att.com for credentials until TEST Env with Certificate Manager is fully tested.  Include
           1. AAF Namespace (you MUST be the owner for the request to be accepted)
           2. Fully Qualified App ID (ID + Namespace)
           3. Machine to be deployed on.
		   
Client Credentials
------------------
For Beijing, full TLS is expected among all components.  AAF provides the "Certificate Manager" which can "Place" Certificate information 

Example Source Code
-------------------
Note the FULL class is available in the authz repo, cadi_aaf/org/onap/aaf/client/sample/Sample.java