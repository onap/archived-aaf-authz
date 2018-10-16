#!/bin/bash 
#
# Engage normal Cass Init, then check for data installation
#
DIR="/opt/app/aaf/status"
INSTALLED_VERSION=/var/lib/cassandra/AAF_VERSION

if [ ! -e /aaf_cmd ]; then
  ln -s /opt/app/aaf/cass_init/cmd.sh /aaf_cmd
  chmod u+x /aaf_cmd
fi

# Always need startup status...
if [ ! -e "$DIR" ]; then
  mkdir -p "$DIR"
fi

function status {
     echo "$@"
     echo "$@" > $DIR/aaf_cass
}

function wait_start {
    sleep 10
    status wait for cassandra to start
    for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
      if [ -z "$(grep 'listening for CQL clients' /var/log/cassandra/system.log)" ]; then
        echo "Waiting for Cassandra to start... Sleep 10"
        sleep 10
      else
         break
      fi
    done
}


function wait_cql {
   status wait for keyspace to be initialized
   for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
     if [ -n "$(/usr/bin/cqlsh -e 'describe keyspaces' | grep authz)"  ]; then
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
       STATUS="$(cat $DIR/aaf_cass)"
       if [ "$STATUS" = "ready" ]; then
	break
     else
        echo "Waiting for Start, $STATUS... Sleep 10"
        sleep 10
      fi
    done
}

function install_cql {
    wait_start started   
    # Now, make sure data exists
    if [ ! -e $INSTALLED_VERSION ] && [ -n "$(/usr/bin/cqlsh -e 'describe keyspaces' | grep authz)" ]; then
      /usr/bin/cqlsh -e 'DROP KEYSPACE authz' 
    fi
    if [ -z "`/usr/bin/cqlsh -e 'describe keyspaces' | grep authz`" ]; then
        status install 
        echo "Initializing Cassandra DB" 
        echo "Docker Installed Basic Cassandra on aaf_cass.  Executing the following "
        echo "NOTE: This creator provided is only a Single Instance. For more complex Cassandra, create independently"
        echo ""
        echo " cd /opt/app/aaf/cass_init"
        cd /opt/app/aaf/cass_init
        echo " cqlsh -f keyspace.cql"
        /usr/bin/cqlsh -f keyspace.cql
	status keyspace installed
        echo " cqlsh -f init.cql"
        /usr/bin/cqlsh -f init.cql
	status data initialized
        echo ""
        echo "The following will give you a temporary identity with which to start working, or emergency"
        echo " cqlsh -f temp_identity.cql"
        echo "2.1.15"> $INSTALLED_VERSION
    else 
      echo "Cassandra DB already includes 'authz' keyspace"
    fi
    status $1
}

function install_onap {
    echo " cd /opt/app/aaf/cass_init"
    install_cql initialized
    status prep data for bootstrapping
    cd /opt/app/aaf/cass_init
    bash prep.sh
    status push data to cassandra
    bash push.sh
    cd -
    status ready
}

case "$1" in
  start)
    # start install_cql in background, waiting for process to start
    install_cql ready &

    # Startup like normal
    echo "Cassandra Startup"
    /usr/local/bin/docker-entrypoint.sh 
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
    /usr/local/bin/docker-entrypoint.sh 
  ;;
esac

