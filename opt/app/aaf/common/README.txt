# Initial instructions for Common Directory

1) Generate a Keyfile
	a) From the "Cadi" Lib directory
		java -jar <CADI DIRECTORY>/lib/cadi_core*.jar keygen com.osaaf.keyfile
2) "cp" com.osaaf.common.props.sample to a locally named file
	a) It is best to replace relative paths with canonical paths
	a) Add your Cassandra Connection info.
	b) For your Password, do (from "Cadi Lib" again):
		java -jar <CADI DIRECTORY>/lib/cadi_core*.jar digest com.osaaf.keyfile
		Prepend "enc:" to the encrypted password
3) "ln -s" the locally named file to com.osaaf.common.props
4) "cp" com.osaaf.props.sample to com.osaaf.props
	Note: This file will be replaced by Certificate Manager if used
	a) Update with appropriate "Certificate Manger" URL, if used
