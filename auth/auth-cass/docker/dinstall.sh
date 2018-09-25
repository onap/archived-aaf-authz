#!/bin/bash 

. drun.sh

echo $DOCKER
docker exec -it aaf_cass bash aaf_cmd onap

