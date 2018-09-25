#!/bin/bash 

# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
else
  . ../../docker/d.props.init
fi

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
    -d ${ORG}/${PROJECT}/aaf_cass:${VERSION} 
else 
  $DOCKER start aaf_cass
fi
