#
# Initialize a manual Cert.  This is NOT entered in Certman Records
#
echo "FQI (Fully Qualified Identity): "
read FQI
if [ "$1" = "" -o "$1" = "-local" ]; then 
  echo "Personal Certificate"
  SUBJECT="/CN=$FQI/OU=V1`cat subject.aaf`"
  NAME=$FQI
else 
  echo "Application Certificate"
  SUBJECT="/CN=$1/OU=$FQI`cat subject.aaf`"
  FQDN=$1
  NAME=$FQDN
  shift
fi
echo $SUBJECT

if [ -e $FQI.csr ]; then
  SIGN_IT=true
else 
  if [ "$1" = "-local" ]; then
	echo "IMPORTANT: If for any reason, you kill this process, type 'stty sane'"
	echo "Enter the PassPhrase for the Key for $FQI: "
	`stty -echo`
	read PASSPHRASE
	`stty echo`
 
	# remove any previous Private key
	rm private/$NAME.key
	# Create j regaular rsa encrypted key
	openssl req -new -newkey rsa:2048 -sha256 -keyout private/$NAME.key \
	  -out $NAME.csr -outform PEM -subj "$SUBJECT" \
	  -passout stdin  << EOF
$PASSPHRASE
EOF
	chmod 400 private/$NAME.key 
	SIGN_IT=true
  else 
	echo openssl req -newkey rsa:2048 -sha256 -keyout $NAME.key -out $NAME.csr -outform PEM -subj '"'$SUBJECT'"'
	echo chmod 400 $NAME.key
	echo "# All done, print result"
	echo openssl req -verify -text -noout -in $NAME.csr
  fi
fi

if [ "$SIGN_IT" = "true" ]; then
  # Sign it
  openssl ca -config ../openssl.conf -extensions server_cert -out $NAME.crt \
	-cert certs/ca.crt -keyfile private/ca.key \
	-policy policy_loose \
	-days 360 \
	-infiles $NAME.csr
fi




