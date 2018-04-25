#!/bin/bash 
# Pull in Props
. ./d.props

if [ "$1" == "" ]; then
  AAF_COMPONENTS=`ls -r ../aaf_${VERSION}/bin | grep -v '\.'`
else
  AAF_COMPONENTS=$1
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
  docker start aaf_$AAF_COMPONENT
done
