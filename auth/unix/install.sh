#! /bin/sh

##############################
# STATICALLY Named Properties
# The Batch class to start
BATCH_CLS="${CATCH_CLS:=org.onap.aaf.auth.batch.Batch}"

##############################
# Initial Setup for AAF, on regular UNIX O/Ss (not Docker)
. ./l.props

##############################
# Functions

# SED needs escaped slashes
function escSlash {
  echo "${1//\//\\\/}"
}

function debug {
  if [ -n "$DEBUG" ]; then
    echo "$*"
  fi
}


##############################
# TEST if ORG_DIR and INSTALL_DIR are writable by this script
if [ -z "$ORG_DIR" ]; then echo "Shell variable ORG_DIR must be set"; exit 1; fi
if [ -z "$INSTALL_DIR" ]; then echo "Shell variable INSTALL_DIR must be set"; exit 1; fi

for D in "$ORG_DIR" "$INSTALL_DIR"; do
  if [ -w "$D" ]; then
    debug "$D is writable by $USER"
  else
    echo "$D must be writable by $USER to continue..." 
    echo "You may run 'firstAsRoot.sh <user>:<group>' as root to fix this issue, or fix manually"
    exit 1
  fi
done

# If not set, use HOSTNAME
CASSANDRA_CLUSTERS=${CASSANDRA_CLUSTERS:=$HOSTNAME}
ORIG_NS="org.osaaf.aaf"
ROOT_NS="${ROOT_NS:=$ORIG_NS}"
AAF_ID="${AAF_ID:=aaf@aaf.osaaf.org}"

##############################
# DEFINES
JAVA_AGENT="-Dcadi_prop_files=$ORG_DIR/local/$ROOT_NS.props org.onap.aaf.cadi.configure.Agent"

##############################
# Create directory Structure 
INSTALL_DIR=${INSTALL_DIR:=/opt/app/aaf}
for D in "" "status" "cass_init" "cass_init/dats"; do
  if [ -e "$INSTALL_DIR/$D" ]; then
    debug "$INSTALL_DIR/$D exists"
  else
    mkdir -p "$INSTALL_DIR/$D"
    debug "created $INSTALL_DIR/$D "
  fi
done

##############################
# Check for previous install, backup as necessary
if [[ -e $INSTALL_DIR/AAF_VERSION  && "$VERSION" = "$(cat $INSTALL_DIR/AAF_VERSION)" ]]; then
  echo Current Version
elif [ -e $INSTALL_DIR/lib ]; then
  PREV_VER="$(cat $INSTALL_DIR/AAF_VERSION)"
  echo Backing up $PREV_VER
  if [ -e $INSTALL_DIR/$PREV_VER ]; then
    rm -Rf $INSTALL_DIR/$PREV_VER
  fi
  mkdir $INSTALL_DIR/$PREV_VER
  mv $INSTALL_DIR/bin $INSTALL_DIR/lib $INSTALL_DIR/theme $INSTALL_DIR/$PREV_VER
  echo "Backed up bin,lib and theme to $INSTALL_DIR/$PREV_VER"
fi

##############################
# Copy from Compiled Version
cp -Rf ../aaf_$VERSION/* $INSTALL_DIR
echo $VERSION > $INSTALL_DIR/AAF_VERSION

##############################
# Add Theme links
for D in "$ORG_DIR" "$ORG_DIR/public"; do
  if [ -e "$D/theme" ]; then
    debug "$D/theme exists"
  else
    debug "Soft Linking theme $INSTALL_DIR/theme to $D"
    ln -s "$INSTALL_DIR/theme" "$D"
  fi
done

##############################
# Copy from Cass Samples
debug "Copying Casssandra Samples to $INSTALL_DIR/cass_init"
cp ../auth-cass/cass_init/*.cql $INSTALL_DIR/cass_init
cp $(ls ../auth-cass/cass_init/*.sh | grep -v push.sh | grep -v restore.sh) $INSTALL_DIR/cass_init

##############################
# adjust push.sh and restore.sh
BATCH_JAR=$(find .. -name aaf-auth-batch-$VERSION.jar)
if [ -z "$BATCH_JAR" ]; then
  if [ -z "$INSTALL_DIR/lib/aaf-auth-batch-$VERSION.jar" ]; then
    echo "You need to build the AAF Jars with 'mvn' for $VERSION to continue this configuration"
    exit 1
  fi
else
  debug "Copying $BATCH_JAR to $INSTALL_DIR/lib"
  cp $BATCH_JAR $INSTALL_DIR/lib
fi

DEF_ORG_JAR=$(find .. -name aaf-auth-deforg-$VERSION.jar | head -1)
if [ -z "$DEF_ORG_JAR" ]; then
  echo "You need to build the deforg jar to continue this configuration"
  exit 1
else
  echo "Copying $DEF_ORG_JAR to $INSTALL_DIR/lib"
  cp $DEF_ORG_JAR $INSTALL_DIR/lib
fi

# Note: Docker Cass only needs Batch Jar, but AAF on the disk can have only one lib
# so we copy just the Batch jar
for S in push.sh restore.sh; do
  debug "Writing Cassandra $INSTALL_DIR/cass_init/$S script with replacements"
  sed -e "/CQLSH=.*/s//CQLSH=\"cqlsh -k authz $CASSANDRA_CLUSTERS\"/" \
      -e "/-jar .*full.jar/s//-cp .:$(escSlash $INSTALL_DIR/lib/)* $BATCH_CLS /" \
      ../auth-cass/cass_init/$S > $INSTALL_DIR/cass_init/$S
done

##############################
# adjust authBatch.props
CHANGES="-e /GUI_URL=.*/s//GUI_URL=https:\/\/$HOSTNAME:8095\/gui/"

for TAG in "LATITUDE" "LONGITUDE"; do
   CHANGES="$CHANGES -e /${TAG,,}=.*/s//cadi_${TAG,,}=${!TAG}/"
done

CHANGES="$CHANGES -e /aaf_data_dir=.*/s//aaf_data_dir=$(escSlash $ORG_DIR/data)/"

# Cassandra Properties have dots in them, which cause problems for BASH processing
for TAG in "CASSANDRA_CLUSTERS" "CASSANDRA_CLUSTERS_PORT" "CASSANDRA_CLUSTERS_USER" "CASSANDRA_CLUSTERS_PASSWORD"; do
  VALUE="${!TAG}"
  if [ ! -z "$VALUE" ]; then
     DOTTED="${TAG//_/.}"
     NTAG=${DOTTED,,}
     CHANGES="$CHANGES -e /${NTAG}=.*/s//${NTAG}=${!TAG}/"
  fi
done

echo "Writing Batch Properties with conversions to $INSTALL_DIR/cass_init/authBatch.props"
debug "Conversions: $CHANGES"
sed $CHANGES ../auth-cass/cass_init/authBatch.props > $INSTALL_DIR/cass_init/authBatch.props

##############################
# Setup Organizational Data Directories
for D in $ORG_DIR/data $ORG_DIR/local $ORG_DIR/logs $ORG_DIR/public $ORG_DIR/etc $ORG_DIR/bin; do
  if [ ! -e $D ]; then
    debug "Creating $D"
    mkdir -p $D 
  fi
done

##############################
# Convert generated bin files to correct ORG DIR
for B in $(ls $INSTALL_DIR/bin | grep -v .bat); do
  sed -e "/cadi_prop_files=/s//aaf_log4j_prefix=$ROOT_NS cadi_prop_files=/" \
      -e "/$ORIG_NS/s//$ROOT_NS/g" \
      -e "/$(escSlash /opt/app/osaaf)/s//$(escSlash $ORG_DIR)/g" \
      -e "/^CLASSPATH=.*/s//CLASSPATH=$(escSlash $INSTALL_DIR/lib/)*/" \
      $INSTALL_DIR/bin/$B > $ORG_DIR/bin/$B
  chmod u+x $ORG_DIR/bin/$B
  debug "Converted generated app $B and placed in $INSTALL_DIR/bin"
done

##############################
# Create new Initialized Data from ONAP "sample"
if [ "$1" = "sample" ]; then
  ##############################
  # Copy sample dat files 
  #   (ONAP Samples)
  echo "### Copying all ONAP Sample data"
  cp ../sample/cass_data/*.dat $INSTALL_DIR/cass_init/dats

  # Scrub data, because it is coming from ONAP Test systems,
  # and also, need current dates
  echo "### Scrubbing ONAP Sample data"
  mkdir -p $INSTALL_DIR/cass_init/data
  cp ../sample/data/sample.identities.dat $INSTALL_DIR/cass_init/data
  CURR=$(pwd)
  cd $INSTALL_DIR/cass_init/dats
  bash $CURR/../sample/cass_data/scrub.sh
  cd $CURR
  rm -Rf $INSTALL_DIR/cass_init/data

  ##############################
  # Sample Identities
  # Only create if not exists.  DO NOT OVERWRITE after that
  if [ ! -e $ORG_DIR/data/identities.dat ]; then
    cp ../sample/data/sample.identities.dat $ORG_DIR/data/identities.dat
  fi

  ##############################
  # ONAP Test Certs and p12s
  cp ../sample/cert/AAF_RootCA.cer $ORG_DIR/public
  for F in $(ls ../sample/cert | grep b64); do 
    if [ ! -e "$F" ]; then
      if [[ $F = "trust"* ]]; then
        SUB=public
      else
	SUB=local
      fi
      if [[ $F = "demoONAPsigner"* ]]; then
	FILENAME="$ROOT_NS.signer.p12"
      else
	FILENAME="${F/.b64/}"
      fi
      base64 -d ../sample/cert/$F > $ORG_DIR/$SUB/$FILENAME
    fi
  done

  if [ ! -e "$ORG_DIR/CA" ]; then
    cp -Rf ../../conf/CA $ORG_DIR
  fi

  FILE="$ORG_DIR/local/$ROOT_NS.p12"
  if [ ! -e $FILE ]; then
    echo "Bootstrap Creation of Keystore from Signer"
    cd $ORG_DIR/CA

    # Redo all of this after Dublin
    export cadi_x509_issuers="CN=intermediateCA_1, OU=OSAAF, O=ONAP, C=US:CN=intermediateCA_7, OU=OSAAF, O=ONAP, C=US"
    export signer_subj="/CN=intermediateCA_9/OU=OSAAF/O=ONAP/C=US"
    bash bootstrap.sh $ORG_DIR/local/$ROOT_NS.signer.p12 'something easy'
    cp aaf.bootstrap.p12 $FILE

    cd -
#    if [ -n "$CADI_X509_ISSUERS" ]; then
#      CADI_X509_ISSUERS="$CADI_X509_ISSUERS:"
#    fi
#    BOOT_ISSUER="$(cat aaf.bootstrap.issuer)"
#    CADI_X509_ISSUERS="$CADI_X509_ISSUERS$BOOT_ISSUER"
#
#    I=${BOOT_ISSUER##CN=};I=${I%%,*}
#    CM_CA_PASS="something easy"
#    CM_CA_LOCAL="org.onap.aaf.auth.cm.ca.LocalCA,$LOCAL/$ROOT_NS.signer.p12;aaf_intermediate_9;enc:"
#    CM_TRUST_CAS="$PUBLIC/AAF_RootCA.cer"
#    echo "Generated ONAP Test AAF certs"
  fi

  ##############################
  # Initial Properties
  debug "Create Initial Properties"
  if [ ! -e $ORG_DIR/local/$ROOT_NS.props ]; then
    for F in $(ls ../sample/local/$ORIG_NS.*); do
      NEWFILE="$ORG_DIR/local/${F/*$ORIG_NS./$ROOT_NS.}"
      sed -e "/$ORIG_NS/s//$ROOT_NS/g" \
	  $F > $NEWFILE
      debug "Created $NEWFILE"
    done
    for D in public etc logs; do
      for F in $(ls ../sample/$D); do
        NEWFILE="$ORG_DIR/$D/${F/*$ORIG_NS./$ROOT_NS.}"
        sed -e "/$(escSlash /opt/app/osaaf)/s//$(escSlash $ORG_DIR)/g" \
	    -e "/$ORIG_NS/s//$ROOT_NS/g" \
	    ../sample/$D/$F > $NEWFILE
	echo "Created $NEWFILE"
      done
    done
      
    ##############################
    # Set Cassandra Variables
    CHANGES=""
    for TAG in "CASSANDRA_CLUSTERS" "CASSANDRA_CLUSTERS_PORT" "CASSANDRA_CLUSTERS_USER" "CASSANDRA_CLUSTERS_PASSWORD"; do
      VALUE="${!TAG}"
      if [ ! -z "$VALUE" ]; then
         DOTTED="${TAG//_/.}"
         NTAG=${DOTTED,,}
         CHANGES="$CHANGES -e /${NTAG}=.*/s//${NTAG}=${!TAG}/"
      fi
    done
    mv $ORG_DIR/local/$ROOT_NS.cassandra.props $ORG_DIR/local/$ROOT_NS.cassandra.props.backup
    sed $CHANGES $ORG_DIR/local/$ROOT_NS.cassandra.props.backup > $ORG_DIR/local/$ROOT_NS.cassandra.props

    ##############################
    # CADI Config Tool

    # Change references to /opt/app/osaaf to ORG_DIR
    sed -e "/$(escSlash /opt/app/osaaf)/s//$(escSlash $ORG_DIR)/g" \
	-e "/$ORIG_NS/s//$ROOT_NS/" \
	-e "/$ORIG_AAF_ID/s//$AAF_ID/" \
        ../sample/local/aaf.props > _temp.props

    java -cp $INSTALL_DIR/lib/\* $JAVA_AGENT config \
        $AAF_ID \
	aaf_root_ns=$ROOT_NS \
        cadi_etc_dir=$ORG_DIR/local \
        cadi_latitude=${LATITUDE} \
        cadi_longitude=${LONGITUDE} \
        aaf_data_dir=$ORG_DIR/data \
	aaf_locate_url=${AAF_LOCATE_URL:=https://$HOSTNAME:8095} \
        cadi_prop_files=_temp.props:../sample/local/initialConfig.props
    rm _temp.props
  fi

fi

