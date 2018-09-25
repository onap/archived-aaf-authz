#!/bin/bash
#
# Pull data from Cassandra into ".dat" files, and "gzip" them
#
DIR=/opt/app/aaf/cass_init
cd $DIR
mkdir -p dats
cd dats
TABLES="$(cqlsh -e "use authz; describe tables")"
for T in $TABLES ; do
  cqlsh -e "use authz; COPY $T TO '$T.dat' WITH DELIMITER='|';"
done
cd $DIR
tar -cvzf dat.gz dats/*.dat
rm -Rf dats

