#!/bin/bash
# Pull in Variables from d.props
. ./d.props

# Only need Cassandra Link Info when initializing the container.
if [ ! -e ./cass.props ]; then
    cp cass.props.init cass.props
fi
. ./cass.props

DOCKER=${DOCKER:=docker}

CASS_IS_SET="$(grep '<Cass IP>' cass.props)"
if [ -n "$CASS_IS_SET" ]; then
    CASS_IP="$($DOCKER container inspect aaf_cass | grep \"IPAddress\": -m 1 | cut -d '"' -f 4)"
    if [ -n "$CASS_IP" ]; then
      sed -i -e "s/\(^.*:\).*/\1$CASS_IP/" cass.props
    else
      echo "Set CASSASNDRA IP in cass.props"
      exit
    fi
fi

. ./cass.props

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
        --publish $PORTMAP \
        -v "aaf_config:$CONF_ROOT_DIR" \
        ${PREFIX}${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION}
done
