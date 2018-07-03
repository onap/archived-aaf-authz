cp ../certs/ca.crt signer.trustchain
cat certs/ca.crt >> signer.trustchain
openssl pkcs12 -export -name aaf_intermediate_1 -in signer.trustchain -inkey private/ca.key -out aaf_intermediate_1.p12
