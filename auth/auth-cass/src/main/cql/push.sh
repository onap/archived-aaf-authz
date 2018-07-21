mkdir -p dats
cd dats
tar -xvf ../dat.gz
for T in $(ls *.dat); do
  cqlsh -e "use authz; COPY ${T%.dat} FROM '$T' WITH DELIMITER='|';"
done
cd -
rm -Rf dats
