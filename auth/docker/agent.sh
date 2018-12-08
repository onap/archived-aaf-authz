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

if [ -z "$ADMIN" ]; then
  echo -n "Is the target [K]ubernetes or [D]ocker (K):"
  read R
  case $R in 
    d|D) ADMIN=docker 
         echo "ADMIN=docker" >> aaf.props
         ;;
    *) ADMIN=kubectl
         echo "ADMIN=kubectl" >> aaf.props
	;;
  esac
fi
   
if [ "$ADMIN" = "docker" ]; then
   PROPS="VERSION DOCKER_REPOSITORY AAF_FQDN AAF_FQDN_IP DEPLOY_FQI APP_FQDN APP_FQI VOLUME DRIVER LATITUDE LONGITUDE"
   DEF_AAF_FQDN=aaf-onap-test.osaaf.org
else
   PROPS="VERSION DOCKER_REPOSITORY NAMESPACE DEPLOY_FQI DEPLOY_PASSWORD AAF_FQDN APP_FQDN APP_FQI VOLUME PVC DRIVER LATITUDE LONGITUDE"
   DEF_AAF_FQDN=aaf-locate
fi

for V in $PROPS; do
   if [ "$(grep $V ./aaf.props)" = "" ]; then
      unset DEF
      case $V in
	 DOCKER_REPOSITORY) 
	             PROMPT="Docker Repo"; DEF="nexus3.onap.org:10003" ;;
         AAF_FQDN)   PROMPT="AAF's FQDN" 
                     if [ -z "$NAMESPACE" ]; then
		       DEF=$DEF_AAF_FQDN
		     else
                       DEF=$DEF_AAF_FQDN.$NAMESPACE
  		     fi
                     ;;			
         DEPLOY_FQI) PROMPT="Deployer's FQI"; DEF="deployer@people.osaaf.org" ;;
         AAF_FQDN_IP)
		     # Need AAF_FQDN's IP, because not might not be available in mini-container
		     PROMPT="AAF FQDN IP"
  		     DEF=$(host $AAF_FQDN | grep "has address" | tail -1 | cut -f 4 -d ' ')
                     ;;
         APP_FQI)    PROMPT="App's FQI";; 
         APP_FQDN)   PROMPT="App's Root FQDN";; 
         VOLUME)     PROMPT="App's AAF Configuration Volume";DEF=${APP_FQDN/.*/}-config;;
         DRIVER)     PROMPT=$V;DEF=local;;
	 VERSION)    PROMPT="CADI Version";DEF=2.1.9-SNAPSHOT;;
	 NAMESPACE)  PROMPT="Kubernetes Namespace";DEF=onap;;
	 PVC)        PROMPT="Persistent Volume Claim";DEF=$VOLUME-pvc;;
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
            declare $V="$VAR"
         fi
      fi
      echo "$V=$VAR" >> ./aaf.props
   fi
done
. ./aaf.props

if [ -n "$DOCKER_REPOSITORY" ]; then
  PREFIX="$DOCKER_REPOSITORY/"
else
  PREFIX=""
fi 

if [[ "$ADMIN" =~ docker ]]; then
  # Make sure Container Volume exists
  if [ "$($ADMIN volume ls | grep ${VOLUME})" = "" ]; then
    echo -n "Creating Volume: $VOLUME" 
    $ADMIN volume create -d ${DRIVER} ${VOLUME}
  fi

  $ADMIN run \
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
else
  NAMESPACE=${NAMESPACE:=onap}
  YAML=${VOLUME}.yaml
  # Make sure Container Volume exists
  if [ -z "$($ADMIN -n $NAMESPACE get pv | grep ${VOLUME})" ]; then
    if [ ! -r $YAML ]; then
      SIZE=30M
      echo "---" >> $YAML
      echo "kind: PersistentVolume" >> $YAML
      echo "apiVersion: v1" >> $YAML
      echo "metadata:" >> $YAML
      echo "  name: $VOLUME-pv" >> $YAML
      echo "  namespace: $NAMESPACE" >> $YAML
      echo "  labels:" >> $YAML
      echo "    app: $VOLUME" >> $YAML
      echo "    type: local" >> $YAML
      echo "spec:" >> $YAML
      echo "  capacity:" >> $YAML
      echo "    storage: $SIZE" >> $YAML
      echo "  accessModes:" >> $YAML
      echo "    - ReadWriteOnce" >> $YAML
      echo "  hostPath:" >> $YAML
      echo "    path: \"/data/$VOLUME\"" >> $YAML
      echo "  storageClassName: \"manual\"" >> $YAML
      echo "---" >> $YAML
      echo "kind: PersistentVolumeClaim" >> $YAML
      echo "apiVersion: v1" >> $YAML
      echo "metadata:" >> $YAML
      echo "  name: $VOLUME-pvc" >> $YAML
      echo "  namespace: $NAMESPACE" >> $YAML
      echo "  labels:" >> $YAML
      echo "    app: $VOLUME" >> $YAML
      echo "    type: local" >> $YAML
      echo "spec:" >> $YAML
      echo "  selector:" >> $YAML
      echo "    matchLabels:" >> $YAML
      echo "      app: $VOLUME" >> $YAML
      echo "  accessModes:" >> $YAML
      echo "    - ReadWriteOnce" >> $YAML
      echo "  resources:" >> $YAML
      echo "    requests:" >> $YAML
      echo "      storage: $SIZE" >> $YAML
      echo "  storageClassName: "manual"" >> $YAML
    fi
    $ADMIN -n $NAMESPACE create -f $YAML
  fi
  POD=aaf-agent-$USER
  $ADMIN run -n $NAMESPACE $POD \
    -i --rm  \
    --restart=Never \
    --image="$PREFIX"onap/aaf/aaf_agent:$VERSION \
    --overrides='
{
  "apiVersion": "v1",
  "kind": "Pod",
  "metadata": {
    "name": "'$POD'"
  },
  "spec": {
    "volumes": [{
      "name": "'$VOLUME'",
      "persistentVolumeClaim": {
         "claimName": "'$VOLUME'-pvc"
      }
    }],
    "containers": [
      {
        "name": "aaf-agent-'$USER'",
        "imagePullPolicy": "IfNotPresent",
        "image": "'$PREFIX'onap/aaf/aaf_agent:'$VERSION'",
        "args": [
	   "/bin/bash",
           "'$@'"
        ],
        "stdin": true,
        "stdinOnce": true,
        "tty": true,
        "volumeMounts": [
          {
            "mountPath": "/opt/app/osaaf",
            "name": "'$VOLUME'"
          }
        ],
       "env": [
          {
            "name": "AAF_FQDN",
            "value": "'$AAF_FQDN'"
          },{
            "name": "DEPLOY_FQI",
            "value": "'$DEPLOY_FQI'"
          },{
            "name": "DEPLOY_PASSWORD",
            "value": "'$DEPLOY_PASSWORD'"
          },{
            "name": "APP_FQI",
            "value": "'$APP_FQI'"
          },{
            "name": "APP_FQDN",
            "value": "'$APP_FQDN'"
          },{
            "name": "LATITUDE",
            "value": "'$LATITUDE'"
          },{
            "name": "LONGITUDE",
            "value": "'$LONGITUDE'"
          }
        ]
      }
    ]
  }
}'
     

fi
