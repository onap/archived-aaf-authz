#!/bin/bash
cd /opt/app/cass_init
if [ -e dat.gz ]; then
  tar -xvf dat.gz
else 
  echo "No data files"
fi
