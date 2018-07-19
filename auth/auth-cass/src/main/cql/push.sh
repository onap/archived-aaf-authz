tar -xvf dat.gz
for T in x509 ns_attrib config cred user_role perm role artifact ns; do
  cqlsh -e "use authz; COPY $T FROM '$T.dat' WITH DELIMITER='|';"
done

