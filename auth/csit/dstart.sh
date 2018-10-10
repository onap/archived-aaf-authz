#!/bin/bash
# Pull in Props
. ./d.props

DOCKER=${DOCKER:=docker}

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    $DOCKER start aaf_$AAF_COMPONENT
done
