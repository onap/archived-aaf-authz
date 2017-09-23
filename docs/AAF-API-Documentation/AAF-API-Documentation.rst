AT&T Auth Tool 
==============

--------------

AAF 2.0 RESTful interface
=========================

--------------

Accessing RESTful
-----------------

AAF RESTful service is secured by the following:

The Client must utilize HTTP/S. Non Secure HTTP is not acceptable

The Client MUST supply an Identity validated by one of the following mechanisms

-  Valid Global Login Cookie (CSP)
-  BASIC AUTH protocol using CSO Registered MechID, provisioned in AAF
-  (Near Future) Application level Certificate & oAuth

Responses

Each API Entity listed shows what structure will be accepted by service (ContentType) or responded with by service (Accept). Therefore, use these in making your call. Critical for PUT/POST.

Each API call may respond with JSON or XML. Choose the ContentType/Accept that has +json after the type for JSON or +xml after the Type for XML

XSDs for Versions


AAF can support multiple Versions of the API. Choose the ContentType/Accept that has the appropriate version=?.?


Character Restrictions

-  Character Restrictions must depend on the Enforcement Point used
-  Most AAF usage will be AAF Enforcement Point Characters for Instance and Action are:
    *a-zA-Z0-9,.()\_-=%*
    For Instance, you may declare a multi-dimensional key with : (colon) separator, example:

Ask for a Consultation on how these are typically used, or, if your tool is the only Enforcement Point, if set may be expanded

+--------------------+--------------------+--------------------+--------------------+
| Entity             | Method             | Path Info          | Description        |
+====================+====================+====================+====================+
| PERMISSION         | POST               | /authz/perm        | Create a           |
|                    |                    |                    | Permission         |
|                    |                    |                    |                    |
|                    |                    |                    | Permission         |
|                    |                    |                    | consists of:       |
|                    |                    |                    |                    |
|                    |                    |                    | -  type - a        |
|                    |                    |                    |    Namespace       |
|                    |                    |                    |    qualified       |
|                    |                    |                    |    identifier      |
|                    |                    |                    |    specifying what |
|                    |                    |                    |    kind of         |
|                    |                    |                    |    resource is     |
|                    |                    |                    |    being protected |
|                    |                    |                    | -  instance - a    |
|                    |                    |                    |    key, possibly   |
|                    |                    |                    |    multi-dimension |
|                    |                    |                    | al,                |
|                    |                    |                    |    that identifies |
|                    |                    |                    |    a specific      |
|                    |                    |                    |    instance of the |
|                    |                    |                    |    type            |
|                    |                    |                    | -  action - what   |
|                    |                    |                    |    kind of action  |
|                    |                    |                    |    is allowed      |
|                    |                    |                    |                    |
|                    |                    |                    | Note: instance and |
|                    |                    |                    | action can be an   |
|                    |                    |                    | \*                 |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/PermReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/PermRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/perm        | Set Description    |
|                    |                    |                    | for Permission     |
|                    |                    |                    |                    |
|                    |                    |                    | Add Description    |
|                    |                    |                    | Data to Perm       |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/PermReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/PermRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/perm        | Delete a           |
|                    |                    |                    | Permission         |
|                    |                    |                    |                    |
|                    |                    |                    | Delete the         |
|                    |                    |                    | Permission         |
|                    |                    |                    | referenced by      |
|                    |                    |                    | PermKey.           |
|                    |                    |                    |                    |
|                    |                    |                    | You cannot         |
|                    |                    |                    | normally delete a  |
|                    |                    |                    | permission which   |
|                    |                    |                    | is still granted   |
|                    |                    |                    | to roles,          |
|                    |                    |                    |                    |
|                    |                    |                    | however the        |
|                    |                    |                    | "force" property   |
|                    |                    |                    | allows you to do   |
|                    |                    |                    | just that. To do   |
|                    |                    |                    | this: Add          |
|                    |                    |                    |                    |
|                    |                    |                    | 'force=true' as a  |
|                    |                    |                    | query parameter.   |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Using     |
|                    |                    |                    | force will ungrant |
|                    |                    |                    | this permission    |
|                    |                    |                    | from all roles.    |
|                    |                    |                    | Use with care.     |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/PermReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/PermRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/perm/:name/ | Delete a           |
|                    |                    | :type/:action      | Permission         |
|                    |                    |                    |                    |
|                    |                    |                    | Delete the         |
|                    |                    |                    | Permission         |
|                    |                    |                    | referenced by      |
|                    |                    |                    | :type :instance    |
|                    |                    |                    | :action            |
|                    |                    |                    |                    |
|                    |                    |                    | You cannot         |
|                    |                    |                    | normally delete a  |
|                    |                    |                    | permission which   |
|                    |                    |                    | is still granted   |
|                    |                    |                    | to roles,          |
|                    |                    |                    |                    |
|                    |                    |                    | however the        |
|                    |                    |                    | "force" property   |
|                    |                    |                    | allows you to do   |
|                    |                    |                    | just that. To do   |
|                    |                    |                    | this: Add          |
|                    |                    |                    |                    |
|                    |                    |                    | 'force=true' as a  |
|                    |                    |                    | query parameter    |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Using     |
|                    |                    |                    | force will ungrant |
|                    |                    |                    | this permission    |
|                    |                    |                    | from all roles.    |
|                    |                    |                    | Use with care.     |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | instance : string  |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | action : string    |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybUt |
|                    |                    |                    | leStqc29uO3E9MS4wO |
|                    |                    |                    | 2NoYXJzZXQ9dXRmLTg |
|                    |                    |                    | 7dmVyc2lvbj0yLjAsY |
|                    |                    |                    | XBwbGljYXRpb24vanN |
|                    |                    |                    | vbjtxPTEuMDt2ZXJza |
|                    |                    |                    | W9uPTIuMCwqLyo7cT0 |
|                    |                    |                    | xLjA=>`__\ applica |
|                    |                    |                    | tion/PermKey+json; |
|                    |                    |                    | q=1.0;charset=utf- |
|                    |                    |                    | 8;version=2.0,appl |
|                    |                    |                    | ication/json;q=1.0 |
|                    |                    |                    | ;version=2.0,\*/\* |
|                    |                    |                    | ;q=1.0             |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybUt |
|                    |                    |                    | leSt4bWw7cT0xLjA7Y |
|                    |                    |                    | 2hhcnNldD11dGYtODt |
|                    |                    |                    | 2ZXJzaW9uPTIuMCx0Z |
|                    |                    |                    | Xh0L3htbDtxPTEuMDt |
|                    |                    |                    | 2ZXJzaW9uPTIuMA==> |
|                    |                    |                    | `__\ application/P |
|                    |                    |                    | ermKey+xml;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,text/xml;q= |
|                    |                    |                    | 1.0;version=2.0    |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/perm/:type/ | Update a           |
|                    |                    | :instance/:action  | Permission         |
|                    |                    |                    |                    |
|                    |                    |                    | Rename the         |
|                    |                    |                    | Permission         |
|                    |                    |                    | referenced by      |
|                    |                    |                    | :type :instance    |
|                    |                    |                    | :action, and       |
|                    |                    |                    | rename             |
|                    |                    |                    | (copy/delete) to   |
|                    |                    |                    | the Permission     |
|                    |                    |                    | described in       |
|                    |                    |                    | PermRequest        |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | instance : string  |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | action : string    |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406, 409      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/PermReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/PermRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/perms/:type | Get Permissions by |
|                    |                    |                    | Type               |
|                    |                    |                    |                    |
|                    |                    |                    | List All           |
|                    |                    |                    | Permissions that   |
|                    |                    |                    | match the :type    |
|                    |                    |                    | element of the key |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/perms/:type | Get Permissions by |
|                    |                    | /:instance/:action | Key                |
|                    |                    |                    |                    |
|                    |                    |                    | List Permissions   |
|                    |                    |                    | that match key;    |
|                    |                    |                    | :type, :instance   |
|                    |                    |                    | and :action        |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | instance : string  |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | action : string    |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/perms/ns/:n | Get PermsByNS      |
|                    |                    | s                  |                    |
|                    |                    |                    | List All           |
|                    |                    |                    | Permissions that   |
|                    |                    |                    | are in Namespace   |
|                    |                    |                    | :ns                |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MC4 |
|                    |                    |                    | y>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=0.2 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/perms/role/ | Get Permissions by |
|                    |                    | :role              | Role               |
|                    |                    |                    |                    |
|                    |                    |                    | List All           |
|                    |                    |                    | Permissions that   |
|                    |                    |                    | are granted to     |
|                    |                    |                    | :role              |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/perms/user/ | Get Permissions by |
|                    |                    | :user              | User, Query AAF    |
|                    |                    |                    | Perms              |
|                    |                    |                    |                    |
|                    |                    |                    | List All           |
|                    |                    |                    | Permissions that   |
|                    |                    |                    | match user :user   |
|                    |                    |                    |                    |
|                    |                    |                    | 'user' must be     |
|                    |                    |                    | expressed as full  |
|                    |                    |                    | identity (ex:      |
|                    |                    |                    | id@full.domain.com |
|                    |                    |                    | )                  |
|                    |                    |                    |                    |
|                    |                    |                    | Present Queries as |
|                    |                    |                    | one or more        |
|                    |                    |                    | Permissions (see   |
|                    |                    |                    | ContentType Links  |
|                    |                    |                    | below for format). |
|                    |                    |                    |                    |
|                    |                    |                    | If the Caller is   |
|                    |                    |                    | Granted this       |
|                    |                    |                    | specific           |
|                    |                    |                    | Permission, and    |
|                    |                    |                    | the Permission is  |
|                    |                    |                    | valid              |
|                    |                    |                    |                    |
|                    |                    |                    | for the User, it   |
|                    |                    |                    | will be included   |
|                    |                    |                    | in response        |
|                    |                    |                    | Permissions, along |
|                    |                    |                    | with               |
|                    |                    |                    |                    |
|                    |                    |                    | all the normal     |
|                    |                    |                    | permissions on the |
|                    |                    |                    | 'GET' version of   |
|                    |                    |                    | this call. If it   |
|                    |                    |                    | is not             |
|                    |                    |                    |                    |
|                    |                    |                    | valid, or Caller   |
|                    |                    |                    | does not have      |
|                    |                    |                    | permission to see, |
|                    |                    |                    | it will be removed |
|                    |                    |                    | from the list      |
|                    |                    |                    |                    |
|                    |                    |                    | \*Note: This       |
|                    |                    |                    | design allows you  |
|                    |                    |                    | to make one call   |
|                    |                    |                    | for all expected   |
|                    |                    |                    | permissions        |
|                    |                    |                    |                    |
|                    |                    |                    | The permission to  |
|                    |                    |                    | be included MUST   |
|                    |                    |                    | be:                |
|                    |                    |                    |                    |
|                    |                    |                    | .access\|:[:key]\| |
|                    |                    |                    |                    |
|                    |                    |                    | examples:          |
|                    |                    |                    |                    |
|                    |                    |                    | com.onap.myns.acces |
|                    |                    |                    | s\|:ns\|write      |
|                    |                    |                    |                    |
|                    |                    |                    | com.onap.myns.acces |
|                    |                    |                    | s\|:role:myrole\|c |
|                    |                    |                    | reate              |
|                    |                    |                    |                    |
|                    |                    |                    | com.onap.myns.acces |
|                    |                    |                    | s\|:perm:mytype:my |
|                    |                    |                    | instance:myaction\ |
|                    |                    |                    | |read              |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | user : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/perms/user/ | Get Permissions by |
|                    |                    | :user              | User               |
|                    |                    |                    |                    |
|                    |                    |                    | List All           |
|                    |                    |                    | Permissions that   |
|                    |                    |                    | match user :user   |
|                    |                    |                    |                    |
|                    |                    |                    | 'user' must be     |
|                    |                    |                    | expressed as full  |
|                    |                    |                    | identity (ex:      |
|                    |                    |                    | id@full.domain.com |
|                    |                    |                    | )                  |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | user : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MC4 |
|                    |                    |                    | y>`__\ application |
|                    |                    |                    | /Perms+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=0.2 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUGVybXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Perms |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
| ROLE               | POST               | /authz/role        | Create Role        |
|                    |                    |                    |                    |
|                    |                    |                    | Roles are part of  |
|                    |                    |                    | Namespaces         |
|                    |                    |                    |                    |
|                    |                    |                    | Examples:          |
|                    |                    |                    |                    |
|                    |                    |                    | -  com.onap.aaf -   |
|                    |                    |                    |    The team that   |
|                    |                    |                    |    created and     |
|                    |                    |                    |    maintains AAF   |
|                    |                    |                    | -  com.onap.csp -   |
|                    |                    |                    |    The team that   |
|                    |                    |                    |    created Global  |
|                    |                    |                    |    Login           |
|                    |                    |                    |                    |
|                    |                    |                    | Roles do not       |
|                    |                    |                    | include implied    |
|                    |                    |                    | permissions for an |
|                    |                    |                    | App. Instead, they |
|                    |                    |                    | contain explicit   |
|                    |                    |                    | Granted            |
|                    |                    |                    | Permissions by any |
|                    |                    |                    | Namespace in AAF   |
|                    |                    |                    | (See Permissions)  |
|                    |                    |                    |                    |
|                    |                    |                    | Restrictions on    |
|                    |                    |                    | Role Names:        |
|                    |                    |                    |                    |
|                    |                    |                    | -  Must start with |
|                    |                    |                    |    valid Namespace |
|                    |                    |                    |    name,           |
|                    |                    |                    |    terminated by . |
|                    |                    |                    |    (dot/period)    |
|                    |                    |                    | -  Allowed         |
|                    |                    |                    |    Characters are  |
|                    |                    |                    |    a-zA-Z0-9.\_-   |
|                    |                    |                    | -  role names are  |
|                    |                    |                    |    Case Sensitive  |
|                    |                    |                    |                    |
|                    |                    |                    | The right          |
|                    |                    |                    | questions to ask   |
|                    |                    |                    | for defining and   |
|                    |                    |                    | populating a Role  |
|                    |                    |                    | in AAF, therefore, |
|                    |                    |                    | are:               |
|                    |                    |                    |                    |
|                    |                    |                    | -  'What Job       |
|                    |                    |                    |    Function does   |
|                    |                    |                    |    this            |
|                    |                    |                    |    represent?'     |
|                    |                    |                    | -  'Does this      |
|                    |                    |                    |    person perform  |
|                    |                    |                    |    this Job        |
|                    |                    |                    |    Function?'      |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/RoleReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/RoleRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/role        | Set Description    |
|                    |                    |                    | for role           |
|                    |                    |                    |                    |
|                    |                    |                    | Add Description    |
|                    |                    |                    | Data to a Role     |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/RoleReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/RoleRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/role        | Delete Role        |
|                    |                    |                    |                    |
|                    |                    |                    | Delete the Role    |
|                    |                    |                    | referenced by      |
|                    |                    |                    | RoleKey            |
|                    |                    |                    |                    |
|                    |                    |                    | You cannot         |
|                    |                    |                    | normally delete a  |
|                    |                    |                    | role which still   |
|                    |                    |                    | has permissions    |
|                    |                    |                    | granted or users   |
|                    |                    |                    | assigned to it,    |
|                    |                    |                    |                    |
|                    |                    |                    | however the        |
|                    |                    |                    | "force" property   |
|                    |                    |                    | allows you to do   |
|                    |                    |                    | just that. To do   |
|                    |                    |                    | this: Add          |
|                    |                    |                    | 'force=true'       |
|                    |                    |                    |                    |
|                    |                    |                    | as a query         |
|                    |                    |                    | parameter.         |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Using     |
|                    |                    |                    | force will remove  |
|                    |                    |                    | all users and      |
|                    |                    |                    | permission from    |
|                    |                    |                    | this role. Use     |
|                    |                    |                    | with care.         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/RoleReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/RoleRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/role/:role  | Delete Role        |
|                    |                    |                    |                    |
|                    |                    |                    | Delete the Role    |
|                    |                    |                    | named :role        |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZSt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Role+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZSt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Role+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/role/:role/ | Delete Permission  |
|                    |                    | perm               | from Role          |
|                    |                    |                    |                    |
|                    |                    |                    | Ungrant a          |
|                    |                    |                    | permission from    |
|                    |                    |                    | Role :role         |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/R |
|                    |                    |                    | olePermRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/RolePermReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/role/perm   | Add Permission to  |
|                    |                    |                    | Role               |
|                    |                    |                    |                    |
|                    |                    |                    | Grant a Permission |
|                    |                    |                    | to a Role          |
|                    |                    |                    |                    |
|                    |                    |                    | Permission         |
|                    |                    |                    | consists of:       |
|                    |                    |                    |                    |
|                    |                    |                    | -  type - a        |
|                    |                    |                    |    Namespace       |
|                    |                    |                    |    qualified       |
|                    |                    |                    |    identifier      |
|                    |                    |                    |    specifying what |
|                    |                    |                    |    kind of         |
|                    |                    |                    |    resource is     |
|                    |                    |                    |    being protected |
|                    |                    |                    | -  instance - a    |
|                    |                    |                    |    key, possibly   |
|                    |                    |                    |    multi-dimension |
|                    |                    |                    | al,                |
|                    |                    |                    |    that identifies |
|                    |                    |                    |    a specific      |
|                    |                    |                    |    instance of the |
|                    |                    |                    |    type            |
|                    |                    |                    | -  action - what   |
|                    |                    |                    |    kind of action  |
|                    |                    |                    |    is allowed      |
|                    |                    |                    |                    |
|                    |                    |                    | Note: instance and |
|                    |                    |                    | action can be an   |
|                    |                    |                    | \*                 |
|                    |                    |                    |                    |
|                    |                    |                    | Note: Using the    |
|                    |                    |                    | "force" property   |
|                    |                    |                    | will create the    |
|                    |                    |                    | Permission, if it  |
|                    |                    |                    | doesn't exist AND  |
|                    |                    |                    | the requesting ID  |
|                    |                    |                    | is allowed to      |
|                    |                    |                    | create. It will    |
|                    |                    |                    | then grant         |
|                    |                    |                    |                    |
|                    |                    |                    | the permission to  |
|                    |                    |                    | the role in one    |
|                    |                    |                    | step. To do this:  |
|                    |                    |                    | add 'force=true'   |
|                    |                    |                    | as a query         |
|                    |                    |                    | parameter.         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/R |
|                    |                    |                    | olePermRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/RolePermReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/role/perm   | Set a Permission's |
|                    |                    |                    | Roles              |
|                    |                    |                    |                    |
|                    |                    |                    | Set a permission's |
|                    |                    |                    | roles to roles     |
|                    |                    |                    | given              |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/R |
|                    |                    |                    | olePermRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZVB |
|                    |                    |                    | lcm1SZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/RolePermReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/roles/:role | GetRolesByFullName |
|                    |                    |                    |                    |
|                    |                    |                    | List Roles that    |
|                    |                    |                    | match :role        |
|                    |                    |                    |                    |
|                    |                    |                    | Note: You must     |
|                    |                    |                    | have permission to |
|                    |                    |                    | see any given role |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Roles+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Roles |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/roles/name/ | GetRolesByNameOnly |
|                    |                    | :name              |                    |
|                    |                    |                    | List all Roles for |
|                    |                    |                    | only the Name of   |
|                    |                    |                    | Role (without      |
|                    |                    |                    | Namespace)         |
|                    |                    |                    |                    |
|                    |                    |                    | Note: You must     |
|                    |                    |                    | have permission to |
|                    |                    |                    | see any given role |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | name : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Roles+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Roles |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/roles/ns/:n | GetRolesByNS       |
|                    |                    | s                  |                    |
|                    |                    |                    | List all Roles for |
|                    |                    |                    | the Namespace :ns  |
|                    |                    |                    |                    |
|                    |                    |                    | Note: You must     |
|                    |                    |                    | have permission to |
|                    |                    |                    | see any given role |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MC4 |
|                    |                    |                    | y>`__\ application |
|                    |                    |                    | /Roles+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=0.2 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Roles |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/roles/perm/ | GetRolesByPerm     |
|                    |                    | :type/:instance/:a |                    |
|                    |                    | ction              | Find all Roles     |
|                    |                    |                    | containing the     |
|                    |                    |                    | given              |
|                    |                    |                    | Permission.Permiss |
|                    |                    |                    | ion                |
|                    |                    |                    | consists of:       |
|                    |                    |                    |                    |
|                    |                    |                    | -  type - a        |
|                    |                    |                    |    Namespace       |
|                    |                    |                    |    qualified       |
|                    |                    |                    |    identifier      |
|                    |                    |                    |    specifying what |
|                    |                    |                    |    kind of         |
|                    |                    |                    |    resource is     |
|                    |                    |                    |    being protected |
|                    |                    |                    | -  instance - a    |
|                    |                    |                    |    key, possibly   |
|                    |                    |                    |    multi-dimension |
|                    |                    |                    | al,                |
|                    |                    |                    |    that identifies |
|                    |                    |                    |    a specific      |
|                    |                    |                    |    instance of the |
|                    |                    |                    |    type            |
|                    |                    |                    | -  action - what   |
|                    |                    |                    |    kind of action  |
|                    |                    |                    |    is allowed      |
|                    |                    |                    |                    |
|                    |                    |                    | Notes: instance    |
|                    |                    |                    | and action can be  |
|                    |                    |                    | an \*              |
|                    |                    |                    |                    |
|                    |                    |                    | You must have      |
|                    |                    |                    | permission to see  |
|                    |                    |                    | any given role     |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | instance : string  |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | action : string    |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Roles+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Roles |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/roles/user/ | GetRolesByUser     |
|                    |                    | :name              |                    |
|                    |                    |                    | List all Roles     |
|                    |                    |                    | that match user    |
|                    |                    |                    | :name              |
|                    |                    |                    |                    |
|                    |                    |                    | 'user' must be     |
|                    |                    |                    | expressed as full  |
|                    |                    |                    | identity (ex:      |
|                    |                    |                    | id@full.domain.com |
|                    |                    |                    | )                  |
|                    |                    |                    |                    |
|                    |                    |                    | Note: You must     |
|                    |                    |                    | have permission to |
|                    |                    |                    | see any given role |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | name : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Roles+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vUm9sZXM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Roles |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/userRole    | Request User Role  |
|                    |                    |                    | Access             |
|                    |                    |                    |                    |
|                    |                    |                    | Create a UserRole  |
|                    |                    |                    | relationship (add  |
|                    |                    |                    | User to Role)      |
|                    |                    |                    |                    |
|                    |                    |                    | A UserRole is an   |
|                    |                    |                    | object             |
|                    |                    |                    | Representation of  |
|                    |                    |                    | membership of a    |
|                    |                    |                    | Role for limited   |
|                    |                    |                    | time.              |
|                    |                    |                    |                    |
|                    |                    |                    | If a shorter       |
|                    |                    |                    | amount of time for |
|                    |                    |                    | Role ownership is  |
|                    |                    |                    | required, use the  |
|                    |                    |                    | 'End' field.       |
|                    |                    |                    |                    |
|                    |                    |                    | \*\* Note: Owners  |
|                    |                    |                    | of Namespaces will |
|                    |                    |                    | be required to     |
|                    |                    |                    | revalidate users   |
|                    |                    |                    | in these roles     |
|                    |                    |                    |                    |
|                    |                    |                    | before Expirations |
|                    |                    |                    | expire. Namespace  |
|                    |                    |                    | owners will be     |
|                    |                    |                    | notified by email. |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/U |
|                    |                    |                    | serRoleRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/UserRoleReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/userRole/:u | Get if User is In  |
|                    |                    | ser/:role          | Role               |
|                    |                    |                    |                    |
|                    |                    |                    | Returns the User   |
|                    |                    |                    | (with Expiration   |
|                    |                    |                    | date from listed   |
|                    |                    |                    | User/Role) if it   |
|                    |                    |                    | exists             |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | user : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Users+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Users |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/userRole/:u | Delete User Role   |
|                    |                    | ser/:role          |                    |
|                    |                    |                    | Remove Role :role  |
|                    |                    |                    | from User :user.   |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | user : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/userRole/ex | Extend Expiration  |
|                    |                    | tend/:user/:role   |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/userRole/ro | Update Users for a |
|                    |                    | le                 | role               |
|                    |                    |                    |                    |
|                    |                    |                    | Set a Role's users |
|                    |                    |                    | to the users       |
|                    |                    |                    | specified in the   |
|                    |                    |                    | UserRoleRequest    |
|                    |                    |                    | object.            |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Users     |
|                    |                    |                    | supplied will be   |
|                    |                    |                    | the ONLY users     |
|                    |                    |                    | attached to this   |
|                    |                    |                    | role               |
|                    |                    |                    |                    |
|                    |                    |                    | If no users are    |
|                    |                    |                    | supplied, role's   |
|                    |                    |                    | users are reset.   |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/U |
|                    |                    |                    | serRoleRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/UserRoleReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/userRole/us | Update Roles for a |
|                    |                    | er                 | user               |
|                    |                    |                    |                    |
|                    |                    |                    | Set a User's roles |
|                    |                    |                    | to the roles       |
|                    |                    |                    | specified in the   |
|                    |                    |                    | UserRoleRequest    |
|                    |                    |                    | object.            |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Roles     |
|                    |                    |                    | supplied will be   |
|                    |                    |                    | the ONLY roles     |
|                    |                    |                    | attached to this   |
|                    |                    |                    | user               |
|                    |                    |                    |                    |
|                    |                    |                    | If no roles are    |
|                    |                    |                    | supplied, user's   |
|                    |                    |                    | roles are reset.   |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K2pzb |
|                    |                    |                    | 247cT0xLjA7Y2hhcnN |
|                    |                    |                    | ldD11dGYtODt2ZXJza |
|                    |                    |                    | W9uPTIuMCxhcHBsaWN |
|                    |                    |                    | hdGlvbi9qc29uO3E9M |
|                    |                    |                    | S4wO3ZlcnNpb249Mi4 |
|                    |                    |                    | wLCovKjtxPTEuMA==> |
|                    |                    |                    | `__\ application/U |
|                    |                    |                    | serRoleRequest+jso |
|                    |                    |                    | n;q=1.0;charset=ut |
|                    |                    |                    | f-8;version=2.0,ap |
|                    |                    |                    | plication/json;q=1 |
|                    |                    |                    | .0;version=2.0,\*/ |
|                    |                    |                    | \*;q=1.0           |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVSZXF1ZXN0K3htb |
|                    |                    |                    | DtxPTEuMDtjaGFyc2V |
|                    |                    |                    | 0PXV0Zi04O3ZlcnNpb |
|                    |                    |                    | 249Mi4wLHRleHQveG1 |
|                    |                    |                    | sO3E9MS4wO3ZlcnNpb |
|                    |                    |                    | 249Mi4w>`__\ appli |
|                    |                    |                    | cation/UserRoleReq |
|                    |                    |                    | uest+xml;q=1.0;cha |
|                    |                    |                    | rset=utf-8;version |
|                    |                    |                    | =2.0,text/xml;q=1. |
|                    |                    |                    | 0;version=2.0      |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/userRoles/r | Get UserRoles by   |
|                    |                    | ole/:role          | Role               |
|                    |                    |                    |                    |
|                    |                    |                    | List all Users     |
|                    |                    |                    | that are attached  |
|                    |                    |                    | to Role specified  |
|                    |                    |                    | in :role           |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVzK2pzb247cT0xL |
|                    |                    |                    | jA7Y2hhcnNldD11dGY |
|                    |                    |                    | tODt2ZXJzaW9uPTIuM |
|                    |                    |                    | CxhcHBsaWNhdGlvbi9 |
|                    |                    |                    | qc29uO3E9MS4wO3Zlc |
|                    |                    |                    | nNpb249Mi4wLCovKjt |
|                    |                    |                    | xPTAuMg==>`__\ app |
|                    |                    |                    | lication/UserRoles |
|                    |                    |                    | +json;q=1.0;charse |
|                    |                    |                    | t=utf-8;version=2. |
|                    |                    |                    | 0,application/json |
|                    |                    |                    | ;q=1.0;version=2.0 |
|                    |                    |                    | ,\*/\*;q=0.2       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVzK3htbDtxPTEuM |
|                    |                    |                    | DtjaGFyc2V0PXV0Zi0 |
|                    |                    |                    | 4O3ZlcnNpb249Mi4wL |
|                    |                    |                    | HRleHQveG1sO3E9MS4 |
|                    |                    |                    | wO3ZlcnNpb249Mi4w> |
|                    |                    |                    | `__\ application/U |
|                    |                    |                    | serRoles+xml;q=1.0 |
|                    |                    |                    | ;charset=utf-8;ver |
|                    |                    |                    | sion=2.0,text/xml; |
|                    |                    |                    | q=1.0;version=2.0  |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/userRoles/u | Get UserRoles by   |
|                    |                    | ser/:user          | User               |
|                    |                    |                    |                    |
|                    |                    |                    | List all UserRoles |
|                    |                    |                    | for :user          |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVzK2pzb247cT0xL |
|                    |                    |                    | jA7Y2hhcnNldD11dGY |
|                    |                    |                    | tODt2ZXJzaW9uPTIuM |
|                    |                    |                    | CxhcHBsaWNhdGlvbi9 |
|                    |                    |                    | qc29uO3E9MS4wO3Zlc |
|                    |                    |                    | nNpb249Mi4wLCovKjt |
|                    |                    |                    | xPTAuMg==>`__\ app |
|                    |                    |                    | lication/UserRoles |
|                    |                    |                    | +json;q=1.0;charse |
|                    |                    |                    | t=utf-8;version=2. |
|                    |                    |                    | 0,application/json |
|                    |                    |                    | ;q=1.0;version=2.0 |
|                    |                    |                    | ,\*/\*;q=0.2       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlclJ |
|                    |                    |                    | vbGVzK3htbDtxPTEuM |
|                    |                    |                    | DtjaGFyc2V0PXV0Zi0 |
|                    |                    |                    | 4O3ZlcnNpb249Mi4wL |
|                    |                    |                    | HRleHQveG1sO3E9MS4 |
|                    |                    |                    | wO3ZlcnNpb249Mi4w> |
|                    |                    |                    | `__\ application/U |
|                    |                    |                    | serRoles+xml;q=1.0 |
|                    |                    |                    | ;charset=utf-8;ver |
|                    |                    |                    | sion=2.0,text/xml; |
|                    |                    |                    | q=1.0;version=2.0  |
+--------------------+--------------------+--------------------+--------------------+
| NAMESPACE          | POST               | /authz/ns          | Create a Namespace |
|                    |                    |                    |                    |
|                    |                    |                    | Namespace consists |
|                    |                    |                    | of:                |
|                    |                    |                    |                    |
|                    |                    |                    | -  name - What you |
|                    |                    |                    |    want to call    |
|                    |                    |                    |    this Namespace  |
|                    |                    |                    | -  responsible(s)  |
|                    |                    |                    |    - Person(s) who |
|                    |                    |                    |    receive         |
|                    |                    |                    |    Notifications   |
|                    |                    |                    |    and approves    |
|                    |                    |                    |    Requests        |
|                    |                    |                    |                    |
|                    |                    |                    |    regarding this  |
|                    |                    |                    |    Namespace.      |
|                    |                    |                    |    Companies have  |
|                    |                    |                    |    Policies as to  |
|                    |                    |                    |    who may take on |
|                    |                    |                    |                    |
|                    |                    |                    |    this            |
|                    |                    |                    |    Responsibility. |
|                    |                    |                    |    Separate        |
|                    |                    |                    |    multiple        |
|                    |                    |                    |    identities with |
|                    |                    |                    |    commas          |
|                    |                    |                    |                    |
|                    |                    |                    | -  admin(s) -      |
|                    |                    |                    |    Person(s) who   |
|                    |                    |                    |    are allowed to  |
|                    |                    |                    |    make changes on |
|                    |                    |                    |    the namespace,  |
|                    |                    |                    |                    |
|                    |                    |                    |    including       |
|                    |                    |                    |    creating Roles, |
|                    |                    |                    |    Permissions and |
|                    |                    |                    |    Credentials.    |
|                    |                    |                    |    Separate        |
|                    |                    |                    |    multiple        |
|                    |                    |                    |                    |
|                    |                    |                    |    identities with |
|                    |                    |                    |    commas          |
|                    |                    |                    |                    |
|                    |                    |                    | Note: Namespaces   |
|                    |                    |                    | are dot-delimited  |
|                    |                    |                    | (i.e.              |
|                    |                    |                    | com.myCompany.myAp |
|                    |                    |                    | p)                 |
|                    |                    |                    | and must be        |
|                    |                    |                    |                    |
|                    |                    |                    | created with       |
|                    |                    |                    | parent credentials |
|                    |                    |                    | (i.e. To create    |
|                    |                    |                    | com.myCompany.myAp |
|                    |                    |                    | p,                 |
|                    |                    |                    | you must           |
|                    |                    |                    |                    |
|                    |                    |                    | be an admin of     |
|                    |                    |                    | com.myCompany or   |
|                    |                    |                    | com                |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNSZXF |
|                    |                    |                    | 1ZXN0K2pzb247cT0xL |
|                    |                    |                    | jA7Y2hhcnNldD11dGY |
|                    |                    |                    | tODt2ZXJzaW9uPTIuM |
|                    |                    |                    | CxhcHBsaWNhdGlvbi9 |
|                    |                    |                    | qc29uO3E9MS4wO3Zlc |
|                    |                    |                    | nNpb249Mi4wLCovKjt |
|                    |                    |                    | xPTEuMA==>`__\ app |
|                    |                    |                    | lication/NsRequest |
|                    |                    |                    | +json;q=1.0;charse |
|                    |                    |                    | t=utf-8;version=2. |
|                    |                    |                    | 0,application/json |
|                    |                    |                    | ;q=1.0;version=2.0 |
|                    |                    |                    | ,\*/\*;q=1.0       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNSZXF |
|                    |                    |                    | 1ZXN0K3htbDtxPTEuM |
|                    |                    |                    | DtjaGFyc2V0PXV0Zi0 |
|                    |                    |                    | 4O3ZlcnNpb249Mi4wL |
|                    |                    |                    | HRleHQveG1sO3E9MS4 |
|                    |                    |                    | wO3ZlcnNpb249Mi4w> |
|                    |                    |                    | `__\ application/N |
|                    |                    |                    | sRequest+xml;q=1.0 |
|                    |                    |                    | ;charset=utf-8;ver |
|                    |                    |                    | sion=2.0,text/xml; |
|                    |                    |                    | q=1.0;version=2.0  |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/ns          | Set a Description  |
|                    |                    |                    | for a Namespace    |
|                    |                    |                    |                    |
|                    |                    |                    | Replace the        |
|                    |                    |                    | Current            |
|                    |                    |                    | Description of a   |
|                    |                    |                    | Namespace with a   |
|                    |                    |                    | new one            |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNSZXF |
|                    |                    |                    | 1ZXN0K2pzb247cT0xL |
|                    |                    |                    | jA7Y2hhcnNldD11dGY |
|                    |                    |                    | tODt2ZXJzaW9uPTIuM |
|                    |                    |                    | CxhcHBsaWNhdGlvbi9 |
|                    |                    |                    | qc29uO3E9MS4wO3Zlc |
|                    |                    |                    | nNpb249Mi4wLCovKjt |
|                    |                    |                    | xPTEuMA==>`__\ app |
|                    |                    |                    | lication/NsRequest |
|                    |                    |                    | +json;q=1.0;charse |
|                    |                    |                    | t=utf-8;version=2. |
|                    |                    |                    | 0,application/json |
|                    |                    |                    | ;q=1.0;version=2.0 |
|                    |                    |                    | ,\*/\*;q=1.0       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNSZXF |
|                    |                    |                    | 1ZXN0K3htbDtxPTEuM |
|                    |                    |                    | DtjaGFyc2V0PXV0Zi0 |
|                    |                    |                    | 4O3ZlcnNpb249Mi4wL |
|                    |                    |                    | HRleHQveG1sO3E9MS4 |
|                    |                    |                    | wO3ZlcnNpb249Mi4w> |
|                    |                    |                    | `__\ application/N |
|                    |                    |                    | sRequest+xml;q=1.0 |
|                    |                    |                    | ;charset=utf-8;ver |
|                    |                    |                    | sion=2.0,text/xml; |
|                    |                    |                    | q=1.0;version=2.0  |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/ns/:ns      | Delete a Namespace |
|                    |                    |                    |                    |
|                    |                    |                    | Delete the         |
|                    |                    |                    | Namespace :ns.     |
|                    |                    |                    | Namespaces cannot  |
|                    |                    |                    | normally be        |
|                    |                    |                    | deleted when there |
|                    |                    |                    |                    |
|                    |                    |                    | are still          |
|                    |                    |                    | credentials        |
|                    |                    |                    | associated with    |
|                    |                    |                    | them, but they can |
|                    |                    |                    | be deleted by      |
|                    |                    |                    | setting            |
|                    |                    |                    |                    |
|                    |                    |                    | the "force"        |
|                    |                    |                    | property. To do    |
|                    |                    |                    | this: Add          |
|                    |                    |                    | 'force=true' as a  |
|                    |                    |                    | query parameter    |
|                    |                    |                    |                    |
|                    |                    |                    | WARNING: Using     |
|                    |                    |                    | force will delete  |
|                    |                    |                    | all credentials    |
|                    |                    |                    | attached to this   |
|                    |                    |                    | namespace. Use     |
|                    |                    |                    | with care.         |
|                    |                    |                    |                    |
|                    |                    |                    | if the "force"     |
|                    |                    |                    | property is set to |
|                    |                    |                    | 'force=move', then |
|                    |                    |                    | Permissions and    |
|                    |                    |                    | Roles are not      |
|                    |                    |                    | deleted,but are    |
|                    |                    |                    | retained, and      |
|                    |                    |                    | assigned to the    |
|                    |                    |                    | Parent Namespace.  |
|                    |                    |                    | 'force=move' is    |
|                    |                    |                    | not permitted at   |
|                    |                    |                    | or below           |
|                    |                    |                    | Application Scope  |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 424      |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/ns/:ns/admi | Add an Admin to a  |
|                    |                    | n/:id              | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | Add an Identity    |
|                    |                    |                    | :id to the list of |
|                    |                    |                    | Admins for the     |
|                    |                    |                    | Namespace :ns      |
|                    |                    |                    |                    |
|                    |                    |                    | Note: :id must be  |
|                    |                    |                    | fully qualified    |
|                    |                    |                    |                    |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/ns/:ns/admi | Remove an Admin    |
|                    |                    | n/:id              | from a Namespace   |
|                    |                    |                    |                    |
|                    |                    |                    | Remove an Identity |
|                    |                    |                    | :id from the list  |
|                    |                    |                    | of Admins for the  |
|                    |                    |                    | Namespace :ns      |
|                    |                    |                    |                    |
|                    |                    |                    | Note: :id must be  |
|                    |                    |                    | fully qualified    |
|                    |                    |                    |                    |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/ns/:ns/attr | delete an          |
|                    |                    | ib/:key            | Attribute from a   |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | Delete an          |
|                    |                    |                    | attribute in the   |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | You must be given  |
|                    |                    |                    | direct permission  |
|                    |                    |                    | for key by AAF     |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | key : string       |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/ns/:ns/attr | Add an Attribute   |
|                    |                    | ib/:key/:value     | from a Namespace   |
|                    |                    |                    |                    |
|                    |                    |                    | Create an          |
|                    |                    |                    | attribute in the   |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | You must be given  |
|                    |                    |                    | direct permission  |
|                    |                    |                    | for key by AAF     |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | key : string       |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | value : string     |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | PUT                | /authz/ns/:ns/attr | update an          |
|                    |                    | ib/:key/:value     | Attribute from a   |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | Update Value on an |
|                    |                    |                    | existing attribute |
|                    |                    |                    | in the Namespace   |
|                    |                    |                    |                    |
|                    |                    |                    | You must be given  |
|                    |                    |                    | direct permission  |
|                    |                    |                    | for key by AAF     |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | key : string       |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authz/ns/:ns/resp | Add a Responsible  |
|                    |                    | onsible/:id        | Identity to a      |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | Add an Identity    |
|                    |                    |                    | :id to the list of |
|                    |                    |                    | Responsibles for   |
|                    |                    |                    | the Namespace :ns  |
|                    |                    |                    |                    |
|                    |                    |                    | Note: :id must be  |
|                    |                    |                    | fully qualified    |
|                    |                    |                    | 	                |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 201                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406, 409 |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | DELETE             | /authz/ns/:ns/resp | Remove a           |
|                    |                    | onsible/:id        | Responsible        |
|                    |                    |                    | Identity from      |
|                    |                    |                    | Namespace          |
|                    |                    |                    |                    |
|                    |                    |                    | Remove an Identity |
|                    |                    |                    | :id to the list of |
|                    |                    |                    | Responsibles for   |
|                    |                    |                    | the Namespace :ns  |
|                    |                    |                    |                    |
|                    |                    |                    | Note: :id must be  |
|                    |                    |                    | fully qualified    |
|                    |                    |                    |                    |
|                    |                    |                    |                    |
|                    |                    |                    | Note: A namespace  |
|                    |                    |                    | must have at least |
|                    |                    |                    | 1 responsible      |
|                    |                    |                    | party              |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | ns : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Void+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVm9pZCt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Void+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/ns/attrib/: | get Ns Key List    |
|                    |                    | key                | From Attribute     |
|                    |                    |                    |                    |
|                    |                    |                    | Read Attributes    |
|                    |                    |                    | for Namespace      |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | key : string       |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vS2V5cyt |
|                    |                    |                    | qc29uO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsYXBwb |
|                    |                    |                    | GljYXRpb24vanNvbjt |
|                    |                    |                    | xPTEuMDt2ZXJzaW9uP |
|                    |                    |                    | TIuMCwqLyo7cT0xLjA |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Keys+json;q=1.0;c |
|                    |                    |                    | harset=utf-8;versi |
|                    |                    |                    | on=2.0,application |
|                    |                    |                    | /json;q=1.0;versio |
|                    |                    |                    | n=2.0,\*/\*;q=1.0  |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vS2V5cyt |
|                    |                    |                    | 4bWw7cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCx0ZXh0L |
|                    |                    |                    | 3htbDtxPTEuMDt2ZXJ |
|                    |                    |                    | zaW9uPTIuMA==>`__\ |
|                    |                    |                    |  application/Keys+ |
|                    |                    |                    | xml;q=1.0;charset= |
|                    |                    |                    | utf-8;version=2.0, |
|                    |                    |                    | text/xml;q=1.0;ver |
|                    |                    |                    | sion=2.0           |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/nss/:id     | Return Information |
|                    |                    |                    | about Namespaces   |
|                    |                    |                    |                    |
|                    |                    |                    | Lists the          |
|                    |                    |                    | Admin(s),          |
|                    |                    |                    | Responsible        |
|                    |                    |                    | Party(s), Role(s), |
|                    |                    |                    | Permission(s)      |
|                    |                    |                    |                    |
|                    |                    |                    | Credential(s) and  |
|                    |                    |                    | Expiration of      |
|                    |                    |                    | Credential(s) in   |
|                    |                    |                    | Namespace :id      |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK2p |
|                    |                    |                    | zb247cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCxhcHBsa |
|                    |                    |                    | WNhdGlvbi9qc29uO3E |
|                    |                    |                    | 9MS4wO3ZlcnNpb249M |
|                    |                    |                    | i4wLCovKjtxPTEuMA= |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Nss+json;q=1.0;ch |
|                    |                    |                    | arset=utf-8;versio |
|                    |                    |                    | n=2.0,application/ |
|                    |                    |                    | json;q=1.0;version |
|                    |                    |                    | =2.0,\*/\*;q=1.0   |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK3h |
|                    |                    |                    | tbDtxPTEuMDtjaGFyc |
|                    |                    |                    | 2V0PXV0Zi04O3ZlcnN |
|                    |                    |                    | pb249Mi4wLHRleHQve |
|                    |                    |                    | G1sO3E9MS4wO3ZlcnN |
|                    |                    |                    | pb249Mi4w>`__\ app |
|                    |                    |                    | lication/Nss+xml;q |
|                    |                    |                    | =1.0;charset=utf-8 |
|                    |                    |                    | ;version=2.0,text/ |
|                    |                    |                    | xml;q=1.0;version= |
|                    |                    |                    | 2.0                |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/nss/admin/: | Return Namespaces  |
|                    |                    | user               | where User is an   |
|                    |                    |                    | Admin              |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK2p |
|                    |                    |                    | zb247cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCxhcHBsa |
|                    |                    |                    | WNhdGlvbi9qc29uO3E |
|                    |                    |                    | 9MS4wO3ZlcnNpb249M |
|                    |                    |                    | i4wLCovKjtxPTEuMA= |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Nss+json;q=1.0;ch |
|                    |                    |                    | arset=utf-8;versio |
|                    |                    |                    | n=2.0,application/ |
|                    |                    |                    | json;q=1.0;version |
|                    |                    |                    | =2.0,\*/\*;q=1.0   |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK3h |
|                    |                    |                    | tbDtxPTEuMDtjaGFyc |
|                    |                    |                    | 2V0PXV0Zi04O3ZlcnN |
|                    |                    |                    | pb249Mi4wLHRleHQve |
|                    |                    |                    | G1sO3E9MS4wO3ZlcnN |
|                    |                    |                    | pb249Mi4w>`__\ app |
|                    |                    |                    | lication/Nss+xml;q |
|                    |                    |                    | =1.0;charset=utf-8 |
|                    |                    |                    | ;version=2.0,text/ |
|                    |                    |                    | xml;q=1.0;version= |
|                    |                    |                    | 2.0                |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/nss/childre | Return Child       |
|                    |                    | n/:id              | Namespaces         |
|                    |                    |                    |                    |
|                    |                    |                    | Lists all Child    |
|                    |                    |                    | Namespaces of      |
|                    |                    |                    | Namespace :id      |
|                    |                    |                    |                    |
|                    |                    |                    | Note: This is not  |
|                    |                    |                    | a cached read      |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | id : string        |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK2p |
|                    |                    |                    | zb247cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCxhcHBsa |
|                    |                    |                    | WNhdGlvbi9qc29uO3E |
|                    |                    |                    | 9MS4wO3ZlcnNpb249M |
|                    |                    |                    | i4wLCovKjtxPTEuMA= |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Nss+json;q=1.0;ch |
|                    |                    |                    | arset=utf-8;versio |
|                    |                    |                    | n=2.0,application/ |
|                    |                    |                    | json;q=1.0;version |
|                    |                    |                    | =2.0,\*/\*;q=1.0   |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK3h |
|                    |                    |                    | tbDtxPTEuMDtjaGFyc |
|                    |                    |                    | 2V0PXV0Zi04O3ZlcnN |
|                    |                    |                    | pb249Mi4wLHRleHQve |
|                    |                    |                    | G1sO3E9MS4wO3ZlcnN |
|                    |                    |                    | pb249Mi4w>`__\ app |
|                    |                    |                    | lication/Nss+xml;q |
|                    |                    |                    | =1.0;charset=utf-8 |
|                    |                    |                    | ;version=2.0,text/ |
|                    |                    |                    | xml;q=1.0;version= |
|                    |                    |                    | 2.0                |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/nss/either/ | Return Namespaces  |
|                    |                    | :user              | where User Admin   |
|                    |                    |                    | or Owner           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK2p |
|                    |                    |                    | zb247cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCxhcHBsa |
|                    |                    |                    | WNhdGlvbi9qc29uO3E |
|                    |                    |                    | 9MS4wO3ZlcnNpb249M |
|                    |                    |                    | i4wLCovKjtxPTAuOA= |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Nss+json;q=1.0;ch |
|                    |                    |                    | arset=utf-8;versio |
|                    |                    |                    | n=2.0,application/ |
|                    |                    |                    | json;q=1.0;version |
|                    |                    |                    | =2.0,\*/\*;q=0.8   |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK3h |
|                    |                    |                    | tbDtxPTEuMDtjaGFyc |
|                    |                    |                    | 2V0PXV0Zi04O3ZlcnN |
|                    |                    |                    | pb249Mi4wLHRleHQve |
|                    |                    |                    | G1sO3E9MS4wO3ZlcnN |
|                    |                    |                    | pb249Mi4w>`__\ app |
|                    |                    |                    | lication/Nss+xml;q |
|                    |                    |                    | =1.0;charset=utf-8 |
|                    |                    |                    | ;version=2.0,text/ |
|                    |                    |                    | xml;q=1.0;version= |
|                    |                    |                    | 2.0                |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/nss/respons | Return Namespaces  |
|                    |                    | ible/:user         | where User is      |
|                    |                    |                    | Responsible        |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK2p |
|                    |                    |                    | zb247cT0xLjA7Y2hhc |
|                    |                    |                    | nNldD11dGYtODt2ZXJ |
|                    |                    |                    | zaW9uPTIuMCxhcHBsa |
|                    |                    |                    | WNhdGlvbi9qc29uO3E |
|                    |                    |                    | 9MS4wO3ZlcnNpb249M |
|                    |                    |                    | i4wLCovKjtxPTEuMA= |
|                    |                    |                    | =>`__\ application |
|                    |                    |                    | /Nss+json;q=1.0;ch |
|                    |                    |                    | arset=utf-8;versio |
|                    |                    |                    | n=2.0,application/ |
|                    |                    |                    | json;q=1.0;version |
|                    |                    |                    | =2.0,\*/\*;q=1.0   |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vTnNzK3h |
|                    |                    |                    | tbDtxPTEuMDtjaGFyc |
|                    |                    |                    | 2V0PXV0Zi04O3ZlcnN |
|                    |                    |                    | pb249Mi4wLHRleHQve |
|                    |                    |                    | G1sO3E9MS4wO3ZlcnN |
|                    |                    |                    | pb249Mi4w>`__\ app |
|                    |                    |                    | lication/Nss+xml;q |
|                    |                    |                    | =1.0;charset=utf-8 |
|                    |                    |                    | ;version=2.0,text/ |
|                    |                    |                    | xml;q=1.0;version= |
|                    |                    |                    | 2.0                |
+--------------------+--------------------+--------------------+--------------------+
| USER               | GET                | /authn/basicAuth   | Is given BasicAuth |
|                    |                    |                    | valid?             |
|                    |                    |                    |                    |
|                    |                    |                    | !!!! DEPRECATED    |
|                    |                    |                    | without X509       |
|                    |                    |                    | Authentication     |
|                    |                    |                    | STOP USING THIS    |
|                    |                    |                    | API BY DECEMBER    |
|                    |                    |                    | 2017, or use       |
|                    |                    |                    | Certificates !!!!  |
|                    |                    |                    | Use                |
|                    |                    |                    | /authn/validate    |
|                    |                    |                    | instead Note:      |
|                    |                    |                    | Validate a         |
|                    |                    |                    | Password using     |
|                    |                    |                    | BasicAuth Base64   |
|                    |                    |                    | encoded Header.    |
|                    |                    |                    | This HTTP/S call   |
|                    |                    |                    | is intended as a   |
|                    |                    |                    | fast User/Password |
|                    |                    |                    | lookup for         |
|                    |                    |                    | Security           |
|                    |                    |                    | Frameworks, and    |
|                    |                    |                    | responds 200 if it |
|                    |                    |                    | passes BasicAuth   |
|                    |                    |                    | security, and 403  |
|                    |                    |                    | if it does not.    |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403                |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | text/plain         |
+--------------------+--------------------+--------------------+--------------------+
|                    | POST               | /authn/validate    | Is given           |
|                    |                    |                    | Credential valid?  |
|                    |                    |                    |                    |
|                    |                    |                    | Validate a         |
|                    |                    |                    | Credential given a |
|                    |                    |                    | Credential         |
|                    |                    |                    | Structure. This is |
|                    |                    |                    | a more             |
|                    |                    |                    | comprehensive      |
|                    |                    |                    | validation, can do |
|                    |                    |                    | more than          |
|                    |                    |                    | BasicAuth as       |
|                    |                    |                    | Credential types   |
|                    |                    |                    | exp                |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403                |
|                    |                    |                    |                    |
|                    |                    |                    | ContentType:       |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vQ3JlZFJ |
|                    |                    |                    | lcXVlc3QranNvbjtxP |
|                    |                    |                    | TEuMDtjaGFyc2V0PXV |
|                    |                    |                    | 0Zi04O3ZlcnNpb249M |
|                    |                    |                    | i4wLGFwcGxpY2F0aW9 |
|                    |                    |                    | uL2pzb247cT0xLjA7d |
|                    |                    |                    | mVyc2lvbj0yLjAsKi8 |
|                    |                    |                    | qO3E9MS4w>`__\ app |
|                    |                    |                    | lication/CredReque |
|                    |                    |                    | st+json;q=1.0;char |
|                    |                    |                    | set=utf-8;version= |
|                    |                    |                    | 2.0,application/js |
|                    |                    |                    | on;q=1.0;version=2 |
|                    |                    |                    | .0,\*/\*;q=1.0     |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vQ3JlZFJ |
|                    |                    |                    | lcXVlc3QreG1sO3E9M |
|                    |                    |                    | S4wO2NoYXJzZXQ9dXR |
|                    |                    |                    | mLTg7dmVyc2lvbj0yL |
|                    |                    |                    | jAsdGV4dC94bWw7cT0 |
|                    |                    |                    | xLjA7dmVyc2lvbj0yL |
|                    |                    |                    | jA=>`__\ applicati |
|                    |                    |                    | on/CredRequest+xml |
|                    |                    |                    | ;q=1.0;charset=utf |
|                    |                    |                    | -8;version=2.0,tex |
|                    |                    |                    | t/xml;q=1.0;versio |
|                    |                    |                    | n=2.0              |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/users/:user | Get if User is In  |
|                    |                    | /:role             | Role               |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Users+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Users |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/users/perm/ | Get Users By       |
|                    |                    | :type/:instance/:a | Permission         |
|                    |                    | ction              |                    |
|                    |                    |                    | List all Users     |
|                    |                    |                    | that have          |
|                    |                    |                    | Permission         |
|                    |                    |                    | specified by :type |
|                    |                    |                    | :instance :action  |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | type : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | instance : string  |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | action : string    |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 404, 406           |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MS4 |
|                    |                    |                    | w>`__\ application |
|                    |                    |                    | /Users+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=1.0 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Users |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+
|                    | GET                | /authz/users/role/ | Get Users By Role  |
|                    |                    | :role              |                    |
|                    |                    |                    | Returns the User   |
|                    |                    |                    | (with Expiration   |
|                    |                    |                    | date from listed   |
|                    |                    |                    | User/Role) if it   |
|                    |                    |                    | exists             |
|                    |                    |                    |                    |
|                    |                    |                    | --------------     |
|                    |                    |                    |                    |
|                    |                    |                    | Parameters         |
|                    |                    |                    |                    |
|                    |                    |                    | user : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | role : string      |
|                    |                    |                    | (Required)         |
|                    |                    |                    |                    |
|                    |                    |                    | Expected HTTP Code |
|                    |                    |                    |                    |
|                    |                    |                    | 200                |
|                    |                    |                    |                    |
|                    |                    |                    | Explicit HTTP      |
|                    |                    |                    | Error Codes        |
|                    |                    |                    |                    |
|                    |                    |                    | 403, 404, 406      |
|                    |                    |                    |                    |
|                    |                    |                    | Accept:            |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | ranNvbjtxPTEuMDtja |
|                    |                    |                    | GFyc2V0PXV0Zi04O3Z |
|                    |                    |                    | lcnNpb249Mi4wLGFwc |
|                    |                    |                    | GxpY2F0aW9uL2pzb24 |
|                    |                    |                    | 7cT0xLjA7dmVyc2lvb |
|                    |                    |                    | j0yLjAsKi8qO3E9MC4 |
|                    |                    |                    | y>`__\ application |
|                    |                    |                    | /Users+json;q=1.0; |
|                    |                    |                    | charset=utf-8;vers |
|                    |                    |                    | ion=2.0,applicatio |
|                    |                    |                    | n/json;q=1.0;versi |
|                    |                    |                    | on=2.0,\*/\*;q=0.2 |
|                    |                    |                    |                    |
|                    |                    |                    | ` <./example/YXBwb |
|                    |                    |                    | GljYXRpb24vVXNlcnM |
|                    |                    |                    | reG1sO3E9MS4wO2NoY |
|                    |                    |                    | XJzZXQ9dXRmLTg7dmV |
|                    |                    |                    | yc2lvbj0yLjAsdGV4d |
|                    |                    |                    | C94bWw7cT0xLjA7dmV |
|                    |                    |                    | yc2lvbj0yLjA=>`__\ |
|                    |                    |                    |  application/Users |
|                    |                    |                    | +xml;q=1.0;charset |
|                    |                    |                    | =utf-8;version=2.0 |
|                    |                    |                    | ,text/xml;q=1.0;ve |
|                    |                    |                    | rsion=2.0          |
+--------------------+--------------------+--------------------+--------------------+

