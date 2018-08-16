#!/bin/bash
. ./d.props

docker run \
    -it \
    --rm \
    --mount 'type=volume,src=aaf_config,dst='$CONF_ROOT_DIR',volume-driver=local' \
    --add-host="$HOSTNAME:$HOST_IP" \
    --add-host="aaf.osaaf.org:$HOST_IP" \
    --env AAF_ENV=${AAF_ENV} \
    --env AAF_REGISTER_AS=${AAF_REGISTER_AS} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --name aaf_config_$USER \
    $PREFIX${ORG}/${PROJECT}/aaf_config:${VERSION} \
    /bin/bash "$@"
