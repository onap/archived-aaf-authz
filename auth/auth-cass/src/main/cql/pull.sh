mkdir -p dats
cd dats
for T in ns ns_attrib cred user_role perm role config artifact ; do
  cqlsh -e "use authz; COPY $T TO '$T.dat' WITH DELIMITER='|';"
done
tar -cvzf ../dat.gz *.dat
rm *.dat
cd -
rmdir dats

