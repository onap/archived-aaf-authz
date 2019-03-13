use authz;
alter TABLE cred ADD tag varchar;
alter TABLE future ADD target_key varchar;
alter TABLE future ADD target_date timestamp;
CREATE INDEX future_target_key ON authz.future (target_key);

CREATE TABLE notified (
  user          text,
  target        text,
  key           text,
  last          timestamp,
  PRIMARY KEY (user,target,key)
);

