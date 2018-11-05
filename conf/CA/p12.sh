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
# Create a p12 file from local certs
#

if [ "$1" = "" ]; then
  echo "Enter Keystore Name: "
  read MACH
else
  MACH=$1
fi

  # Add Cert AND Intermediate CAs (Clients will have Root CAs (or not))
  cat certs/$MACH.crt  > $MACH.chain
  # Add THIS Intermediate CA into chain
  cat certs/ca.crt >> $MACH.chain

  # Make a pkcs12 keystore, a jks keystore and a pem keystore
  rm -f $MACH.p12
  # Note: Openssl will pickup and load all Certs in the Chain file
  openssl pkcs12 -name $MACH -export -in $MACH.chain -inkey private/$MACH.key -out $MACH.p12
