#!/bin/bash 
if [ -e /usr/bin/docker ]; then 
  DOCKER=/usr/bin/docker
elif [ -e /usr/local/bin/docker ]; then
  DOCKER=/usr/local/bin/docker
else
  echo Docker not available in /usr/bin or /usr/local/bin
  exit
fi

if [ "$($DOCKER volume ls | grep aaf_cass_data)" = "" ]; then
  $DOCKER volume create aaf_cass_data
  echo "Created Cassandra Volume aaf_cass_data"
fi

echo "Running DInstall"
if [ "`$DOCKER ps -a | grep aaf_cass`" == "" ]; then
  echo "starting Cass from 'run'"
  # NOTE: These HEAP Sizes are minimal. Not set for full organizations.
  $DOCKER run \
    --name aaf_cass \
    -e HEAP_NEWSIZE=512M \
    -e MAX_HEAP_SIZE=1024M \
    -e CASSANDRA_DC=dc1 \
    -e CASSANDRA_CLUSTER_NAME=osaaf \
    --mount 'type=volume,src=aaf_cass_data,dst=/var/lib/cassandra,volume-driver=local' \
    -d cassandra:3.11 
  # Set on local Disk
  # -v /opt/app/cass:/var/lib/cassandra 
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
  $DOCKER cp "../src/main/cql/." aaf_cass:/opt/app/cass_init
  echo "The following files are on /opt/app/cass_init dir on aaf_cass"
  $DOCKER exec aaf_cass ls /opt/app/cass_init
  
  if [ "`$DOCKER exec aaf_cass /usr/bin/cqlsh -e 'describe keyspaces' | grep authz`" == "" ]; then
    echo "Docker Installed Basic Cassandra on aaf_cass.  Executing the following "
    echo "NOTE: This creator provided is only a Single Instance. For more complex Cassandra, create independently"
    echo ""
    echo " cd /opt/app/cass_init"  
    echo " cqlsh -f keyspace.cql"
    echo " cqlsh -f init.cql"
    echo ""
    echo "The following will give you a temporary identity with which to start working, or emergency"
    echo " cqlsh -f temp_identity.cql"
    echo "Create Keyspaces and Tables"
    $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/keyspace.cql
    $DOCKER exec aaf_cass bash /usr/bin/cqlsh -e 'describe keyspaces'
    $DOCKER exec aaf_cass bash /usr/bin/cqlsh -f /opt/app/cass_init/init.cql
    cd ../../sample/cass_data
    ID_FILE=../data/identities.dat 
    if [ -e $ID_FILE ]; then
      DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -d "+6 months")
      echo $DATE
      CRED="cred.dat"
      # Enter for People
      echo "Default Passwords for People"
      for ID in $(grep '|a|' $ID_FILE | sed -e "s/|.*//"); do
	 if [ "$ID" = "aaf" ]; then
	    DOMAIN="aaf.osaaf.org";
	 else
            DOMAIN="$ID.onap.org";
	 fi
	 echo "$ID@$DOMAIN|2|${DATE}|0xd993c5617486296f1b99d04de31633332b8ba1a550038e23860f9dbf0b2fcf95|Initial ID|org.osaaf.people|53344|" >> $CRED
      done
    
      for ID in $(grep '|e|' $ID_FILE | sed -e "s/|.*//"); do
	 echo "$ID@people.osaaf.org|2|${DATE}|0xd993c5617486296f1b99d04de31633332b8ba1a550038e23860f9dbf0b2fcf95|Initial ID|org.osaaf.people|53344|" >> $CRED
      done
    
      mv user_role.dat tmp
      sed "s/\(^.*|\)\(.*|\)\(.*|\)\(.*\)/\1${DATE}|\3\4/" tmp > user_role.dat 

      for DAT in ns perm role ns_attrib user_role cred; do 
          $DOCKER container cp $DAT.dat aaf_cass:/tmp/$DAT.dat
          $DOCKER exec aaf_cass bash /usr/bin/cqlsh -k authz -e "COPY authz.$DAT FROM '/tmp/$DAT.dat' WITH DELIMITER='|'"
          $DOCKER exec -t aaf_cass rm /tmp/$DAT.dat
      done
      rm $CRED
      mv tmp user_role.dat
    else
        echo DInstall requires access to 'identities.dat'
    fi
    cd -
  fi
else 
  $DOCKER start aaf_cass
fi
