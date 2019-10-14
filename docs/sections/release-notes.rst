.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Release Notes
=============

Version: 2.1.15 (El Alto, 5.0.1)
---------------------------------------------

:Release Date: 2019-08-12

**New Features**
El Alto is a consolidation release.  New Features are not Added.  
However, for El Alto, ONAP is propagating the AAF Auto-Configuration and Certificate Generation feature from Dublin, see below

An important change, however, is that the AAF Locator requires internal K8s Apps to use 
	internal-to-K8s Service URL tags as (example) "onap.org.osaaf.aaf.service:2.1" 
        external-to-K8s Service URL tags as (example) "org.osaaf.aaf.service.2.1"

        IF you are using previous configurations, you may need to clear the existing directory
	
	- Login to your Init Container
	- cd /opt/app/osaaf/local
        - CAREFULLY rm *.*, and have it regenerate 

**Bug Fixes**
	- `AAF-859 <https://jira.onap.org/browse/AAF-859>`_ Images hardcoded in AAF helm deployment yamls

**Known Issues - solve in Frankfurt**
        - `AAF-962 <https://jira.onap.org/browse/AAF-962>`_ AAF Certs could not generate...    dd

Version: 2.1.13 (Dublin, 4.0.0-ONAP)
---------------------------------------

:Release Date: 2019-06-06

**New Features**

Note: In general, Infrastructure must be accomplished in the release PRIOR to general usage.  This is the case for most of the features included here.

	- AAF has built the required features to automatically generate all Certificates and Configurations real-time.  This will be utilized by ONAP MSs in El Alto
	- AAF has the ability to publish both Public and Internal Private K8s Service information (Locator)
	- Greatly Reduced size of Docker Images
	- Greatly enhanced startup procedures in K8s, to more cleanly start, with Certificate, Property Generation every time
	- Ability to run internally as non-root (fully setup K8s in El Alto)
	- Removal of unused classes in Batch
	- Large improvement in Batch and methodology, to be used in El Alto

**Bug Fixes**
	- `AAF-797 <https://jira.onap.org/browse/AAF-797>`_ Update IP address for aaf-onap-test.osaaf.org
	- `AAF-794 <https://jira.onap.org/browse/AAF-794>`_ Misleading error message in agent.sh
	- `AAF-773 <https://jira.onap.org/browse/AAF-773>`_ aaf-cass timing issues
	- `AAF-769 <https://jira.onap.org/browse/AAF-769>`_ AAF CSIT not working
	- `AAF-727 <https://jira.onap.org/browse/AAF-727>`_ Cert Subject Check confused by Email
	- `AAF-722 <https://jira.onap.org/browse/AAF-722>`_ aaf continues to be available to aai-resources even though aaf database appears to be down
	- `AAF-720 <https://jira.onap.org/browse/AAF-720>`_ Docker Images not passing Signal -1
	- `AAF-645 <https://jira.onap.org/browse/AAF-645>`_ Fix "Null" string for fetching path inside CADI API enforcement filter
	- `AAF-522 <https://jira.onap.org/browse/AAF-522>`_ rsa 4096 signing fails with TPM
	- `AAF-813 <https://jira.onap.org/browse/AAF-813>`_ Missing Role for dmaap-bc Identity
	- `AAF-514 <https://jira.onap.org/browse/AAF-514>`_ TPM Plugin: Remove global structure used for storing session data
	- `AAF-785 <https://jira.onap.org/browse/AAF-785>`_ non STAGING version on master
	- `AAF-822 <https://jira.onap.org/browse/AAF-822>`_ Startup issues with K8S, Certs

**Usage Notes**
	- AAF Core and SMS elements have consistently started from scratch. The one case where this didn't happen for SMS, 
		it was found that incompatible data was left in volume.  Removal of old data for SMS (See SMS notes) should resolve
        - On the same instance, one AAF Core component had a similar scenario.  A simple bounce of aaf-locator resolved.
	- Existing Cassandra
		- For each release, AAF maintains the authz/auth/auth-cass/cass_init/init.cql which is used to setup Keyspaces from scratch
		- Any changes are also done in small CQL files, you MIGHT need authz/auth/auth-cass/cass_init/init2_10.cql for Dublin


Version: 2.1.8 (Casablanca, 3.0.0-ONAP, Casablanca Maintenance Release)
--------------------------------------------------------------------------

Note: AAF did not create new artifacts for Casablanca Maintenance Release.


:Release Date: 2018-11-30

**New Features**

 - AAF created a local CA and CA Strategy to be utilized for ONAP Test Environments that can instantiated daily, yet have continuity over time and environments. (REAL ONAP instantiations should use their *own* CAs outside of initial tests.)
 - AAF has auto-creation of configurations and certificates.  This is expected to be done inside an "agent" container, and used by Apps.
 - AAF stores and creates "Bootstrap Data" for all users of AAF in ONAP.  This simplifies the efforts of ONAP components to organize their Authorizations, and so that various Test Environments can start with correct data every time.
 - Refactored all of AAF instantiations to use the above, and have consistency between the 5 ways to start AAF.
 - Ability for CADI Clients to map previous User/Password combinations to current credentials for migration purposes. This is applied to Shiro Plugin as well
 - CADI Coarse Grain Enforcement Point (Authorize API access). 
 - Created Backward compatibility features, both for DB (Cassandra) and for API access.


**Bug Fixes**
	- AAF in OOM was not stable coming out of Beijing.  AAF OOM was refactored using above Container based Configurations.
	- `AAF-617 <https://jira.onap.org/browse/AAF-617>`_ LOCATE Proxy DELETE not working
	- `AAF-605 <https://jira.onap.org/browse/AAF-605>`_ DB Stoppage not causing Reset of Connection
	- `AAF-601 <https://jira.onap.org/browse/AAF-601>`_ Agent "showpass" errors on optional "chal" file, when not exists
	- `AAF-600 <https://jira.onap.org/browse/AAF-600>`_ Bad Data for APPC in AAF Test Evironment
	- `AAF-598 <https://jira.onap.org/browse/AAF-598>`_ Inconsistent Startup with truly persistent Cass Data
	- `AAF-597 <https://jira.onap.org/browse/AAF-597>`_ Please change default appc@appc.onap.org permission
	- `AAF-592 <https://jira.onap.org/browse/AAF-592>`_ SDNC not able to authenticate with BAth username/password
	- `AAF-530 <https://jira.onap.org/browse/AAF-530>`_ AAF inside Kubernetes inaccessible for clients from outside

**Known Issues**
   N/A

**Other**
   - REAL ONAP versus ONAP Test Environment
     - CA used in ONAP Test Environment should (of course) NOT be used by individual companies in REAL deployments.
     - Cassandra Instance in Kubernetes ONAP Test environment is a single instance.  REAL deployments should follow global, multi-datacenter deployment strategies per Cassandra recommendations.
   - AAF team organized all the Identities, all the Credentials, etc, on behalf of ONAP Apps.

**Security Notes**
 - AAF has achieved clean scans for everything in authz.git repo
 - In the cadi.git (used for Adaptors), there is a Shiro adapter.  Shiro itself has security flags, *NOT* the adapter, so understand the security issues of Shiro before use.

 - AAF code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The AAF open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=43386201>`_.


**Upgrade Notes**
  NA

**Deprecation Notes**

Version: 2.1.1 (Beijing, 2.0.0-ONAP)
--------------------------------------

:Release Date: 2017-06-05


**New Features:**

 - Service (primary) – All the Authorization information (more on that in a bit)
 - Locate – how to find ANY OR ALL AAF instances across any geographic distribution
 - OAuth 2.0 – new component providing Tokens and Introspection (no time to discuss here)
 - GUI – Tool to view and manage Authorization Information, and create Credentials
 - Certman – Certificate Manger, create and renew X509 with Fine-Grained Identity
 - FS – File Server to provide access to distributable elements (like well known certs)
 - Hello - Test your client access (certs, OAuth 2.0, etc)

**Bug Fixes**
   - `AAF-290 <https://jira.onap.org/browse/AAF-290>`_ Fix aaf truststore
   - `AAF-270 <https://jira.onap.org/browse/AAF-270>`_ AAF fails health check on HEAT deployment
   - `AAF-286 <https://jira.onap.org/browse/AAF-286>`_ SMS fails health check on OOM deployment
   - `AAF-273 <https://jira.onap.org/browse/AAF-273>`_ Cassandra pod running over 8G heap - or 10% of ONAP ram (for 135 other pods on 256G 4 node cluster)

   
**Known Issues**
   N/A

**Other**
   - REAL ONAP versus ONAP Test Environment
     - Cassandra Instance in Kubernetes ONAP Test environment is a single instance.  REAL deployments should follow global, multi-datacenter deployment strategies per Cassandra recommendations.


================
Quick Links
================
 	- `AAF project page <https://wiki.onap.org/display/DW/Application+Authorization+Framework+Project>`_
 	- `CII Best Practices Silver Badge information for AAF <https://bestpractices.coreinfrastructure.org/en/projects/2303?criteria_level=1>`_
 	- `CII Best Practices Passing Badge information for AAF <https://bestpractices.coreinfrastructure.org/en/projects/2303?criteria_level=0>`_
 	- `Project Vulnerability Review Table for AAF <https://wiki.onap.org/pages/viewpage.action?pageId=43386201>`_



