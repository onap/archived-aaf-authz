#!/bin/bash 
# Pull in Properties
. ./d.props

if [ "$1" == "" ]; then
  AAF_COMPONENTS=`ls ../aaf_${VERSION}/bin | grep -v '\.'`
else
  AAF_COMPONENTS=$1
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
  docker stop aaf_$AAF_COMPONENT
done
