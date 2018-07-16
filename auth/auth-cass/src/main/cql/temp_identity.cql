USE authz;
// Create Root pass
INSERT INTO cred (id,ns,type,cred,expires)
  VALUES ('aaf@aaf.osaaf.org','org.osaaf.aaf',1,0xdd82c1882969461de74b46427961ea2c,'2099-12-31') using TTL 14400;

