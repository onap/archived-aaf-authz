#!/bin/bash 
DOCKER=/usr/bin/docker

if [ "`$DOCKER ps -a | grep aaf_cass`" == "" ]; then
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
  echo "Check for running Docker Container aaf_cass, then run again."
  exit
fi 

sleep 20
echo "Running containers"
$DOCKER container ps

echo "Creating /opt/app/cass_init dir on aaf_cass"
$DOCKER exec aaf_cass mkdir -p /opt/app/cass_init
echo "cp the following files to /opt/app/cass_init dir on aaf_cass"
ls ../src/main/cql
$DOCKER cp "../src/main/cql/." aaf_cass:/opt/app/cass_init
echo "The following files are on /opt/app/cass_init dir on aaf_cass"
$DOCKER exec -it aaf_cass ls /opt/app/cass_init

echo "Docker Installed Basic Cassandra on aaf_cass.  Executing the following "
echo "NOTE: This creator provided is only a Single Instance. For more complex Cassandra, create independently"
echo ""
echo " cd /opt/app/cass_init"  
echo " cqlsh -u root -p root -f keyspace.cql"
echo " cqlsh -u root -p root -f init.cql"
echo " cqlsh -u root -p root -f osaaf.cql"
echo ""
echo "The following will give you a temporary identity with which to start working, or emergency"
echo " cqlsh -u root -p root -f temp_identity.cql"
echo "Create Keyspaces and Tables"
$DOCKER exec -it aaf_cass bash -c '\
cd /opt/app/cass_init; \
echo "Creating Keyspace";cqlsh -u root -p root -f keyspace.cql;\
echo "Creating init";cqlsh -u root -p root -f init.cql;\
echo "Creating osaaf";cqlsh -u root -p root -f osaaf.cql;\
echo "Creating temp Identity";cqlsh -u root -p root -f temp_identity.cql'

echo "Inspecting aafcassadra.  Use to get the IP address to update org.osaaf.cassandra.props"
$DOCKER inspect aaf_cass | grep '"IPAddress' | head -1
