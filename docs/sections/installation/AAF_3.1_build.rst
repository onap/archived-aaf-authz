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
 
  $ git clone --depth 1 https://gerrit.onap.org/r/aaf/authz

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

-----------------------------
Existing Cassandra Instance
-----------------------------

When AAF modifies or adds a Cassandra Table, it is entered in two places:
	- COMPLETE Table Schemes for Startup:  authz/auth/auth-cass/cass_init/init.cql
	- INCREMENTAL Table Schemes for Existing AAF Setups:  authz/auth/auth-cass/cass_init/init<Interface Version>.cql

### As an example, Assume interface change of "2_10" AND you have an existing Cassandra, add these steps, otherwise, skip
$ docker container cp init2_10.cql aaf_cass:/tmp
$ docker exec -it aaf_cass bash
(docker) $ cd /tmp
###
$ cqlsh -f 'init2_10.cql'

--------------------
New Docker Cassandra
--------------------

Assuming you are in your src/authz directory::
| $ cd auth/auth-cass/docker
| $ bash dinstall.sh

FOR DEVELOPMENT:
Normally, Cassandra in Containers are NOT published external to Docker for security exposure reasons.  HOWEVER, you can do the above as follows and then be able to access the DB while coding, debugging, etc::

| $ bash dinstall.sh publish

---------------------
AAF Itself
---------------------

Assuming you are in your src/authz directory::

| $ cd auth/docker
| ### If you have not done so before (don't overwrite your work!)
| $ cp d.props.init d.props

Adjust for ACTUAL AAF version if required.

There are two special scripts created.
	- "aaf.sh" - The script that creates and accesses AAF's Configuration information.  It uses "d.props"
	- "agent.sh" - A script for use by MSs OTHER than AAF (i.e. aai) for creating AAF Configuration. It uses "aaf.props".  (note, need to have AAF running to generate Certificates)

In BOTH cases, the scripts will ask for Properties required that are not in the current d.props or 

------------------------------
"Bleeding Edge" Source install
------------------------------

AAF can be built, and local Docker Images built with the following::

  $ bash dbuild.sh

Otherwise, just let it pull from Nexus

------------------------------
Other Scripts
------------------------------
The following will act on ALL possible AAF instances if there are no Params.  You can apply to only ONE MS by adding short name i.e. "service" instead of "aaf_service"
  - drun.sh - starts up Docker FRESH instances of AAF locally (not K8s)
  - dstop.sh - stops Docker instances of AAF locally
  - dclean.sh - cleans up containers in Docker so you can do "dbuild.sh" (above) and start fresh
  - dstart.sh - starts Docker containers that were previously stopped... Doesn't refresh actual container.
  - dbounce.sh - stops and starts in one method




