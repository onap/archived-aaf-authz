.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

========================================
Setting up Certs and CADI Configurations
========================================

*Note: this document assumes UNIX Bash Shell.  Being Java, AAF works in Windows, but you will have to create your own script/instruction conversions.*

------------------
Strategy
------------------

ONAP is deployed in Docker Containers or Kubernetes managed Docker Containers.  Therefore, this instruction utilizes a Docker Container as a standalone Utility... (This means that this container will stop as soon as it is done with its work... it is not a long running daemon)

Given that all ONAP entities are also in Docker Containers, they all can access Persistent Volumes.

This tool creates all the Configurations, including Certificates, onto a declared Volume on the directories starting with "/opt/app/osaaf"

------------------
Prerequisites
------------------
  * Docker
    * Note: it does NOT have to be the SAME Docker that AAF is deployed on...
    | but it DOES have be accessible to the AAF Instance.  
  * For ONAP, this means
    
    * Windriver VPN
    * include "10.12.6.214 aaf-onap-test.osaaf.org" in your /etc/hosts or DNS

-----------------------
Obtain the Agent Script
-----------------------
Choose the directory you wish to start in... 

If you don't want to clone all of AAF, just get the "agent.sh" from a Browser:

  https://gerrit.onap.org/r/gitweb?p=aaf/authz.git;a=blob_plain;f=auth/docker/agent.sh;hb=HEAD
  
  rename file "auth-docker-agent.sh" to "agent.sh" (named because of subdirectory in which it is found) 

Note: curl/wget returns an  html, instead of text.  This cannot be used!
  | You have to mv, and rename it to "agent.sh", but avoids full clone...

-------------------------
Run Script
-------------------------

In your chosen directory ::
 
  $ bash agent.sh

The Agent will look for "aaf.props", and if it doesn't exist, or is missing information, it will ask for it.

This file is available to reuse for multiple calls. More importantly, you should use it as a template for auto-configuration.  (In ONAP, these are HEAT templates and OOM Helm Charts)

---------------------
'aaf.prop' Properties
---------------------

=================== =============== ============
Query               Tag             Description
=================== =============== ============
CADI Version        VERSION         Defaults to CADI version of this
AAF's FQDN          AAF_FQDN        PUBLIC Name for AAF. For ONAP Test, it is 'aaf-onap-test.osaaf.org'
Deployer's FQI      DEPLOY_FQI      In a REAL system, this would be a person or process. For ONAP Testing, the id is deployer@people.osaaf.org, password (see Dynamic Properties) is 'demo123456!'
App's Root FQDN     APP_FQDN        This will show up in the Cert Subject, and should be the name given by Docker. i.e. clamp.onap
App's FQI           APP_FQI         Fully Qualified ID given by Organization and with AAF NS/domain.  ex: clamp@clamp.onap.org 
App's Volume        VOLUME          Volume to put the data, see above. ex: clamp_aaf
DRIVER              DRIVER          Docker Volume type... See Docker Volume documentation
LATITUDE of Node    LATITUDE        Global latitude coordinate of Node (best guess in Kubernetes)
LONGITUDE of Node   LONGITUDE       Global longitude coordinate of Node (best guess in Kubernetes)
=================== =============== ============

---------------------
Dynamic Properties
---------------------

These Properties do not automatically save in 'aaf.props', because...

  | Passwords should not be stored clear text, with the possible exception of constant Environment Recreation, where it is impractical.
  | The IP of the AAF's FQDN is looked up, if possible.  It can be set, however, when lookup isn't available.

=================== =============== ============
Query               Tag             Description
=================== =============== ============
Deployer's Password DEPLOY_PASSWORD Password for the Deployer. Avoids storing, except where impossible otherwise. 
IP of <AAF_FQDN>    AAF_FQDN_IP     IP for Name of AAF FQDN, if not available by normal lookup means
=================== =============== ============

-------------------------------
Typical ONAP Entity Info in AAF
-------------------------------
*This is not intended to be a comprehensive list, but a short list of main entities*

============================= ===========================  ======================= ==============================================
ONAP Namespaces               APP FQI                      APP FQDN OOM            APP FQDN HEAT
============================= ===========================  ======================= ==============================================
org.osaaf.aaf                 aaf@aaf.osaaf.org            aaf.onap                aaf.api.simpledemo.onap.org
org.onap.aaf-sms              aaf-sms@aaf-sms.onap.org     aaf-sms.onap            aaf-sms.api.simpledemo.onap.org
org.onap.aai                  aai@aai.onap.org             aai.onap                aai.api.simpledemo.onap.org
org.onap.appc                 appc@appc.onap.org           appc.onap               appc.api.simpledemo.onap.org
org.onap.clamp                clamp@clamp.onap.org         clamp.onap              clamp.api.simpledemo.onap.org
org.onap.dcae                 dcae@dcae.onap.org           dcae.onap               dcae.api.simpledemo.onap.org
org.onap.dmaap                dmaap@dmaap.onap.org         dmaap.onap              dmaap.api.simpledemo.onap.org                                         
org.onap.dmaap-bc             dmaap-bc@dmaap-bc.onap.org   dmaap-bc.onap           dmaap-bc.api.simpledemo.onap.org
org.onap.dmaap-dr             dmaap-bc@dmaap-dr.onap.org   dmaap-dr.onap           dmaap-dr.api.simpledemo.onap.org                                           
org.onap.dmaap-mr             dmaap-mr@dmaap-mr.onap.org   dmaap-mr.onap           dmaap-mr.api.simpledemo.onap.org
org.onap.oof                  oof@oof.onap.org             oof.onap                oof.api.simpledemo.onap.org
org.onap.policy               policy@policy.onap.org       policy.onap             policy.api.simpledemo.onap.org
org.onap.pomba                pomba@pomba.onap.org         pomba.onap              pomba.api.simpledemo.onap.org
org.onap.portal               portal@portal.onap.org       portal.onap             portal.api.simpledemo.onap.org
org.onap.sdc                  sdc@sdc.onap.org             sdc.onap                sdc.api.simpledemo.onap.org
org.onap.sdnc                 sdnc@sdnc.onap.org           sdnc.onap               sdnc.api.simpledemo.onap.org
org.onap.so                   so@so.onap.org               so.onap                 so.api.simpledemo.onap.org
org.onap.vfc                  vfc@vfc.onap.org             vfc.onap                vfc.api.simpledemo.onap.org
org.onap.vid                  vid@vid.onap.org             vid.onap                vid.api.simpledemo.onap.org
============================= ===========================  ======================= ==============================================

*Note 1: FQDNs are set in AAF's Credential Artifact data, accessible in "Cred Details" from Namespace Page*

*Note 2: Also, AAF itself is different, as it is not an ONAP only component... It is also expected to be used outside of ONAP.*

If something goes wrong, and Certificate is not created, you can adjust the data, remove the data from the Container's /opt/app/osaaf/local dir, and it will generate again. ::

  root@77777:/opt/app/osaaf/local# rm *
  root@77777:/opt/app/osaaf/local# exit
  $ bash agent.sh bash

-------------
Informational
-------------

There are two sets of Credentials at play here.  The ability to create the Certificate belongs to one of
  
  * The person responsible for the ID in the Organization 
  * A delegated deployer

It is expected in large organizations that Individual Employees are given the responsibility of an ID for an APP they are responsible for.

  In ONAP test, to simplify create/tear-down environment... 
     | The **Owner** is always "mmanager@people.osaaf.org". 
     | The **Sponsor** is always "aaf_admin@people.osaaf.org".

In a large org, there are probably many Operations teams to support many different apps.

  In ONAP test, 
     The **Deployer** is always set to "deployer@people.osaaf.org" for all Apps.


