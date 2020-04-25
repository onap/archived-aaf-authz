#
. ./l.props
if [ -z "$1" ]; then
  echo "Enter 'user:group' for the directory after creation"
  read CHOWN
else
  CHOWN="$1"	
fi


for D in $INSTALL_DIR $ORG_DIR; do
  if [ -e $D ]; then
    echo "$D already exists"
  else
    mkdir -p $D 
    echo "$D created"
  fi
  echo "Setting Ownership of $D to $CHOWN"
  chown $CHOWN $D
done
