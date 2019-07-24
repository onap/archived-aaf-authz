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
CADI_VERSION=${CADI_VERSION:=2.1.15-SNAPSHOT}

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
    --env aaf_locator_container=docker \
    --link aaf-service --link aaf-locate --link aaf-oauth --link aaf-cm \
    --name aaf-agent-$USER \
    "$PREFIX"onap/aaf/aaf_agent:$VERSION \
    bash -c "bash /opt/app/aaf_config/bin/agent.sh $PARAMS"
}

function sso {
  if [ -n "$2" ]; then
    echo "$1=$2" >> $HOME/.aaf/sso.props
  fi
}

function reset_sso {
    mkdir -p ~/.aaf
    > $HOME/.aaf/sso.props
    sso aaf_locate_url "https://$AAF_FQDN:8095"
    sso cadi_latitude "$LATITUDE"
    sso cadi_longitude "$LONGITUDE"
    sso cadi_loglevel "DEBUG"
    TRUSTSTORE="$(ls truststore*.jks | tail -1)"
    if [ -z "$TRUSTSTORE" ]; then
      echo "Place a truststore*.jar which has YOUR CA in it here"
      exit
    fi
    sso cadi_truststore "${PWD}/${TRUSTSTORE}"
    sso cadi_truststore_password changeit
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
  aafcli) 
    shift
    reset_sso
    if [ -f aaf-cadi-aaf-$VERSION-full.jar ]; then
      java -Dcadi_prop_files="$HOME/.aaf/sso.props" -jar aaf-cadi-aaf-$VERSION-full.jar $@
    else 
      echo "For local use, you need to have 'aaf-cadi-aaf-$VERSION-full.jar' (or newer)"
    fi
    ;;
  local) 
    shift
    CMD="$1"
    if [ -z "$2" ]; then 
	CMD="$CMD $APP_FQI $APP_FQDN"
    else 
      if [ "-" = "$2" ]; then
         CMD="$CMD $APP_FQI"
      else
         CMD="$CMD $2"
      fi
      if [ "-" = "$3" ]; then
         CMD="$CMD $APP_FQDN"
      else
         CMD="$CMD $3"
      fi
    fi
    reset_sso
    sso aaf_id "$DEPLOY_FQI"
    sso aaf_password "$DEPLOY_PASSWORD"
    if [ -f aaf-cadi-aaf-$VERSION-full.jar ]; then
      java -Dcadi_prop_files="$HOME/.aaf/sso.props" -cp aaf-cadi-aaf-$VERSION-full.jar org.onap.aaf.cadi.configure.Agent $CMD 
    else 
      echo "For local use, you need to have 'aaf-cadi-aaf-$VERSION-full.jar' (or newer)"
    fi
    ;;
  *)
    run_it --rm 
    ;;
esac

