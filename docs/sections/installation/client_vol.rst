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

  Note: curl/wget get html, instead of text
  | You might have to mv, and rename it to "agent.sh", but avoids full clone

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
Deployer's FQI      DEPLOY_FQI      deployer@people.osaaf.org.  In a REAL system, this would be a person or process 
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

-----------------------
ONAP Entity Info in AAF
-----------------------

============================= ===========================  =======================
ONAP Namespaces               APP FQI                      APP FQDN
============================= ===========================  =======================
org.onap.aaf-sms              aaf-sms@aaf-sms.onap.org     aaf-sms
org.onap.aai                  aai@aai.onap.org             aai
org.onap.appc                 appc@appc.onap.org           appc
org.onap.clamp                clamp@clamp.onap.org         clamp
org.onap.dcae                 dcae@dcae.onap.org           dcae
org.onap.dmaap-bc             dmaap-bc@dmaap-bc.onap.org   dmaap-bc
org.onap.dmaap-mr             dmaap-mr@dmaap-mr.onap.org   dmaap-mr
org.onap.oof                  oof@oof.onap.org             oof
org.onap.sdnc                 sdnc@sdnc.onap.org           sdnc
============================= ===========================  =======================

*Note: FQDNs are set in AAF's Credential Artifact data, accessible in "Cred Details" from Namespace Page*

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
     The **Deployer** is always set to "deploy@people.osaaf.org" for all Apps.


