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

. ./d.props

DOCKER=${DOCKER:=docker}
function run_it() {
  $DOCKER run $@ \
    -v "aaf_config:$CONF_ROOT_DIR" \
    -v "aaf_status:/opt/app/aaf/status" \
    --env aaf_locator_container=docker \
    --env aaf_locator_fqdn=${HOSTNAME} \
    --env aaf_locate_url=https://aaf-locate:8095 \
    --env AAF_ENV=${AAF_ENV} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --env CASSANDRA_CLUSTER=${CASSANDRA_CLUSTER} \
    --env CASSANDRA_USER=${CASSANDRA_USER} \
    --env CASSANDRA_PASSWORD=${CASSANDRA_PASSWORD} \
    --env CASSANDRA_PORT=${CASSANDRA_PORT} \
    --name aaf_config_$USER \
    $PREFIX${ORG}/${PROJECT}/aaf_config:${VERSION} \
    bash -c "bash /opt/app/aaf_config/bin/agent.sh $PARAMS"
}

PARAMS=$@
case "$1" in 
  bash)
    PARAMS="&& cd /opt/app/osaaf/local && exec bash"
    run_it -it --rm  
    ;;
  taillog)
    run_it -it --rm 
    ;;
  *)
    run_it --rm 
    ;;
esac

