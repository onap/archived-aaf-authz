use authz;
alter TABLE cred ADD tag varchar;
alter TABLE future ADD target_key varchar;
alter TABLE future ADD target_date timestamp;
