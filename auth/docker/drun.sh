#!/bin/bash
# Validate for realtime Cassandra info

# check if outside Cluster defined... otherwise, set CASS_HOST for using expected Docker based Cass
if [ -z "$(grep -e '^CASS_CLUSTER=.*' d.props)" ]; then
  if [ "$(uname)" = "Darwin" ]; then
    SED="sed -i .bak"
  else
    SED="sed -i"
  fi

  CASSANDRA_IP=$(docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' aaf_cass)
  if [ -z "$(grep -e '^CASS_HOST.*' d.props)" ]; then
    $SED "s/# CASS_HOST=.*/CASS_HOST=cass.aaf.osaaf.org:$CASSANDRA_IP/"  d.props
  else 
    $SED "s/CASS_HOST=.*/CASS_HOST=cass.aaf.osaaf.org:$CASSANDRA_IP/"  d.props
  fi
  echo "Updated d.props for CASSANDRA Name/IP"
  grep -e '^CASS_HOST.*' d.props
fi

# Pull in Variables from d.props
. ./d.props

DOCKER=${DOCKER:=docker}

bash aaf.sh onap

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    case "$AAF_COMPONENT" in
    "service")
        PORTMAP="8100:8100"
        LINKS="--link aaf_cass:cassandra "
        ;;
    "locate")
        PORTMAP="8095:8095"
        LINKS="--link aaf_cass:cassandra "
        ;;
    "oauth")
        PORTMAP="8140:8140"
        LINKS="--link aaf_cass:cassandra "
        ;;
    "gui")
        PORTMAP="8200:8200"
        ;;
    "cm")
        PORTMAP="8150:8150"
        LINKS="--link aaf_cass:cassandra "
        ;;
    "hello")
        PORTMAP="8130:8130"
        ;;
    "fs")
        PORTMAP="80:8096"
        ;;
    esac

    echo Starting aaf_$AAF_COMPONENT...

    if [ -n "$AAF_REGISTER_AS" ] && [ "$HOSTNAME" != "$AAF_REGISTER_AS" ]; then
       AH_ROOT="$HOSTNAME $AAF_REGISTER_AS"
    else
       AH_ROOT="$HOSTNAME"
    fi

    for A in aaf.osaaf.org $AH_ROOT; do 
       ADD_HOST="$ADD_HOST --add-host=$A:$HOST_IP"
    done

    if [[ "$CASS_HOST" =~ ":" ]]; then
       echo "Adding Cassandra Host $CASS_HOST"
       ADD_HOST="$ADD_HOST --add-host=$CASS_HOST"
    fi
    $DOCKER run \
        -d \
        --name aaf_$AAF_COMPONENT \
        --hostname="${AAF_COMPONENT}.aaf.osaaf.org" \
	$ADD_HOST \
        ${LINKS} \
        --env AAF_ENV=${AAF_ENV} \
        --env AAF_REGISTER_AS=${AAF_REGISTER_AS} \
        --env LATITUDE=${LATITUDE} \
        --env LONGITUDE=${LONGITUDE} \
        --env CASS_HOST=${CASS_HOST} \
        --env CASSANDRA_CLUSTER=${CASSANDRA_CLUSTER} \
        --env CASSANDRA_USER=${CASSANDRA_USER} \
        --env CASSANDRA_PASSWORD=${CASSANDRA_PASSWORD} \
        --env CASSANDRA_PORT=${CASSANDRA_PORT} \
        --publish $PORTMAP \
        -v "aaf_config:$CONF_ROOT_DIR" \
        ${PREFIX}${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION}
done
