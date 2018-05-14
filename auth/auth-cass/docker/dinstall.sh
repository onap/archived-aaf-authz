#!/bin/bash 
DOCKER=/usr/bin/docker
echo "Running DInstall"
if [ "`$DOCKER ps -a | grep aaf_cass`" == "" ]; then
  echo "starting Cass from 'run'"
  $DOCKER run --name aaf_cass  -d cassandra:3.11
  echo "aaf_cass Starting"
  for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
     if [ "`$DOCKER container logs aaf_cass | grep 'listening for CQL clients'`" == "" ]; then
	 	echo "Sleep 10"
        sleep 10
     else 
     	break
     fi
  done
  
  echo "Running Phase 2 DInstall"
  $DOCKER container ps
  
  echo "Creating /opt/app/cass_init dir on aaf_cass"
  $DOCKER exec aaf_cass mkdir -p /opt/app/cass_init
  echo "cp the following files to /opt/app/cass_init dir on aaf_cass"
  ls ../src/main/cql
  $DOCKER cp "../src/main/cql/." aaf_cass:/opt/app/cass_init
  echo "The following files are on /opt/app/cass_init dir on aaf_cass"
  $DOCKER exec aaf_cass ls /opt/app/cass_init
  
  echo "Docker Installed Basic Cassandra on aaf_cass.  Executing the following "
  echo "NOTE: This creator provided is only a Single Instance. For more complex Cassandra, create independently"
  echo ""
  echo " cd /opt/app/cass_init"  
  echo " cqlsh -f keyspace.cql"
  echo " cqlsh -f init.cql"
  echo " cqlsh -f osaaf.cql"
  echo ""
  echo "The following will give you a temporary identity with which to start working, or emergency"
  echo " cqlsh -f temp_identity.cql"
  echo "Create Keyspaces and Tables"
  $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/keyspace.cql
  $DOCKER exec aaf_cass bash /usr/bin/cqlsh -e 'describe keyspaces'
  $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/init.cql
  $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/osaaf.cql
  $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/temp_indentity.cql
else 
  $DOCKER start aaf_cass
fi
