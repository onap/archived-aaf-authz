.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

============================
Building 
============================
*Note: this document assumes UNIX Bash Shell.  Being Java, AAF works in Windows, but you will have to create your own script/instruction conversions.*

---------------------
Building from Source 
---------------------
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

------------------
Existing Cassandra
------------------

AAF Casablanca has added a table.  If you have an existing AAF Cassandra, do the following *ONCE* :

### If Container Cassandra, add these steps, otherwise, skip
$ docker container cp init2_1.cql aaf_cass:/tmp
$ docker exec -it aaf_cass bash
(docker) $ cd /tmp
###
$ cqlsh -f 'init2_1.cql'

--------------------
New Docker Cassandra
--------------------

Assuming you are in your src/authz directory::
$ cd auth/auth-cass/docker
$ bash dinstall.sh

---------------------
AAF Itself
---------------------

Assuming you are in your src/authz directory::

| $ cd auth/docker
| ### If you have not done so before (don't overwrite your work!)
| $ cp d.props.init d.props

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

------------------------------
"Bleeding Edge" Source install
------------------------------

AAF can be built, and local Docker Images built with the following::

  $ bash dbuild.sh

Otherwise, just let it pull from Nexus

