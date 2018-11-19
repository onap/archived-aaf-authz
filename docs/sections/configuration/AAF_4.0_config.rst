.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

============================
Configuring
============================
*Note: this document assumes UNIX Bash Shell.  Being Java, AAF works in Windows, but you will have to create your own script/instruction conversions.*

--------------------
Configure AAF Volume
--------------------

AAF uses a Persistent Volume to store data longer term, such as CADI configs, Organization info, etc, so that data is not lost when changing out a container.

This volume is created automatically, as necessary, and linked into the container when starting. ::

  ## Be sure to have your 'd.props' file filled out before running.
  $ bash aaf.sh

----------------------------
Bootstrapping with Keystores
----------------------------

Start the container in bash mode, so it stays up. ::

  $ bash aaf.sh bash
  id@77777: 

In another shell, find out your Container name. ::
  
  $ docker container ls | grep aaf_config

CD to directory with CA p12 files 
  
  * org.osaaf.aaf.p12
  * org.osaaf.aaf.signer.p12    (if using Certman to sign certificates)

Copy keystores for this AAF Env ::

  $ docker container cp -L org.osaaf.aaf.p12 aaf_agent_<Your ID>:/opt/app/osaaf/local
  ### IF using local CA Signer 
  $ docker container cp -L org.osaaf.aaf.signer.p12 aaf_agent_<Your ID>:/opt/app/osaaf/local

In Agent Window ::

  id@77777: agent encrypt cadi_keystore_password
  ### IF using local CA Signer 
  id@77777: agent encrypt cm_ca.local 

Check to make sure all passwords are set ::

  id@77777: grep "enc:" *.props

When good, exit from Container Shell and run AAF ::

  id@77777: exit
  $ bash drun.sh

Check the Container logs for correct Keystore passwords, other issues ::

  $ docker container logs aaf_<service>

Watch logs ::

  $ bash aaf.sh taillog

Notes:

You can find an ONAP Root certificate, and pre-built trustores  for ONAP Test systems at:
  | authz/auth/sample/public/AAF_RootCA.cert
  | authz/auth/sample/public/truststoreONAPall.jks

Good Tests to run ::

  ## From "docker" dir
  ##
  ## assumes you have DNS or /etc/hosts entry for aaf-onap-test.osaaf.org
  ##
  $ curl --cacert ../sample/public/AAF_RootCA.cer -u demo@people.osaaf.org:demo123456! https://aaf-onap-test.osaaf.org:8100/authz/perms/user/demo@people.osaaf.org
  $ openssl s_client -connect aaf-onap-test.osaaf.org:8100

-------------------------------
Working with CADI Properties
-------------------------------

.. code:: bash

   cadi.properties Template
  # This is a normal Java Properties File
  # Comments are with Pound Signs at beginning of lines,
  # and multi-line expression of properties can be obtained by backslash at end of line
  #hostname=

  cadi_loglevel=WARN
  cadi_keyfile=conf/keyfile


  # Configure AAF
  aaf_url=http://172.18.0.2:8101
  #if you are running aaf service from a docker image you have to use aaf service IP and port number
  aaf_id=<yourAPPID>@onap.org
  aaf_password=enc:<encrypt>

  aaf_dme_timeout=5000
  # Note, User Expires for not Unit Test should be something like 900000 (15 mins) default is 10 mins
  # 15 seconds is so that Unit Tests don't delay compiles, etc
  aaf_user_expires=15000
  # High count... Rough top number of objects held in Cache per cycle.  If high is reached, more are
  # recycled next time.  Depending on Memory usage, 2000 is probably decent.  1000 is default
  aaf_high_count=100


How to create CADI Keyfile & Encrypt Password
---------------------------------------------

Password Encryption
-------------------
CADI provides a method to encrypt data so that Passwords and other sensitive data can be stored safely.

Keygen (Generate local Symmetrical Key)
A Keyfile is created by Cadi Utility.

.. code:: bash

  java -jar cadi-core-<version>.jar keygen <keyfile>
  Given this key file unlocks any passwords created, it should be stored in your configuration directory and protected with appropriate access permissions. For instance, if your container is Tomcat, and runs with a "tomcat" id, then you should:

.. code:: bash

  java -jar cadi-core-<version>.jar keygen keyfile
  chmod 400 keyfile
  chown tomcat:tomcat keyfile
  
Digest - Encrypt a Password
---------------------------
The password is obtained by using the Cadi digest Utility (contained in the cadi-core-<version>.jar).

.. code:: bash

  java -jar cadi-core-<version>.jar digest <your_password> <keyfile>
   •	"<keyfile>" is created by Cadi Utility, #keygen
   •	Understand that if you change the keyfile, then you need to rerun "digest" on passwords used in the users/groups definitions.
   •	Note: You cannot mix versions of cadi; the version used to digest your password must be the same version used at runtime.
   
CADI PROPERTIES
   CADI properties, typically named "cadi.properties", must have passwords encrypted.
      1.	Take the results of the "Digest" command and prepend "enc:"
      2.	Use this as the value of your property
	  
Example:   aaf_password=enc:fMKMBfKHlRWL68cxD5XSIWNKRNYi5dih2LEHRFMIsut

