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
# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
DOCKER=${DOCKER:-docker}
if [ "$DOCKER" = "podman" ]; then
  PODNAME=aaf-cass.onap
  if $(podman pod exists $PODNAME); then
     echo "Using existing 'podman' pod $PODNAME"
     POD="--pod $PODNAME "
  else
     echo "Create new 'podman' pod $PODNAME"
     # Note: Cassandra needs "infra" to work
     #  Keep in separate pod
     #podman pod create --infra=true -n $PODNAME --publish 9042:9042
     podman pod create --infra=false -n $PODNAME 
     #POD="--pod new:$PODNAME "
     POD="--pod $PODNAME "
     PUBLISH='--publish 9042:9042 '
  fi
else
  PUBLISH='--publish 9042:9042 '
fi

if [ "$($DOCKER volume ls | grep aaf_cass_data)" = "" ]; then
  $DOCKER volume create aaf_cass_data
  echo "Created Cassandra Volume aaf_cass_data"
fi

# Optional mount instead of v
#    --mount 'type=volume,src=aaf_cass_data,dst=/var/lib/cassandra,volume-driver=local' \
if [ "`$DOCKER ps -a | grep aaf-cass`" == "" ]; then
  echo "starting Cass from 'run'"
  # NOTE: These HEAP Sizes are minimal. Not set for full organizations.
  #  --user ${USER} \
  $DOCKER run \
    --name aaf-cass \
    -e HEAP_NEWSIZE=512M \
    -e MAX_HEAP_SIZE=1024M \
    -e CASSANDRA_DC=dc1 \
    -e CASSANDRA_CLUSTER_NAME=osaaf \
    -v "aaf_cass_data:/var/lib/cassandra" \
    -v "aaf_status:/opt/app/aaf/status" \
    ${POD} \
    $PUBLISH \
    -d ${PREFIX}${ORG}/${PROJECT}/aaf_cass:${VERSION} "onap"
else 
  $DOCKER start aaf-cass
fi
