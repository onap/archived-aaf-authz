#/bin/bash 

cd dats
export DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -d "+6 months")

TEMP=$(mktemp)

mv user_role.dat $TEMP
cat $TEMP | awk -F '|' '{print $1"|"$2"|"ENVIRON["DATE"]"|"$4"|"$5}' > user_role.dat

mv cred.dat $TEMP
cat $TEMP | awk -F '|' '{print $1"|"$2"|"ENVIRON["DATE"]"|"$4"|"$5"|"$6"|"$7"|"$8}' > cred.dat

rm $TEMP

cd - > /dev/null

