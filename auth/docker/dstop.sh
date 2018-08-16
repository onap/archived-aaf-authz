#!/bin/bash
# Pull in Properties
. ./d.props

if [ "$1" == "" ]; then
    AAF_COMPONENTS=$(cat components)
else
    AAF_COMPONENTS="$@"
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    docker stop aaf_$AAF_COMPONENT
done
