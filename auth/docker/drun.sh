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
# Pull in Variables from d.props
. ./d.props

CASSANDRA_CLUSTER=${CASSANDRA_CLUSTER:=$CASSANDRA_DOCKER}

DOCKER=${DOCKER:=docker}

# Running without params keeps from being TTY
bash ./aaf.sh 

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    LINKS=""
    CMD_LINE=""
    PUBLISH=""
    case "$AAF_COMPONENT" in
    "service")
        PUBLISH="--publish 8100:8100"
        if [ -z "$CASSANDRA_DOCKER" ]; then
	  CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-service && exec bin/service"
        else
	  CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-service aaf-cass && exec bin/service"
          LINKS="--link $CASSANDRA_DOCKER"
	  echo $CASSANDRA_CLUSTER
        fi
        ;;
    "locate")
        PUBLISH="--publish 8095:8095"
        LINKS="--link aaf-cass --link aaf-service"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-locate aaf-service && exec bin/locate"
        ;;
    "oauth")
        PUBLISH="--publish 8140:8140"
        LINKS="--link aaf-cass --link aaf-service --link aaf-locate"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-oauth aaf-service && exec bin/oauth"
        ;;
    "cm")
        PUBLISH="--publish 8150:8150"
        LINKS="--link aaf-cass --link aaf-service --link aaf-locate --link aaf-oauth"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-cm aaf-locate && exec bin/cm"
        ;;
    "gui")
        PUBLISH="--publish 8200:8200"
        LINKS="--link aaf-service --link aaf-locate --link aaf-oauth --link aaf-cm"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-gui aaf-locate && exec bin/gui"
        ;;
    "fs")
        PUBLISH="--publish 80:8096"
        LINKS="--link aaf-locate"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-fs aaf-locate && exec bin/fs"
        ;;
    "hello")
        PUBLISH="--publish 8130:8130"
        LINKS="--link aaf-service --link aaf-locate --link aaf-oauth --link aaf-cm"
	CMD_LINE="cd /opt/app/aaf && /bin/bash bin/pod_wait.sh aaf-hello aaf-locate && exec bin/hello"
        ;;
    esac

    echo Starting aaf-$AAF_COMPONENT...
    if [ -n "${DUSER}" ]; then
       THE_USER="--user $DUSER"
    fi


    $DOCKER run  \
        -d \
        ${THE_USER} \
        --name aaf-$AAF_COMPONENT \
        ${LINKS} \
        --env AAF_ENV=${AAF_ENV} \
        --env aaf_locator_container=docker \
        --env aaf_locator_container_ns=${NAMESPACE} \
        --env aaf_locator_fqdn=${HOSTNAME} \
        --env aaf_locator_public_fqdn=${HOSTNAME} \
        --env aaf_deployed_version=${VERSION} \
        --env LATITUDE=${LATITUDE} \
        --env LONGITUDE=${LONGITUDE} \
        --env CASSANDRA_CLUSTER=${CASSANDRA_CLUSTER} \
        --env CASSANDRA_USER=${CASSANDRA_USER} \
        --env CASSANDRA_PASSWORD=${CASSANDRA_PASSWORD} \
        --env CASSANDRA_PORT=${CASSANDRA_PORT} \
        $PUBLISH \
        -v "aaf_config:$CONF_ROOT_DIR" \
        -v "aaf_status:/opt/app/aaf/status" \
        ${PREFIX}${ORG}/${PROJECT}/aaf_core:${VERSION} \
	/bin/bash -c "$CMD_LINE"
done
