USE authz;
// Create Root pass
INSERT INTO cred (id,ns,type,cred,expires)
  VALUES ('initial@osaaf.org','org.osaaf',1,0xdd82c1882969461de74b46427961ea2c,'2099-12-31') using TTL 14400;

INSERT INTO user_role(user,role,expires,ns,rname)
  VALUES ('initial@osaaf.org','org.admin','2099-12-31','org','admin') using TTL 14400;

INSERT INTO user_role(user,role,expires,ns,rname)
  VALUES ('initial@osaaf.org','org.osaaf.aaf.admin','2099-12-31','org.osaaf.aaf','admin') using TTL 14400;


