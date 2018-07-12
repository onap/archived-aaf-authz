use authz;
CREATE TABLE config (
  name          varchar,
  tag           varchar,
  value         varchar,
  PRIMARY KEY (name,tag)
);
