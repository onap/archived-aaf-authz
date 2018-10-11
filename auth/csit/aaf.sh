#!/bin/bash
. ./d.props
if [ -e ./cass.props ]; then
  . ./cass.props
fi

DOCKER=${DOCKER:=docker}
function run_it() {
  $DOCKER run $@ \
    -v "aaf_config:$CONF_ROOT_DIR" \
    --add-host="$HOSTNAME:$HOST_IP" \
    --add-host="aaf.osaaf.org:$HOST_IP" \
    --env HOSTNAME=${HOSTNAME} \
    --env AAF_ENV=${AAF_ENV} \
    --env AAF_REGISTER_AS=${AAF_REGISTER_AS} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --env CASS_HOST=${CASS_HOST} \
    --env CASSANDRA_CLUSTER=${CASSANDRA_CLUSTER} \
    --env CASSANDRA_USER=${CASSANDRA_USER} \
    --env CASSANDRA_PASSWORD=${CASSANDRA_PASSWORD} \
    --env CASSANDRA_PORT=${CASSANDRA_PORT} \
    --name aaf_config_$USER \
    $PREFIX${ORG}/${PROJECT}/aaf_config:${VERSION} \
    /bin/bash $PARAMS
}

function set_prop() {
  $DOCKER exec -d aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2" "$3"
}

function encrypt_it() {
  $DOCKER exec -d aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP encrypt "$1" "$2"
}

function set_it() {
  $DOCKER exec -d aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2"
}

PARAMS="$@"
if [ "$PARAMS" != "" ]; then
  run_it -it --rm 
fi

