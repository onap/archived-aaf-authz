#!/bin/bash
# Pull in Variables from d.props
. ./d.props

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(ls ../aaf_${VERSION}/bin | grep -v '\.')
else
    AAF_COMPONENTS=$1
fi

docker image rm $ORG/$PROJECT/aaf_agent:${VERSION}
docker image rm $ORG/$PROJECT/aaf_config:${VERSION}
docker image rm $ORG/$PROJECT/aaf_core:${VERSION}

echo "Y" | docker container prune
for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    docker image rm $ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
done
echo "Y" | docker image prune
