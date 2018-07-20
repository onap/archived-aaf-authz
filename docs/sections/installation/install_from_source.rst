.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Installing from Source Code
============================

*Note: this document assumes UNIX Bash Shell.  Being AAF works in Windows, but you will have to create your own script/instruction conversions.*

------------------
Modes
------------------

AAF can be run in various ways
  * Standalone (on your O/S)
  * Docker (localized)
  * Kubernetes
  * ONAP Styles
    * HEAT (Docker Container Based Initilization)
    * OOM  (a Helm Chart based Kubernetes Environment)

------------------
Prerequisites
------------------

You need the following tools to build and run AAF
  * git
  * maven
  * Java (JDK 1.8+, openjdk is fine)
  * Cassandra
     * a separate installation is fine
     * these instructions will start off with a Docker based Cassandra instance
  * Machine - one of the following
     * Standalone Java Processes - no additional running environments necessary
     * docker - typically available via packages for O/S
     * kubernetes - ditto
     

------------------
Build from Source
------------------
Choose the directory you wish to start in... This process will create an "authz" subdirectory::

  $ mkdir -p ~/src
  $ cd ~/src

Use 'git' to 'clone' the master code::
 
  $ git clone https://gerrit.onap.org/r/aaf/authz

Change to that directory::

  $ cd authz

Use Maven to build::

  << TODO, get ONAP Settings.xml>>
  $ mvn install

.. -----------------
.. Standalone
.. -----------------

-----------------
Docker Mode
-----------------

After you have successfully run maven, you will need a Cassandra.  If you don't have one, here are instructions for a Docker Standalone Cassandra.  For a *serious* endeavor, you need a multi-node Cassandra.

From "authz"::

  $ cd auth/auth-cass/src/main/cql
  $ vi config.dat

===================
Existing Cassandra
===================

AAF Casablanca has added a table.  If you have an existing AAF Cassandra, do the following::

  ### If Container Cassandra, add these steps, otherwise, skip
  $ docker container cp init2_1.cql aaf_cass:/tmp
  $ docker exec -it aaf_cass bash
  (docker) $ cd /tmp
  ###
  $ cqlsh -f 'init2_1.cql'

=====================
New Docker Cassandra
=====================

Assuming you are in your src/authz directory::

  $ cd auth/auth-cass/docker
  $ sh dinstall.sh

---------------------
AAF Itself
---------------------

Assuming you are in your src/authz directory::

  $ cd auth/docker
  ### If you have not done so before (don't overwrite your work!)
  $ cp d.props.init d.props

You will need to edit and fill out the information in your d.props file.  Here is info to help

**Local Env info** - These are used to load the /etc/hosts file in the Containers, so AAF is available internally and externally

  =============== =============
  Variable        Explanation
  =============== =============
  HOSTNAME        This must be the EXTERNAL FQDN of your host.  Must be in DNS or /etc/hosts
  HOST_IP         This must be the EXTERNAL IP of your host. Must be accessible from "anywhere"
  CASS_HOST       If Docker Cass, this is the INTERNAL FQDN/IP.  If external Cass, then DNS|/etc/hosts entry
  aaf_env         This shows up in GUI and certs, to differentiate environments
  aaf_register_as As pre-set, it is the same external hostname.
  cadi_latitude   Use "https://bing.com/maps", if needed, to locate your current Global Coords
  cadi_longitude  ditto
  =============== =============

==============================
"Bleeding Edge" Source install
==============================

AAF can be built, and local Docker Images built with the following::

  $ sh dbuild.sh

Otherwise, just let it pull from Nexus

==============================
Configure AAF Volume
==============================

AAF uses a Persistent Volume to store data longer term, such as CADI configs, Organization info, etc, so that data is not lost when changing out a container.

This volume is created automatically, as necessary, and linked into the container when starting. ::

  ## Be sure to have your 'd.props' file filled out before running.
  $ sh aaf.sh

==============================
Bootstrapping with Keystores
==============================

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

  $ sh aaf.sh taillog

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














