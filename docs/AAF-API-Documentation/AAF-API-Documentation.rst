AAF 2.0 RESTful interface
=========================

Accessing RESTful
-----------------

-AAF RESTful service is secured by the following:
-The Client must utilize HTTP/S. Non Secure HTTP is not acceptable
-The Client MUST supply an Identity validated by one of the following mechanisms

	-  Valid Global Login Cookie (CSP)
	-  BASIC AUTH protocol using CSO Registered MechID, provisioned in AAF
	-  BASIC AUTH protocol using ATTUID@csp.att.com, Global Login Password
	-  (Available 3rd Qtr 2015) Valid tGuard Login Cookie
	-  (Near Future) Application level Certificate

Responses

Each API Entity listed shows what structure will be accepted by service (ContentType) or responded with by service (Accept). Therefore, use these in making your call. Critical for PUT/POST.

Each API call may respond with JSON or XML. Choose the ContentType/Accept that has +json after the type for JSON or +xml after the Type for XML

XSDs for Versions

AAF can support multiple Versions of the API. Choose the ContentType/Accept that has the appropriate version=?.?

All Errors coming from AAF return AT&T Standard Error Message as a String: `JSON <./example/YXBwbGljYXRpb24vRXJyb3IranNvbg==>`__ `XML <./example/YXBwbGljYXRpb24vRXJyb3IreG1s>`__ (does not apply to errors from Container)

Character Restrictions

-  Character Restrictions must depend on the Enforcement Point used
-  Most AAF usage will be AAF Enforcement Point Characters for Instance and Action are:
    *a-zA-Z0-9,.()\_-=%*
    For Instance, you may declare a multi-dimensional key with : (colon) separator, example:

Ask for a Consultation on how these are typically used, or, if your tool is the only Enforcement Point, if set may be expanded

+--------------------+--------------------+--------------------+---------------------------------------------------+
| Entity             | Method             | Path Info          | Description                                       |
+====================+====================+====================+===================================================+
| PERMISSION         | POST               | /authz/perm        | Create a Permission                               |
|                    |                    |                    | Permission consists of:                           |
|                    |                    |                    | -  type - a Namespace qualified identifier        |
|                    |                    |                    |   specifying what kind of resource is being       |
|                    |                    |                    |     protected                                     |
|                    |                    |                    | -  instance - a key, possibly  multi-dimensional  |
|                    |                    |                    |    that identifies a specific instance of the     |
|                    |                    |                    |    type                                           |
|                    |                    |                    | -  action - what kind of action  is allowed       |
|                    |                    |                    | Note: instance and action can be an \*            |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 201                                               |
|                    |                    |                    | Explicit HTTP Error Codes                         |
|                    |                    |                    | 403, 404, 406, 409                                | 
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | PUT                | /authz/perm        | Set Description  for Permission                   |
|                    |                    |                    | Add Description Data  to Perm                     |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP Error Codes                         |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | DELETE             | /authz/perm        | Delete a Permission                               |
|                    |                    |                    | Delete the Permission referenced by PermKey.      |
|                    |                    |                    | You cannot normally delete a permission which     |
|                    |                    |                    | is still granted  to roles, however the           |
|                    |                    |                    | "force" property  allows you to do just that. To  |
|                    |                    |                    | do this: Add                                      |
|                    |                    |                    | 'force=true' as a query parameter.                |
|                    |                    |                    | **WARNING**: Using force will ungrant this        |
|                    |                    |                    | permission from all roles. Use with care.         |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               | 
|                    |                    |                    | Explicit HTTP Error Codes                         |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | DELETE             | /authz/perm/:name/ | Delete a  Permission                              |
|                    |                    |   :type/:action    | Delete the Permission referenced by :type         |
|                    |                    |                    | :instance: action                                 |
|                    |                    |                    | You cannot normally delete a permission which     |
|                    |                    |                    |  is still granted to roles, however the           |
|                    |                    |                    | "force" property  allows you to do                |
|                    |                    |                    | just that. To do this: Add  'force=true' as a     |
|                    |                    |                    | query parameter                                   |
|                    |                    |                    |                                                   |
|                    |                    |                    | WARNING: Using force will ungrant this permission |
|                    |                    |                    | from all roles. Use with care.                    |
|                    |                    |                    | ------------------------------------------------- |
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | type : string (Required)                          |
|                    |                    |                    | instance : string (Required)                      |
|                    |                    |                    | action : string (Required)                        |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | PUT                | /authz/perm/:type/ | Update a Permission                               |
|                    |                    | :instance/:action  |  Rename the Permission referenced by              |
|                    |                    |                    | :type :instance :action, and  rename              |
|                    |                    |                    | (copy/delete) to the Permission described in      |
|                    |                    |                    | PermRequest                                       |
|                    |                    |                    | -----------------------------------------------   |
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | type : string (Required)                          |
|                    |                    |                    | instance : string (Required)                      |
|                    |                    |                    | action : string (Required)                        |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406 ,409                                     |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | GET                | /authz/perms/:type | Get Permissions by Type                           |
|                    |                    |                    |                                                   |
|                    |                    |                    | List All Permissions that match the :type         |
|                    |                    |                    | element of the key                                |
|                    |                    |                    | ------------------------------------------------- |
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | type : string (Required)                          |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | GET                | /authz/perms/:type | Get Permissions by  Key                           |
|                    |                    | /:instance/:action |  List Permissions  that match key;                |
|                    |                    |                    | :type, :instance and :action                      |
|                    |                    |                    | --------------------------------------------------|
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | type : string (Required)                          |
|                    |                    |                    | instance : string (Required)                      |
|                    |                    |                    | action : string (Required)                        |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | GET                | /authz/perms/ns/:n | Get PermsByNS                                     |
|                    |                    | s                  | List All Permissions that are in Namespace :ns    |
|                    |                    |                    | --------------------------------------------------|
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | type : ns (Required)                              |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | GET                | /authz/perms/role/ | Get Permissions by Role                           |
|                    |                    |     :role          | List All Permissions that are granted to :role    |
|                    |                    |                    | --------------------------------------------------|
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | role : string (Required)                          |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | POST               | /authz/perms/user/ | Get Permissions by User, Query AAF Perms          |
|                    |                    | :user              |                                                   |
|                    |                    |                    | List All Permissions that   match user :user      |
|                    |                    |                    |                                                   |
|                    |                    |                    | 'user' must be expressed as full                  |
|                    |                    |                    | identity (ex:  id@full.domain.com)                |
|                    |                    |                    | Present Queries as  one or more Permissions (see  |
|                    |                    |                    | ContentType Links below for format). If the       |
|                    |                    |                    | Caller is Granted this specific Permission, and   |
|                    |                    |                    | the Permission is valid for the User, it will be  |
|                    |                    |                    | included in response permissions,along with all   |
|                    |                    |                    |  the normal permissions on the 'GET' version of   |
|                    |                    |                    |  this call. If it is not valid,or caller does not |
|                    |                    |                    | permission to see,  it will be removed from the   |
|                    |                    |                    | list.                                             |
|                    |                    |                    | \*Note: This design allows you to make one call   |
|                    |                    |                    |  for all expected permissions                     |
|                    |                    |                    |                                                   |
|                    |                    |                    | The permission to be included MUST be:            |
|                    |                    |                    | .access\|:[:key]\|                                |
|                    |                    |                    |                                                   |
|                    |                    |                    | examples:                                         |
|                    |                    |                    |                                                   |
|                    |                    |                    | com.att.myns.access|:ns|write                     |
|                    |                    |                    | com.att.myns.access\|:role:myrole\|create         |
|                    |                    |                    | com.att.myns.access\|:perm:mytype:myinstance:     |
|                    |                    |                    | myaction\|read                                    |
|                    |                    |                    | --------------------------------------------------|
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | user:string(Required)                             |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | GET                | /authz/perms/user/ | Get Permissions by User                           |
|                    |                    | :user              |                                                   |
|                    |                    |                    | List All Permissions that match user :user        |
|                    |                    |                    | 'user' must be  expressed as full                 |
|                    |                    |                    | identity (ex:id@full.domain.com)                  |
|                    |                    |                    | --------------------------------------------------|
|                    |                    |                    | Parameters                                        |
|                    |                    |                    | user:string(Required)                             |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
| ROLE               | POST               | /authz/role        | Create Role                                       |
|                    |                    |                    |                                                   |
|                    |                    |                    | Roles are part of Namespaces                      |
|                    |                    |                    | Examples:                                         |
|                    |                    |                    |                                                   |
|                    |                    |                    | -  com.att.aaf -  The team that   created and     |
|                    |                    |                    |    maintains AAF                                  |
|                    |                    |                    |                                                   |
|                    |                    |                    | Roles do not include implied  permissions for an  |
|                    |                    |                    | App. Instead, they contain explicit Granted       |
|                    |                    |                    |  Permissions by any Namespace in AAF              |
|                    |                    |                    | Restrictions on Role Names:                       |
|                    |                    |                    | -  Must start with valid Namespace name,          |
|                    |                    |                    |    terminated by .(dot/period)                    |
|                    |                    |                    | -  Allowed Characters are a-zA-Z0-9._-            |
|                    |                    |                    | -  role names are Case Sensitive                  |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 201                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 403, 404, 406, 409                                |
+--------------------+--------------------+--------------------+---------------------------------------------------+
|                    | PUT                | /authz/role        | Set Description for role                          |
|                    |                    |                    | Add Description  Data to a Role                   |
|                    |                    |                    | Expected HTTP Code                                |
|                    |                    |                    | 200                                               |
|                    |                    |                    | Explicit HTTP  Error Codes                        |
|                    |                    |                    | 404, 406                                          |
+--------------------+--------------------+--------------------+---------------------------------------------------+
