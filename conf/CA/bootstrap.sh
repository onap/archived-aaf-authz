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
# Streamlined AAF Bootstrap initial Cert
# Removed Variables so it can be run for AutoDeployments
#
echo "Bootstrap AAF Certificate"
mkdir -p private certs newcerts
chmod 700 private
chmod 755 certs newcerts
touch index.txt
echo "unique_subject = no" > index.txt.attr
if [ ! -e ./serial ]; then
  echo $(date +%s)_$(shuf -i 0-1000000 -n 1)  > ./serial
fi

NAME=aaf.bootstrap
HOSTNAME="${HOSTNAME:=$(hostname -)}"
PUBLIC_FQDN="${aaf_locator_public_fqdn:=$HOSTNAME}"
FQDN="${aaf_locator_fqdn:=$PUBLIC_FQDN}"
FQI=aaf@aaf.osaaf.org
SUBJECT="/CN=$FQDN/OU=$FQI`cat subject.aaf`"
SIGNER_P12=$1
SIGNER_KEY=/tmp/aaf_signer.key
SIGNER_CRT=/tmp/aaf_signer.crt
PASSPHRASE=$2
if [ "PASSPHRASE" = "" ]; then
  PASSPHRASE="something easy"
fi
BOOTSTRAP_SAN=/tmp/$NAME.san
BOOTSTRAP_KEY=/tmp/$NAME.key
BOOTSTRAP_CSR=/tmp/$NAME.csr
BOOTSTRAP_CRT=/tmp/$NAME.crt
BOOTSTRAP_CHAIN=/tmp/$NAME.chain
BOOTSTRAP_P12=$NAME.p12
BOOTSTRAP_ISSUER=$NAME.issuer


# If Signer doesn't exist, create Self-Signed CA
if [ ! -e "$SIGNER_P12"  ]; then
  # Creating Signer CA
  openssl req -config openssl.conf -x509 -sha256 -extensions v3_ca \
    -newkey rsa:4096 -subj /CN="Signer$(cat subject.aaf)" \
    -keyout $SIGNER_KEY -out $SIGNER_CRT -days 365 -passout stdin << EOF
$PASSPHRASE
EOF

  # Move to P12 (Signer)
  openssl pkcs12 -name RootCA -export -in $SIGNER_CRT -inkey $SIGNER_KEY -out $SIGNER_P12 -passin stdin -passout stdin << EOF
$PASSPHRASE
$PASSPHRASE
$PASSPHRASE
EOF

else
  # Get Private key from P12
  openssl pkcs12 -in $SIGNER_P12 -nocerts -nodes -passin stdin -passout stdin -out $SIGNER_KEY << EOF
$PASSPHRASE
$PASSPHRASE
EOF

  # Get Cert from P12
  openssl pkcs12 -in $SIGNER_P12 -clcerts -nokeys -passin stdin -out $SIGNER_CRT << EOF
$PASSPHRASE
EOF

fi

# SANS
cp san.conf $BOOTSTRAP_SAN
SANS=$FQDN
if [ "$FQDN" -ne "$HOSTNAME" ]; then
  SANS="$SANS $HOSTNAME"
fi

for ROOT in $(cat san_root.aaf); do
   SANS="$SANS $ROOT"
   for C in service locate oauth token introspect gui cm hello; do
     SANS="$SANS $C.$ROOT"
   done
done

for C in service locate oauth token introspect gui cm hello; do
   SANS="$SANS aaf-$C"
   SANS="$SANS aaf-$C.onap"
done

NUM=1
for D in $SANS; do
    echo "DNS.$NUM = $D" >> $BOOTSTRAP_SAN
    NUM=$((NUM+1))
done

# Create CSR
openssl req -new -newkey rsa:2048 -nodes -keyout $BOOTSTRAP_KEY \
	-out $BOOTSTRAP_CSR -outform PEM -subj "$SUBJECT" \
	-passout stdin  << EOF
$PASSPHRASE
EOF

echo Sign it
openssl ca -batch -config openssl.conf -extensions server_cert \
	-cert $SIGNER_CRT -keyfile $SIGNER_KEY \
	-policy policy_loose \
	-days 365 \
	-passin stdin \
	-out $BOOTSTRAP_CRT \
	-extfile $BOOTSTRAP_SAN \
	-infiles $BOOTSTRAP_CSR << EOF
$PASSPHRASE
EOF

# Make a P12
# Add THIS Intermediate CA into chain
cat $BOOTSTRAP_CRT
cp $BOOTSTRAP_CRT $BOOTSTRAP_CHAIN
cat $SIGNER_CRT >> $BOOTSTRAP_CHAIN
cat $BOOTSTRAP_CHAIN

# Note: Openssl will pickup and load all Certs in the Chain file
#openssl pkcs12 -name $FQI -export -in $BOOTSTRAP_CRT -inkey $BOOTSTRAP_KEY -CAfile $SIGNER_CRT -out $BOOTSTRAP_P12 -passin stdin -passout stdin << EOF
openssl pkcs12 -name $FQI -export -in $BOOTSTRAP_CHAIN -inkey $BOOTSTRAP_KEY -out $BOOTSTRAP_P12 -passin stdin -passout stdin << EOF
$PASSPHRASE
$PASSPHRASE
$PASSPHRASE
EOF

# Make Issuer name
ISSUER=$(openssl x509 -subject -noout -in $SIGNER_CRT | cut -c 9- | sed -e 's/ = /=/g' -e 's/\//, /g')
for I in $ISSUER; do
  if [ -z "$REVERSE" ]; then
    REVERSE="${I%,}"
  else
    REVERSE="${I%,}, ${REVERSE}"
  fi
done
echo "$REVERSE" > $BOOTSTRAP_ISSUER

# Cleanup
rm -f $BOOTSTRAP_SAN $BOOTSTRAP_KEY $BOOTSTRAP_CSR $BOOTSTRAP_CRT $SIGNER_KEY $SIGNER_CRT $BOOTSTRAP_CHAIN
