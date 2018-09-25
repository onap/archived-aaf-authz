#!/bin/bash
CQLSH=/Volumes/Data/apache-cassandra-2.1.14/bin/cqlsh
DIR=.
for T in ns perm role user_role cred config; do
  $CQLSH -e  "COPY authz.$T TO '$DIR/$T.dat' WITH DELIMITER='|'"
done
