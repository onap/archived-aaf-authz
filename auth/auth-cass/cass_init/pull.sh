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
# Pull data from Cassandra into ".dat" files, and "gzip" them
#
DIR=/opt/app/aaf/cass_init
cd $DIR
mkdir -p dats
cd dats
TABLES="$(cqlsh -e "use authz; describe tables")"
for T in $TABLES ; do
  cqlsh -e "COPY authz.$T TO '$T.dat' WITH DELIMITER='|';"
done
cd $DIR
tar -cvzf dat.gz dats/*.dat
rm -Rf dats
