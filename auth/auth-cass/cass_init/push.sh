#!/bin/bash
#########
#  ============LICENSE_START====================================================
#  org.onap.aaf
#  ===========================================================================
#  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
# Push data from Cassandra ".dat" files into Tables
# These are obtained from "gzipped" files, or pre-placed (i.e. initialization) 
#   in the "dats" directory
#

CQLSH="${CQLSH:=/usr/bin/cqlsh} -k authz"

DIR=/opt/app/aaf/cass_init
cd $DIR
if [ ! -e dats ]; then
  if [ -e dat.gz ]; then
     tar -xvf dat.gz
  else 
     echo "No Data to push for Cassandra"
     exit
  fi
fi
cd dats
for T in $(ls *.dat); do
  if [ -s $T ]; then
    $CQLSH --request-timeout=100 -e "COPY authz.${T/.dat/} FROM '$T' WITH DELIMITER='|';";
  fi
done
cd $DIR
#rm -Rf dats

