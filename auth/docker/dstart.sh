#!/bin/bash
# Pull in Props
. ./d.props

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    docker start aaf_$AAF_COMPONENT
done
