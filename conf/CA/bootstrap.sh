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
  echo '01' > ./serial
fi

NAME=aaf.bootstrap
FQDN=$(hostname -f)
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
NUM=1
for D in $FQDN aaf.osaaf.org service.aaf.osaaf.org locate.aaf.osaaf.org oauth.aaf.osaaf.org gui.aaf.osaaf.org cm.aaf.osaaf.org hello.aaf.osaaf.org; do
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
	-days 90 \
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

# Note: Openssl will pickup and load all Certs in the Chain file
openssl pkcs12 -name $FQI -export -in $BOOTSTRAP_CHAIN -inkey $BOOTSTRAP_KEY -out $BOOTSTRAP_P12 -passin stdin -passout stdin << EOF
$PASSPHRASE
$PASSPHRASE
$PASSPHRASE
EOF

# Cleanup
rm -f $BOOTSTRAP_SAN $BOOTSTRAP_KEY $BOOTSTRAP_CSR $BOOTSTRAP_CRT $BOOTSTRAP_CHAIN $SIGNER_KEY $SIGNER_CRT 
