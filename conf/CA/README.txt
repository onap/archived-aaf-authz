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
##
# NOTE: This README is "bash" capable.  bash README.txt
#
# create simple but reasonable directory structure
mkdir -p private certs newcerts
chmod 700 private
chmod 755 certs newcerts
touch index.txt
echo '01' > serial

echo "IMPORTANT: If for any reason, you kill this process, type 'stty sane'"
echo "Enter the PassPhrase for your Key: "
`stty -echo`
#read PASSPHRASE
PASSPHRASE=HunkyDoryDickoryDock
`stty echo`

# Create a regaular rsa encrypted key
openssl genrsa -aes256 -out private/ca.ekey -passout stdin 4096 << EOF
$PASSPHRASE
EOF

# Move to a Java readable time, not this one is NOT Encrypted.
openssl pkcs8 -in private/ca.ekey -topk8 -nocrypt -out private/ca.key -passin stdin << EOF
$PASSPHRASE
EOF
chmod 400 private/ca.key private/ca.ekey

# Generate a CA Certificate
openssl req -config openssl.conf \
      -key private/ca.key \
      -new -x509 -days 7300 -sha256 -extensions v3_ca \
      -out certs/ca.crt << EOF
$PASSPHRASE
EOF

# All done, print result
openssl x509 -text -noout -in certs/ca.crt
