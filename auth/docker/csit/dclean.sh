#!/bin/bash
# Pull in Variables from d.props
. ./d.props
DOCKER=${DOCKER:=docker}

if [ "$1" == "" ]; then
    AAF_COMPONENTS="$(cat components) config core agent"
else
    AAF_COMPONENTS="$@"
fi

echo "Y" | $DOCKER container prune
for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    $DOCKER image rm $ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
    if [ ! "$PREFIX" = "" ]; then
      $DOCKER image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
      $DOCKER image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:latest
    fi
done
echo "Y" | $DOCKER image prune
