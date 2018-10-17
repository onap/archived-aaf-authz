#!/bin/bash


# Fill out "aaf.props" if not filled out already
if [ ! -e aaf.props ]; then
  > ./aaf.props
fi
 
. ./aaf.props

DOCKER=${DOCKER:=docker}
CADI_VERSION=${CADI_VERSION:=2.1.6-SNAPSHOT}

for V in VERSION DOCKER_REPOSITORY AAF_FQDN AAF_FQDN_IP DEPLOY_FQI APP_FQDN APP_FQI VOLUME DRIVER LATITUDE LONGITUDE; do
   if [ "$(grep $V ./aaf.props)" = "" ]; then
      unset DEF
      case $V in
	 DOCKER_REPOSITORY) 
	        PROMPT="Docker Repo"
	        DEF=""
	        ;;
         AAF_FQDN)   PROMPT="AAF's FQDN";;
         DEPLOY_FQI) PROMPT="Deployer's FQI";;
         AAF_FQDN_IP)
		# Need AAF_FQDN's IP, because not might not be available in mini-container
		PROMPT="AAF FQDN IP"
  		DEF=$(host $AAF_FQDN | grep "has address" | tail -1 | cut -f 4 -d ' ')
                ;;
         APP_FQI)    PROMPT="App's FQI";; 
         APP_FQDN)   PROMPT="App's Root FQDN";; 
         VOLUME)     PROMPT="APP's AAF Configuration Volume";;
         DRIVER)     PROMPT=$V;DEF=local;;
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
            echo "agent.sh needs each value queried.  Please start again."
            exit
         else
            VAR=$DEF
         fi
      fi
      echo "$V=$VAR" >> ./aaf.props
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

$DOCKER run \
    -it \
    --rm \
    -v "${VOLUME}:/opt/app/osaaf" \
    --add-host="$AAF_FQDN:$AAF_FQDN_IP" \
    --env AAF_FQDN=${AAF_FQDN} \
    --env DEPLOY_FQI=${DEPLOY_FQI} \
    --env DEPLOY_PASSWORD=${DEPLOY_PASSWORD} \
    --env APP_FQI=${APP_FQI} \
    --env APP_FQDN=${APP_FQDN} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --name aaf_agent_$USER \
    "$PREFIX"onap/aaf/aaf_agent:$VERSION \
    /bin/bash "$@"
