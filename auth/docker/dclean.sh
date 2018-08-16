#!/bin/bash
# Pull in Variables from d.props
. ./d.props

if [ "$1" == "" ]; then
    AAF_COMPONENTS="$(cat components) config core agent"
else
    AAF_COMPONENTS="$@"
fi

docker image rm $ORG/$PROJECT/aaf_agent:${VERSION}
docker image rm $ORG/$PROJECT/aaf_config:${VERSION}
docker image rm $ORG/$PROJECT/aaf_core:${VERSION}

echo "Y" | docker container prune
for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    docker image rm $ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
    if [ "$PREFIX" = "" ]; then
      docker image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
      docker image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:latest
    fi
done
echo "Y" | docker image prune
