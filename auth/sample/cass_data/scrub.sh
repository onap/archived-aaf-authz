> user_role.dat
for ID in $(grep -v "#" ../data/sample.identities.dat | awk -F\| '{print $1}' | grep -v "^$"); do
  grep "$ID@" dats/user_role.dat >> user_role.dat
done

for D in ns ns_attrib perm role config; do 
  cp dats/$D.dat .
done

echo "Roles in TEST data not in sample.identities.dat (../data)"
UR="$(mktemp)"
DUR="$(mktemp)"

cat user_role.dat | awk -F\| '{print $1}' | sort -u > $UR
cat dats/user_role.dat | awk -F\| '{print $1}' | sort -u > $DUR

echo "Removed IDs from user_roles"
diff $UR $DUR | grep "^>" | sort -u 

rm "$UR" "$DUR"
