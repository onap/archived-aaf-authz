#
# Create a p12 file from local certs
#
echo "FQI (Fully Qualified Identity): "
read FQI

if [ "$1" = "" ]; then
  MACH=$FQI  
else 
  MACH=$1
fi

# Add Cert AND Intermediate CAs (Clients will have Root CAs (or not))
  cat $MACH.crt  > $MACH.chain
  for CA in `ls intermediateCAs`; do
        cat "intermediateCAs/$CA" >> $MACH.chain
  done

  # Make a pkcs12 keystore, a jks keystore and a pem keystore
  rm -f $MACH.p12
  # Note: Openssl will pickup and load all Certs in the Chain file
  openssl pkcs12 -name $FQI -export -in $MACH.chain -inkey private/$MACH.key -out $MACH.p12 

