
echo "FYI, by convention, truststore passwords are 'changeit', but you may add something more sophisticated"
# PCKS12 does not appear to be able to mark CAs as Trusted
# openssl pkcs12 -export -name AAF_Root_CA -in certs/ca.crt -nokeys -out truststore.p12
keytool -importcert -file certs/ca.crt -trustcacerts -alias AAF_ROOT_CA -keystore truststore.jks
