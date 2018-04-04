#
# Import the keys and certs to pkcs11 based softhsm  
#

if [ "$#" -ne 3 ]; then
  echo "Usage: p11.sh <user pin> <so pin> <id>"
  exit 1
fi

LIB_PATH=/usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so

mkdir -p p11key p11crt cacerts
# Conver the keys and certs to DER format
# key to der
openssl rsa -in private/ca.key -outform DER -out p11key/cakey.der
# cert to der 
cp certs/ca.crt cacerts
DLIST=`ls -d intermediate_*`
for DIR in $DLIST; do
  cp $DIR/certs/ca.crt cacerts/$DIR.crt
done
for CA in `ls cacerts`; do
  openssl x509 -in cacerts/$CA -outform DER -out p11crt/$CA
done

# create token directory
mkdir /var/lib/softhsm/tokens
# create slot 
softhsm2-util --init-token --slot 0 --label "ca token" --pin $1 --so-pin $2
# import key into softhsm
pkcs11-tool --module $LIB_PATH -l --pin $1 --write-object p11key/cakey.der --type privkey --id $3
# import certs into softhsm
for CRT in `ls cacerts`; do
  pkcs11-tool --module $LIB_PATH -l --pin $1 --write-object p11crt/$CRT --type cert --id $3
done

rm -r p11key
rm -r p11crt
rm -r cacerts
