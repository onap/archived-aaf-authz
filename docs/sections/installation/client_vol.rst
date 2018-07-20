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

The Agent will look for "aaf.props", and if it doesn't exist, or is missing information, it will ask for it


--------------- ---------------
Tag             Value
--------------- ---------------
CADI Version    Defaults to CADI version of this
AAF's FQDN      PUBLIC Name for AAF. For ONAP Test, it is 'aaf-onap-test.osaaf.org'
Deployer's FQI  deployer@people.osaaf.org.  In a REAL system, this would be a person or process 
App's Root FQDN This will show up in the Cert Subject, and should be the name given by Docker. i.e. clamp.onap
App's FQI       Fully Qualified ID given by Organization and with AAF NS/domain.  ex: clamp@clamp.onap.org 
App's Volume    Volume to put the data, see above. ex: clamp_aaf
DRIVER		Docker Volume type... See Docker Volume documentation
LATITUDE	Global latitude coordinate of Node (best guess for Kubernetes)
LONGITUDE	Global longitude coordinate of Node (best guess for Kubernetes)
--------------- ---------------



