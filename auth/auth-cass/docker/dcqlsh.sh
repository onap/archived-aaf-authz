#!/bin/bash

# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
${DOCKER:=docker} exec -it aaf_cass /usr/bin/cqlsh -k authz

