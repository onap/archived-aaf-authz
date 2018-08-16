#!/bin/bash
# Pull in Variables from d.props
. ./d.props

# Only need Cassandra Link Info when initializing the container.
if [ ! -e ./cass.props ]; then
    cp cass.props.init cass.props
    echo "Edit appropriate Cassandra Link Info into cass.props"
    exit
fi

. ./cass.props

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    case "$AAF_COMPONENT" in
    "service")
        PORTMAP="8100:8100"
        LINKS="--link aaf_cass:cassandra --add-host=$CASS_HOST"
        ;;
    "locate")
        PORTMAP="8095:8095"
        LINKS="--link aaf_cass:cassandra --add-host=$CASS_HOST"
        ;;
    "oauth")
        PORTMAP="8140:8140"
        LINKS="--link aaf_cass:cassandra --add-host=$CASS_HOST"
        ;;
    "gui")
        PORTMAP="8200:8200"
        ;;
    "cm")
        PORTMAP="8150:8150"
        LINKS="--link aaf_cass:cassandra --add-host=$CASS_HOST"
        ;;
    "hello")
        PORTMAP="8130:8130"
        ;;
    "fs")
        PORTMAP="80:8096"
        ;;
    esac

    echo Starting aaf_$AAF_COMPONENT...

    docker run \
        -d \
        --name aaf_$AAF_COMPONENT \
        --hostname="${AAF_COMPONENT}.aaf.osaaf.org" \
        --add-host="$HOSTNAME:$HOST_IP" \
        --add-host="aaf.osaaf.org:$HOST_IP" \
        ${LINKS} \
        --publish $PORTMAP \
        --mount 'type=volume,src=aaf_config,dst='$CONF_ROOT_DIR',volume-driver=local' \
        ${PREFIX}${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION}
done
