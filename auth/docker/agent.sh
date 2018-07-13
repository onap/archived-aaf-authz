#!/bin/bash
. ./d.props

docker run \
    -it \
    --rm \
    --mount 'type=volume,src=aaf_config,dst='$CONF_ROOT_DIR',volume-driver=local' \
    --add-host="$HOSTNAME:$HOST_IP" \
    --add-host="aaf.osaaf.org:$HOST_IP" \
    --name aaf_agent_$USER \
    ${ORG}/${PROJECT}/aaf_config:${VERSION} \
    /bin/bash "$@"
