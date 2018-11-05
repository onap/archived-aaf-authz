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
# NOTE: This README is "bash" capable.  bash README.txt
#
# create simple but reasonable directory structure
mkdir -p private certs newcerts 
chmod 700 private
chmod 755 certs newcerts
touch index.txt
echo "unique_subject = no" > index.txt.attr

if [ ! -e serial ]; then
   echo $(date +%s) > serial
fi

if [  "$1" == "" ]; then
  CN=$1
else
  CN=RootCA
fi

echo "IMPORTANT: If for any reason, you kill this process, type 'stty sane'"
echo "Enter the PassPhrase for your Key: "
`stty -echo`
read PASSPHRASE
`stty echo`

if [ ! -e /private/ca.ekey ]; then
  # Create a regaular rsa encrypted key
  openssl genrsa -aes256 -out private/ca.ekey -passout stdin 4096 << EOF
$PASSPHRASE
EOF
fi

if [ ! -e /private/ca.key ]; then
  # Move to a Java/Filesystem readable key. Note that this one is NOT Encrypted.
  openssl pkcs8 -in private/ca.ekey -topk8 -nocrypt -out private/ca.key -passin stdin << EOF
$PASSPHRASE
EOF
fi
chmod 400 private/ca.key private/ca.ekey


if [ -e subject.aaf ]; then
  SUBJECT="-subj /CN=$CN`cat subject.aaf`"
else
  SUBJECT=""
fi

# Generate a CA Certificate
openssl req -config openssl.conf \
      -key private/ca.key \
      -new -x509 -days 7300 -sha256 -extensions v3_ca \
      $SUBJECT \
      -out certs/ca.crt 

if [ -e certs/ca.crt ]; then
  # All done, print result
  openssl x509 -text -noout -in certs/ca.crt
fi
