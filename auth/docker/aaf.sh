#!/bin/bash
. ./d.props

function run_it() {
  docker run $@ \
    --mount 'type=volume,src=aaf_config,dst='$CONF_ROOT_DIR',volume-driver=local' \
    --add-host="$HOSTNAME:$HOST_IP" \
    --add-host="aaf.osaaf.org:$HOST_IP" \
    --env AAF_ENV=${AAF_ENV} \
    --env AAF_REGISTER_AS=${AAF_REGISTER_AS} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --name aaf_config_$USER \
    $PREFIX${ORG}/${PROJECT}/aaf_config:${VERSION} \
    /bin/bash $PARAMS
}

function set_prop() {
docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2" "$3"
}

function encrypt_it() {
  docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP encrypt "$1" "$2"
}

function set_it() {
  docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2"
}

PARAMS="$@"
if [ "$PARAMS" != "" ]; then
  run_it -it --rm 
fi

