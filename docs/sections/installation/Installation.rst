.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Installation
============
This document will illustrates how to build and deploy all AAF components.

Clone AAF Code:
Build AAF with settings.xml:
Build Docker Images:
Modify the  properties file:
Mount the sample to /opt/app/osaaf:
Run the docker containers:
Clone AAF Code:
bharath@bharath:~$ git clone https://git.onap.org/aaf/authz


Build AAF with settings.xml:
---------------------------
Copy the settings.xml from here and paste in ~/.m2/settings.xml

Then run the following command

.. code:: bash

    bharath@bharath:~$ cd authz && mvn clean install -DskipTests


If the build is successful, then you can see a folder in "authz/auth" called "aaf_VERSION-SNAPSHOT" which contains all binaries of the components

.. code:: bash

   bharath@bharath:~/authz/auth$ ls
aaf_2.1.1-SNAPSHOT  auth-cass     auth-cmd   auth-deforg  auth-gui    auth-locate  auth-service  pom.xml  target
auth-batch          auth-certman  auth-core  auth-fs      auth-hello  auth-oauth   docker        sample

Build Docker Images:
-------------------
Now after building binaries, the next step is to build docker images for each aaf component.

.. code:: bash

    bharath@bharath:~/authz/auth/docker$ chmod +x *.sh
    bharath@bharath:~/authz/auth/docker$ ./dbuild.sh
	
The above command will build the following images:

aaf_service
aaf_oauth
aaf_locate
aaf_hello
aaf_gui
aaf_fs
aaf_cm
Modify the  properties file:
Modify the contents of the "authz/auth/docker/d.props

.. code:: bash

    bharath@bharath:~/authz/auth/docker$ cat d.props
	
# Variables for building Docker entities
ORG=onap
PROJECT=aaf
DOCKER_REPOSITORY=nexus3.onap.org:10003
OLD_VERSION=2.1.0-SNAPSHOT
VERSION=2.1.1-SNAPSHOT
CONF_ROOT_DIR=/opt/app/osaaf


# Local Env info
HOSTNAME="<HOSTNAME>"
HOST_IP="<HOST_IP>"
CASS_HOST="cass"

Replace the <HOSTNAME>  with your hostname and HOST_IP with your host IP.

Add  the following entry to your /etc/hosts file



127.0.0.1 aaf.osaaf.org
Mount the sample to /opt/app/osaaf:
As you can see there is a parameter "CONF_ROOT_DIR" which is set to "/opt/app/osaaf". So we have to create a folder "/opt/app/osaaf" and copy the contents of authz/auth/sample to /opt/app/osaaf

.. code:: bash

   bharath@bharath:~/authz/auth$ mkdir -p /opt/app/osaaf
   bharath@bharath:~/authz/auth$ cp -r sample/* /opt/app/osaaf/

Run the docker containers:
--------------------------
.. code:: bash

    bharath@bharath:~/authz/auth/docker$ ls
    dbash.sh  dbuild.sh  dclean.sh  Dockerfile  d.props  dpush.sh  drun.sh  dstart.sh  dstop.sh
    bharath@bharath:~/authz/auth/docker$ ./drun.sh






