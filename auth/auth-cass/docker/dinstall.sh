#!/bin/bash 

if [ "`docker ps -a | grep aaf_cass`" == "" ]; then
  docker run --name aaf_cass  -d cassandra:3.11
  echo "aaf_cass Starting"
  echo "Check for running Docker Container aaf_cass, then run again."
  # we have to exit here so that the calling script can load CQL files
  exit
else
  for CNT in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
     if [ "`docker container logs aaf_cass | grep 'listening for CQL clients'`" == "" ]; then
	 	echo "Sleep 10"
        sleep 10
     else 
	 	YESCQL="yes"
     	break
     fi
  done
fi 

if [ "$YESCQL" == "" ]; then
  echo "CQL Never started... exiting"
  exit
fi

docker exec aaf_cass mkdir -p /opt/app/cass_init
docker cp "../src/main/cql/." aaf_cass:/opt/app/cass_init

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
echo "Sleeping for 20 seconds"

echo "Create Keyspaces and Tables"
docker exec -it aaf_cass bash -c '\
cd /opt/app/cass_init; \
echo "Creating Keyspace";cqlsh -u root -p root -f keyspace.cql;\
echo "Creating init";cqlsh -u root -p root -f init.cql;\
echo "Creating osaaf";cqlsh -u root -p root -f osaaf.cql;\
echo "Creating temp Identity";cqlsh -u root -p root -f temp_identity.cql'

echo "Inspecting aafcassadra.  Use to get the IP address to update org.osaaf.cassandra.props"
docker inspect aaf_cass | grep '"IPAddress' | head -1
