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
# Initialize a manual Cert.  This is NOT entered in Certman Records
# $1 - CN (Common Name)
# $2 - FQI (Fully Qualified Identity)
# $3-$n - SANs (Service Alias Names)
#

if [ "$2" = "" ]; then
  echo "FQI (Fully Qualified Identity): "
  read FQI
fi

if [ "$1" = "" -o "$1" = "-local" ]; then
  echo "Personal Certificate"
  SUBJECT="/CN=$FQI/OU=V1`cat subject.aaf`"
  NAME=$FQI
else
  echo "Application Certificate"
  SUBJECT="/CN=$1/OU=$FQI`cat subject.aaf`"
  NAME=$1

  if [ "$3" = "" ]; then
    echo "Enter any SANS, delimited by spaces: "
    read SANS
  else
    SANS=""
    while [ ! "$3" = "" ]; do
    SANS=${SANS}" "$3
    shift
    done
  fi
fi

# Do SANs
if [ "$SANS" = "" ]; then
   echo no SANS
    if [ -e $NAME.san ]; then
      rm $NAME.san
    fi
  else
   echo some SANS: $SANS
    cp ../san.conf $NAME.san
    NUM=1
    for D in $SANS; do
        echo "DNS.$NUM = $D" >> $NAME.san
	      NUM=$((NUM+1))
    done
fi

echo $SUBJECT

if [ ! -e $NAME.csr ]; then
  if [ "$1" = "-local" ]; then
	echo "IMPORTANT: If for any reason, you kill this process, type 'stty sane'"
	echo "Enter the PassPhrase for the Key for $FQI: "
	`stty -echo`
	read PASSPHRASE
	`stty echo`

	# remove any previous Private key
	rm private/$NAME.key
	# Create regular rsa encrypted key
	openssl req -new -newkey rsa:2048 -sha256 -keyout private/$NAME.key \
	  -out $NAME.csr -outform PEM -subj "$SUBJECT" \
	  -passout stdin  << EOF
$PASSPHRASE
EOF
	chmod 400 private/$NAME.key
  else
	openssl req -newkey rsa:2048 -sha256 -keyout private/$NAME.key -out $NAME.csr -outform PEM -subj "$SUBJECT"
	chmod 400 $NAME.key
	echo "# All done, print result"
	openssl req -verify -text -noout -in $NAME.csr
  fi
fi

  # Sign it
  if [ -e $NAME.san ]; then
    openssl ca -config ../openssl.conf -extensions server_cert -out certs/$NAME.crt \
	-cert certs/ca.crt -keyfile private/ca.key \
	-policy policy_loose \
	-days 360 \
	-extfile $NAME.san \
	-infiles $NAME.csr
  else
    openssl ca -config ../openssl.conf -extensions server_cert -out certs/$NAME.crt \
	-cert certs/ca.crt -keyfile private/ca.key \
	-policy policy_loose \
	-days 360 \
	-infiles $NAME.csr
  fi
