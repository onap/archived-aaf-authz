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
#
# Import the keys and certs to pkcs11 based softhsm  
#

if [ "$#" -ne 3 ]; then
  echo "Usage: p11.sh <user pin> <so pin> <id>"
  exit 1
fi

LIB_PATH=/usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so

mkdir -p p11key p11crt cacerts
# Conver the keys and certs to DER format
# key to der
openssl rsa -in private/ca.key -outform DER -out p11key/cakey.der
# cert to der 
cp certs/ca.crt cacerts
DLIST=`ls -d intermediate_*`
for DIR in $DLIST; do
  cp $DIR/certs/ca.crt cacerts/$DIR.crt
done
for CA in `ls cacerts`; do
  openssl x509 -in cacerts/$CA -outform DER -out p11crt/$CA
done

# create token directory
mkdir /var/lib/softhsm/tokens
# create slot 
softhsm2-util --init-token --slot 0 --label "ca token" --pin $1 --so-pin $2
# import key into softhsm
pkcs11-tool --module $LIB_PATH -l --pin $1 --write-object p11key/cakey.der --type privkey --id $3
# import certs into softhsm
for CRT in `ls cacerts`; do
  pkcs11-tool --module $LIB_PATH -l --pin $1 --write-object p11crt/$CRT --type cert --id $3
done

rm -r p11key
rm -r p11crt
rm -r cacerts
