#!/bin/bash
. ./d.props
docker run \
  -it \
  --mount 'type=volume,src=aaf_config,dst=/opt/app/osaaf,volume-driver=local' \
  --name aaf_agent_$USER \
  ${ORG}/${PROJECT}/aaf_config:${VERSION} \
  /bin/bash $*
docker container rm aaf_agent_$USER > /dev/null
