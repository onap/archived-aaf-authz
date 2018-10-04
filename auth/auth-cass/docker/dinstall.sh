#!/bin/bash 
#
# This is only called from HEAT, as it needs a single check and wait for Cassandra to be ready
#
. drun.sh

echo "Waiting for Cass to be initialized"
for I in 1 2 3 4 5 6 7 8 9 10 11 12 13 14; do
  docker exec -it aaf_cass bash aaf_cmd wait 2> /dev/null
  if [ "$?" -ne "0" ]; then
    echo "Container not ready... Sleep 10"
    sleep 10
  else
    echo "aaf_cass is ready"
    break
  fi 
done


