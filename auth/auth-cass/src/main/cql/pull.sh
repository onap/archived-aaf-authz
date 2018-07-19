for T in x509 ns_attrib config cred user_role perm role artifact ns; do
  cqlsh -e "use authz; COPY $T TO '$T.dat' WITH DELIMITER='|';"
done
tar -cvzf dat.gz *.dat

