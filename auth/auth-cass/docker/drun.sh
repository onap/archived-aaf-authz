#!/bin/bash

# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
DOCKER=${DOCKER:-docker}

if [ "$1" = "publish" ]; then
  PUBLISH='--publish 9042:9042 '
fi

if [ "$($DOCKER volume ls | grep aaf_cass_data)" = "" ]; then
  $DOCKER volume create aaf_cass_data
  echo "Created Cassandra Volume aaf_cass_data"
fi

# Optional mount instead of v
#    --mount 'type=volume,src=aaf_cass_data,dst=/var/lib/cassandra,volume-driver=local' \
if [ "`$DOCKER ps -a | grep aaf_cass`" == "" ]; then
  echo "starting Cass from 'run'"
  # NOTE: These HEAP Sizes are minimal. Not set for full organizations.
  $DOCKER run \
    --name aaf_cass \
    -e HEAP_NEWSIZE=512M \
    -e MAX_HEAP_SIZE=1024M \
    -e CASSANDRA_DC=dc1 \
    -e CASSANDRA_CLUSTER_NAME=osaaf \
    -v "aaf_cass_data:/var/lib/cassandra" \
    -v "aaf_status:/opt/app/aaf/status" \
    $PUBLISH \
    -d ${PREFIX}${ORG}/${PROJECT}/aaf_cass:${VERSION} "onap"
else 
  $DOCKER start aaf_cass
fi
