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


# Fill out "aaf.props" if not filled out already
if [ ! -e aaf.props ]; then
  > ./aaf.props
fi
 
. ./aaf.props

DOCKER=${DOCKER:=docker}
CADI_VERSION=${CADI_VERSION:=2.1.10-SNAPSHOT}

for V in VERSION DOCKER_REPOSITORY HOSTNAME CONTAINER_NS AAF_FQDN AAF_FQDN_IP DEPLOY_FQI APP_FQDN APP_FQI VOLUME DRIVER LATITUDE LONGITUDE; do
   if [ "$(grep $V ./aaf.props)" = "" ]; then
      unset DEF
      case $V in
	 DOCKER_REPOSITORY) 
	        PROMPT="Docker Repo"
	        DEF="nexus3.onap.org:10003"
	        ;;
	 HOSTNAME) 
	        PROMPT="HOSTNAME (blank for Default)"
	        DEF=""
	        ;;
         AAF_FQDN)   PROMPT="AAF's FQDN";;
         DEPLOY_FQI) PROMPT="Deployer's FQI";;
         AAF_FQDN_IP)
		# Need AAF_FQDN's IP, because not might not be available in mini-container
		PROMPT="AAF FQDN IP"
		LOOKUP=$(host "${AAF_FQDN}" | grep "has address")
		if [ -n "${LOOKUP}" ]; then
  		    DEF=$(echo ${LOOKUP} | tail -1 | cut -f 4 -d ' ')
                fi
                ;;
         APP_FQDN)   PROMPT="App's Root FQDN";;
         APP_FQI)    PROMPT="App's FQI"
		     if [[ "${APP_FQDN}" != *"."* ]]; then
	 	       DEF="${APP_FQDN}@${APP_FQDN}.onap.org"
                     fi
		     ;; 
         VOLUME)     PROMPT="APP's AAF Configuration Volume"
		     if [[ "${APP_FQDN}" != *"."* ]]; then
		       DEF="${APP_FQDN}_config"
		     fi
		 ;;
         DRIVER)     PROMPT=$V;DEF=local;;
         CONTAINER_NS)     
                     PROMPT=$V;DEF=onap;;
	 VERSION)    PROMPT="CADI Version";DEF=$CADI_VERSION;;
         LATITUDE|LONGITUDE) PROMPT="$V of Node";;
         *)          PROMPT=$V;;
      esac
      if [ "$DEF" = "" ]; then
           PROMPT="$PROMPT: "
      else 
           PROMPT="$PROMPT ($DEF): "
      fi
      read -p "$PROMPT" VAR 
      if [ "$VAR" = "" ]; then
         if [ "$DEF" = "" ]; then
            if [ "$V" != "HOSTNAME" ]; then
              echo "agent.sh needs each value queried.  Please start again."
              exit
            fi
         else
            VAR=$DEF
         fi
      fi
      echo "$V=$VAR" >> ./aaf.props
      declare "$V"="$VAR"
   fi
done
. ./aaf.props

# Make sure Container Volume exists
if [ "$($DOCKER volume ls | grep ${VOLUME})" = "" ]; then
  echo -n "Creating Volume: " 
  $DOCKER volume create -d ${DRIVER} ${VOLUME}
fi

if [ -n "$DOCKER_REPOSITORY" ]; then
  PREFIX="$DOCKER_REPOSITORY/"
else
  PREFIX=""
fi 

function run_it() {
  if [ -n "${DUSER}" ]; then
    USER_LINE="--user ${DUSER}"
  fi
  $DOCKER run  -it  --rm \
    ${USER_LINE} \
    -v "${VOLUME}:/opt/app/osaaf" \
    --add-host="$AAF_FQDN:$AAF_FQDN_IP" \
    --env AAF_FQDN=${AAF_FQDN} \
    --env DEPLOY_FQI=${DEPLOY_FQI} \
    --env DEPLOY_PASSWORD=${DEPLOY_PASSWORD} \
    --env APP_FQI=${APP_FQI} \
    --env APP_FQDN=${APP_FQDN} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --env aaf_locator_container_ns=${CONTAINER_NS} \
    --name aaf-agent-$USER \
    "$PREFIX"onap/aaf/aaf_agent:$VERSION \
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

