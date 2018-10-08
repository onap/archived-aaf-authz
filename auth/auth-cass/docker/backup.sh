#!/bin/bash

# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
DOCKER=${DOCKER:-docker}

$DOCKER container exec -it aaf_cass bash -e '/opt/app/aaf/cass_init/pull.sh'
$DOCKER container cp aaf_cass:/opt/app/aaf/cass_init/dat.gz "dat$(date +%Y%m%d).gz"

