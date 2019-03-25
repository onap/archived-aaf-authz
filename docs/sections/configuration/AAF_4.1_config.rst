.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

========================================
Automated Configuration and Certificates
========================================

*Note: this document assumes UNIX Bash Shell.  Being Java, AAF works in Windows, but you will have to create your own script/instruction conversions.*

=================
Optimal Strategy
=================

ONAP is deployed in Docker Containers or Kubernetes managed Docker Containers.  Therefore, this instruction utilizes a Docker Container as a standalone Utility... (This means that this container will stop as soon as it is done with its work... it is not a long running daemon)

Given that all ONAP entities are also in Docker Containers, they all can access Persistent Volumes.

This tool creates all the Configurations, including Certificates, onto a declared Volume on the directories starting with "/opt/app/osaaf"

==================
Prerequisites
==================
  * Access to a RUNNING AAF System

    * For ONAP TEST, this means

      * Windriver VPN
      * include lastest IP of aaf-onap-test.osaaf.org" in your /etc/hosts or DNS

        * As of Mar 20, 2019, this is 10.12.5.145. 

  * For Writing to Volumes for Docker or K8s

    * Docker

      * Note: it does NOT have to be the SAME Docker that AAF is deployed on...

         * but it DOES have be accessible to the AAF Instance.  

  * For creating Configurations on Local Disk

    * For Development purposes
    * For running AAF on Bare Metal (or VM)
    * A Truststore that includes your CA

      * for ONAP TEST, you can obtain truststoreONAPall.jks from the `AAF FileServer`_.

        * (You can also get the ONAP TEST Root CA there)

    * the latest aaf-auth-cmd-<VERSION>-full.jar from `ONAP Nexus`_.
    * you can still use the same "agent.sh" script below

.. _AAF FileServer: http://aaf-onap-test.osaaf.org/-
.. _ONAP Nexus: https://nexus.onap.org/#nexus-search;quick~aaf-auth-cmd

-----------------------
Obtain the Agent Script
-----------------------
Choose the directory you wish to start in... 

If you don't want to clone all of AAF, just get the "agent.sh" from a Browser:

  https://gerrit.onap.org/r/gitweb?p=aaf/authz.git;a=blob_plain;f=auth/docker/agent.sh;hb=HEAD
  
  rename file "auth-docker-agent.sh" to "agent.sh" (named because of subdirectory in which it is found) 

Note: curl/wget returns an  html, instead of text.  This cannot be used!
  | You have to mv, and rename it to "agent.sh", but avoids full clone...

=============
Run Script
=============
----------------
For Docker/K8s
----------------
In your chosen directory ::
 
  $ bash agent.sh

The Agent will look for "aaf.props", and if it doesn't exist, or is missing information, it will ask for it.

This file is available to reuse for multiple calls. More importantly, you should use it as a template for auto-configuration.  (In ONAP, these are HEAT templates and OOM Helm Charts)

--------------------------
For Local/BareMetal (VM)
--------------------------
In your chosen directory ::
 
  $ bash agent.sh local <instructions>

The Agent will look for "aaf.props", and if it doesn't exist, or is missing information, it will ask for it.

IMPORTANT: When you are doing "LOCAL", you are creating a CERTIFICATE for your local Machine.  Therefore, you need to AUTHORIZE this creation 
by creating an "Artifact" as the OWNER of the Namespace (In ONAP Test, all the NSs are owned by "aaf_admin@people.osaaf.org")

  1) Copy the out-of-the-box Artifact from the Credentials of your Namespace
 
    * In ONAP Test, as "aaf_admin", click https://aaf-onap-test.osaaf.org:8200/gui/ns
    * Select the NS you are need a Certificate for (i.e. org.onap.aai)
    * Select the Green "Cred Details" button in Credentials area
    * Select "View All" on credential line
    * Select the ONAP default FQDN line's "Details" button
    * Select "Copy Artifact" Radio Button at Bottom, and enter YOUR MACHINE'S FQDN in the entry box that appears.
    * Click "Copy" button
    * Click "Artifacts Show" Breadcrumb. You should see your new entry.

  2) Edit the new Artifact to match your Local Machine

    * Check the SANS.  If it does not include the original FQDN, then add it.  (Example, add "aai").  This is so this Certificate can be used by aai
      inside of containers as well.  
    * Change the "Directory" to be the Local Directory you want to put your Local Certs in. 
    * Change the "O/S User" to be the O/S user that needs to access the Certificate (yours)
    * Click on the Artifact types you want.  "file" means PEM format private key and cert.  "script" has ready-made O/S crontab and validation scripts 
      for auto-renewal of O/S based Services.  We will do something different for containers.
    * click "Update"

  3) Be sure to validate this information with a "read" command on your target machine.

<**Instructions**> - Commands you can do with agent.sh local:

Note: There are some command line defaults, relating to ID from aaf.props and FQDN, if your local machine (uname -n) REPORTS the same name as your FQDN.  
If it does not, you will need to explicitly set the command.  Examples will use "aai" and local machine "mymachine.myco.com"

  read 
    Prints the Artifact information from Certificate Manager related to command.  Generally, it's a good idea to Read to make sure things are setup
    Example: ``$ bash agent.sh local read aai@aai.onap.org mymachine.myco.com``

  place 
    Actually creates the Certificate Artifacts requested on disk, in the directory requested with the O/S User requested, etc.
    Depending on what you asked for in the Artifact, you should see:
    Example: ``$ bash agent.sh local place aai@aai.onap.org mymachine.myco.com``

      | Writing to /private/tmp/onap
      | Writing file /private/tmp/onap/org.onap.aai.keyfile
      | Writing file /private/tmp/onap/org.onap.aai.crt
      | Writing file /private/tmp/onap/org.onap.aai.key
      | Writing file /private/tmp/onap/org.onap.aai.p12
      | Writing file /private/tmp/onap/org.onap.aai.trust.jks
      | Writing file /private/tmp/onap/org.onap.aai.check.sh
      | Writing file /private/tmp/onap/org.onap.aai.crontab.sh
      | Creating new /private/tmp/onap/org.onap.aai.cred.props
      | 2019-03-25T09:14:29.174-0500: Trans Info
      |   REMOTE Place Artifact 2743.9736ms
      |   Reconstitute Private Key 0.212454ms
      |

    Focus on "Reconstitute Private Key"... if that isn't there, it didn't create

  showpass 
    Shows the passwords generated and used for the various artifacts that need them.  Example org.onap.aai. will be generated with a password.
    Example: ``$ bash agent.sh local showpass aai@aai.onap.org mymachine.myco.com``

=======================
'aaf.prop' Properties
=======================

These properties will be created when you run "agent.sh".  Many of the values will be defaulted, or allow you to change.  It will be placed into an "aaf.props" file for you to save, edit or otherwise modify/utilize.

==================== ================= ============
Query                Tag               Description
==================== ================= ============
DOCKER REPOSITORY    DOCKER_REPOSITORY Defaults to current ONAP Repository
CADI Version         VERSION           Defaults to current CADI (AAF) version
AAF's FQDN           AAF_FQDN          PUBLIC Name for AAF. For ONAP Test, it is 'aaf-onap-test.osaaf.org'
AAF FQDN IP          AAF_FQDN_IP       If FQDN isn't actually found with DNS, you will have to enter the IP.  For 'aaf-onap-test.osaaf.org', as of March 20, 2019, it is '10.12.5.145'
Deployer's FQI       DEPLOY_FQI        In a REAL system, this would be a person or process. For ONAP Testing, the id is 'deployer@people.osaaf.org'
Deployer's PASSWORD  DEPLOY_PASSWORD   OPTIONAL!! REAL systems should not store passwords in clear text. For ONAP Testing, the password is 'demo123456!'
App's Root FQDN      APP_FQDN          This will show up in the Cert Subject, make it the App Acronym. i.e 'clamp'
App's FQI            APP_FQI           Fully Qualified ID given by Organization and with AAF NS/domain.  ex: 'clamp@clamp.onap.org'
App's Volume         VOLUME            Volume to put the data, see above. ex: 'clamp_config'
DRIVER               DRIVER            Docker Volume type... See Docker Volume documentation. Default is 'local'
LATITUDE of Node     LATITUDE          Global latitude coordinate of Node (best guess in Kubernetes)
LONGITUDE of Node    LONGITUDE         Global longitude coordinate of Node (best guess in Kubernetes)
HOSTNAME             HOSTNAME          Defaults to SYSTEM provided "hostname". Use when System doesn't report what is actually needed, such as vanity urls, multi-NIC cards, short names, i.e. htydb77 reported instead of htydb77.some.company.org, etc.
Docker User          DUSER             User needed inside the Docker Container.  Without, this will be root
Container NS         CONTAINER_NS      The Namespace for the container.  Provided for Multi-NS support, this would be "onap" for Test OOM, etc.
==================== ================= ============

=================================
Typical ONAP Entity Info in AAF
=================================
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

===============
Informational
===============

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












