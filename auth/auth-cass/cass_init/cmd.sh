#!/bin/bash 
#########
#  ============LICENSE_START====================================================
#  org.onap.aaf
#  ===========================================================================
#  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
#
# Engage normal Cass Init, then check for data installation
#
DIR="/opt/app/aaf/status"
INSTALLED_VERSION=/var/lib/cassandra/AAF_VERSION
AAF_INIT_DATA=/var/lib/cassandra/AAF_INIT_DATA
CQLSH=${CQLSH:=/opt/cassandra/bin/cqlsh}

if [ ! -e /aaf_cmd ]; then
  ln -s /opt/app/aaf/cass_init/cmd.sh /aaf_cmd
  chmod u+x /aaf_cmd
fi

# Always need startup status...
if [ ! -e "$DIR" ]; then
  mkdir -p "$DIR"
  chmod 777 $DIR
fi

function status {
     echo "$@"
     echo "$@" > $DIR/aaf-cass
}

function wait_start {
    sleep 10
    status wait for cassandra to start
    for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
      if [ -z "$(grep 'listening for CQL clients' /var/log/cassandra/system.log)" ]; then
        echo "Waiting for Cassandra to start... Sleep 10"
        sleep 10
      else
         status cassandra started
         break
      fi
    done
    # Logs state Cassandra is up.  Now use cqlsh to ensure responsive
    echo "Cassandra started, wait until it is responsive"
    for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
      if  [ -z "$(cqlsh -e 'describe keyspaces')" ]; then
        echo "Waiting for Cassandra to be responsive... Sleep 10"
        sleep 10
      else
        echo "Cassandra responded"
        status cassandra responsive
	break
      fi
    done 
}


function wait_cql {
   status wait for keyspace to be initialized
   for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
     if [ -n "$($CQLSH -e 'describe keyspaces' | grep authz)"  ]; then
	break
     else
        echo "Waiting for Keyspaces to be loaded... Sleep 10"
        sleep 10
      fi
    done
}

function wait_ready {
   status wait for cassandra to be fully ready
   for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
       STATUS="$(cat $DIR/aaf-cass)"
       if [ "$STATUS" = "ready" ]; then
	break
     else
        echo "Waiting for Start, $STATUS... Sleep 10"
        sleep 10
      fi
    done
}

function install_cql {
    wait_start cassandra responsive   
    # Now, make sure data exists
    if [ ! -e $INSTALLED_VERSION ] && [ -n "$($CQLSH -e 'describe keyspaces' | grep authz)" ]; then
      $CQLSH --request-timeout=60 -e 'DROP KEYSPACE authz'
    fi

    if [ -z "$($CQLSH --request-timeout 60 -e 'describe keyspaces' | grep authz)" ]; then
        status install 
        echo "Initializing Cassandra DB" 
        echo "Docker Installed Basic Cassandra on aaf.cass.  Executing the following "
        echo "NOTE: This creator provided is only a Single Instance. For more complex Cassandra, create independently"
        echo ""
        echo " cd /opt/app/aaf/cass_init"
        cd /opt/app/aaf/cass_init
        echo " cqlsh -f keyspace.cql"
        $CQLSH --request-timeout=100 -f keyspace.cql
	status keyspace installed
        echo " cqlsh -f init.cql"
        $CQLSH --request-timeout=100 -f init.cql
	status data initialized
        echo ""
        echo "The following will give you a temporary identity with which to start working, or emergency"
        echo " cqlsh -f temp_identity.cql"
        echo "frankfurt" > $INSTALLED_VERSION
    else 
      echo "Cassandra DB already includes 'authz' keyspace"
    fi
    status $1
}

function install_onap {
    echo " cd /opt/app/aaf/cass_init"
    install_cql initialized
    if [ -e "$AAF_INIT_DATA" ]; then 
       echo "AAF Data already initialized on this Cassandra"
    else 
      status prep data for bootstrapping
      cd /opt/app/aaf/cass_init
      status prep data 
      bash prep.sh
      status push data to cassandra
      # bash push.sh
      echo "YES" | bash restore.sh
      cd -
      echo $(date) > $AAF_INIT_DATA
    fi
    status ready
}

case "$1" in
  start)
    # start install_cql in background, waiting for process to start
    install_cql ready &

    # Startup like normal
    echo "Cassandra Startup"
    exec -c "/usr/local/bin/docker-entrypoint.sh"
  ;;
  wait)
    # Wait for initialization.  This can be called from Docker only as a check to make sure it is ready
    wait_ready 

  ;;
  onap)
    cd /opt/app/aaf/cass_init
    # start install_onap (which calls install_cql first) in background, waiting for process to start
    install_onap &

    # Startup like normal
    echo "Cassandra Startup"
    if ! cat /etc/cassandra/cassandra.yaml | grep "write_request_timeout_in_ms: 20000"; then
      sed -i 's/write_request_timeout_in_ms: 2000/write_request_timeout_in_ms: 20000/' /etc/cassandra/cassandra.yaml
    fi
    exec /usr/local/bin/docker-entrypoint.sh 
  ;;
esac

