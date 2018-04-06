echo "FYI, by convention, truststore passwords are 'changeit', but you may add something more sophisticated"
openssl pkcs12 -export -name AAF_Root_CA -in certs/ca.crt -inkey private/ca.key -out truststore.p12
