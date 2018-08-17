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
