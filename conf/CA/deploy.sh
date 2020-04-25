# SED needs escaped slashes
function escSlash {
  echo "${1//\//\\\/}"
}

NS="$(cat ns.aaf)"
DEPLOY_DIR=${PWD/\/CA/}
read -p "AAF Config Directory: [$DEPLOY_DIR]: " input
DEPLOY_DIR=${input:-$DEPLOY_DIR}

echo "Deploying to $DEPLOY_DIR"

APP_NAME="${DEPLOY_DIR##*/}"
CA_CRT="CA_${APP_NAME^^}.crt"
cp -v certs/ca.crt $DEPLOY_DIR/public/$CA_CRT
sed -i.bak \
	-e "/cm_public_dir=.*/s//cm_public_dir=$(escSlash $DEPLOY_DIR/public)/" \
	-e "/cm_trust_cas=.*/s//cm_trust_cas=${CA_CRT}/" \
	$DEPLOY_DIR/etc/org.osaaf.aaf.cm.props

INT_DIR="intermediate_$(cat intermediate.serial)"

cp -v $INT_DIR/certs/ca.crt $DEPLOY_DIR/public/${APP_NAME^^}_SIGNER.crt
SIGNER=${NS}.signer.p12
cp -v $INT_DIR/aaf_$INT_DIR.p12 $DEPLOY_DIR/local/${SIGNER}

CADI="java -jar /opt/app/aaf/lib/aaf-cadi-core-*.jar"
KEYFILE="$DEPLOY_DIR/local/org.osaaf.aaf.keyfile"
if [ ! -f "$KEYFILE" ]; then
  echo $CADI keygen $KEYFILE
fi

echo "Enter Issuer Key Password "
read -s ISSUER_PASS
ISSUER_PASS=$($CADI digest "$ISSUER_PASS" $KEYFILE)
sed -i.bak \
	-e "/cm_ca.local=.*/s//cm_ca.local=org.onap.aaf.auth.cm.ca.LocalCA,$(escSlash $DEPLOY_DIR/local/$SIGNER);aaf_$INT_DIR;enc:$ISSUER_PASS/" \
        $DEPLOY_DIR/local/org.osaaf.aaf.cm.ca.props
