.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Logging
=======

.. note::
   * This section is used to describe the informational or diagnostic messages emitted from 
     a software component and the methods or collecting them.
   
   * This section is typically: provided for a platform-component and sdk; and
     referenced in developer and user guides
   
   * This note must be removed after content has been added.


Where to Access Information
---------------------------
AAF uses log4j framework to generate logs and all the logs are stored in a persistent volume.

Error / Warning Messages
------------------------
Following are the error codes

| Create a Permission - Expected=201, Explicit=403, 404, 406, 409
| Set Description for Permission - Expected=200, Explicit=404, 406
| Delete a Permission Expected=200, Explicit=404, 406
| Update a Permission - Expected=200, Explicit==04, 406, 409
| Get Permissions by Type - Expected=200, Explicit=404, 406
| Get Permissions by Key - Expected=200, Explicit=404, 406
| Get PermsByNS - Expected=200, Explicit==404, 406
| Get Permissions by Role - Expected=200, Explicit=404, 406
| Get Permissions by User, Query AAF Perms - Expected=200, Explicit=404, 406
| Get Permissions by User - Expected=200, Explicit=404, 406
| Create Role - Expected=201, Explicit=403, 404, 406, 409
| Set Description for role= - Expected=200, Explicit=404, 406
| Delete Role - Expected=200, Explicit==404, 406
| Delete Permission from Role - Expected=200, Explicit=404, 406
| Add Permission to Role - Expected=201, Explicit=403, 404, 406, 409
| Set a Permission's Roles - Expected=201, Explicit=403, 404, 406, 409
| GetRolesByFullName - Expected=200, Explicit=404, 406
| GetRolesByNameOnly - Expected=200, Explicit=404, 406
| GetRolesByNS - Expected=200, Explicit=404, 406
| GetRolesByPerm - Expected=200, Explicit=404, 406
| GetRolesByUser - Expected=200, Explicit=404, 406
| Request User Role Access - Expected=201, Explicit=403, 404, 406, 409
| Get if User is In Role - Expected=200, Explicit=403, 404, 406
| Delete User Role - Expected=200, Explicit=403, 404, 406
| Update Users for a role - Expected=200, Explicit=403, 404, 406
| Update Roles for a user - Expected=200, Explicit=403, 404, 406
| Get UserRoles by Role - Expected=200, Explicit=404, 406
| Get UserRoles by User - Expected=200, Explicit=404, 406
| Create a Namespace - Expected=201, Explicit=403, 404, 406, 409
| Set a Description for a Namespace - Expected=200, Explicit=403, 404, 406
| Delete a Namespace - Expected=200, Explicit=403, 404, 424
| Add an Admin to a Namespace - Expected=201, Explicit=403, 404, 406, 409
| Remove an Admin from a Namespace - Expected=200, Explicit=403, 404
| Delete an Attribute from a Namespace - Expected=200, Explicit=403, 404
| Add an Attribute from a Namespace - Expected=201, Explicit=403, 404, 406, 409
| update an Attribute from a Namespace - Expected=200, Explicit=403, 404
| Add a Responsible Identity to a Namespace - Expected=201, Explicit=403, 404, 406, 409
| Remove a Responsible Identity from Namespace - Expected=200, Explicit=403, 404
| get Ns Key List From Attribute - Expected=200, Explicit=403, 404
| Return Information about Namespaces - Expected=200, Explicit=404, 406
| Return Child Namespaces - Expected=200, Explicit=403, 404
| Get Users By Permission - Expected=200, Explicit=404, 406
| Get Users By Role - Expected=200, Explicit=403, 404, 406
| Is given BasicAuth valid? - Expected=200, Explicit=403
| Is given Credential valid? - Expected=200, Explicit=403

