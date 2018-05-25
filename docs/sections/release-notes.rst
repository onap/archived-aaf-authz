.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


Release Notes
=============



Version: 2.1.0
--------------


:Release Date: 2018-04-18



**New Features**

This release fixes the packaging and security issues.

**Bug Fixes**
	NA
**Known Issues**
	NA

**Security Issues**
	This release fixes the security vulnerabilities due to the opensourced libraries used in the project


**Upgrade Notes**
  NA

**Deprecation Notes**

Version: 1.0.1

Release Date: 2017-11-16


New Features:

 - Service (primary) – All the Authorization information (more on that in a bit)
 - Locate – how to find ANY OR ALL AAF instances across any geographic distribution
 - OAuth 2.0 – new component providing Tokens and Introspection (no time to discuss here)
 - GUI – Tool to view and manage Authorization Information, and create Credentials
 - Certman – Certificate Manger, create and renew X509 with Fine-Grained Identity
 - FS – File Server to provide access to distributable elements (like well known certs)
 - Hello - Test your client access (certs, OAuth 2.0, etc)




Bug Fixes
   - `AAF-290 <https://jira.onap.org/browse/AAF-290>`_ Fix aaf trusrstore
   - `AAF-270 <https://jira.onap.org/browse/AAF-270>`_ AAF fails health check on HEAT deployment
   - `AAF-286 <https://jira.onap.org/browse/AAF-286>`_ SMS fails health check on OOM deployment
   - `AAF-273 <https://jira.onap.org/browse/AAF-273>`_ Cassandra pod running over 8G heap - or 10% of ONAP ram (for 135 other pods on 256G 4 node cluster)

   
Known Issues
   - 

Other

