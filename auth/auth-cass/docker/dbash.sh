#!/bin/bash

# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
DOCKER=${DOCKER:-docker}

$DOCKER exec -it aaf_cass bash

