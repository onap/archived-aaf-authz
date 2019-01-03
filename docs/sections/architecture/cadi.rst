.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

CADI Architecture
=================

Authentication
--------------
Realize that the main purpose of CADI in the authentication sphere is to identify first which Authentication scheme is being used.  Therefore, when you’re debugging, you’ll find the terms “TAF” and “EpiTaf”.  The first, just think of it as a pluggable Authentication Module, only one of which is Basic Auth.  The others, in AT&T typically include “CSP Global Logon” and “X509 Certificates”.
 
TAF - "Transmutative Authentication Framework", these are essentially plugable Authentication elements.  Each is able 
The “EpiTaf” is a pun, of course, but it’s software purpose is to run each Module against the incoming transaction.  Each of which is a different scheme, and looks for different things.
 
Reading the Security Info
^^^^^^^^^^^^^^^^^^^^^^^^^
X509 – This checks the core HTTP stream to get a Client Certificate IF it is provided.  If so, it validates that it is signed by a trusted source, and if so, extracts the entity encoded, and creates a Principal to add to HTTPServletRequest.
 
Cookie (Pending Dublin)– This checks for a Cookie, and if present, decodes.  If so, it creates a Principal to add to HTTPServletRequest…
 
BasicAuth – This checks, as you see, the HTTP Servlet Header, but there are various permutations of exactly how it gets there, so there are, unfortunately, several paths that must be accommodated.  One of which is the Caching… we don’t want to hit the DB or remote User/Password API (provided by the organization) for the same password over and over, so we need to cache the response, and if it matches exactly, then create the Principal, etc. 

Success for Authentication
^^^^^^^^^^^^^^^^^^^^^^^^^^

In "raw" Java, the responses can simply be read from the "TAF" (or "EpiTaf") being used.

In J2EE, the "CADI Filter" will take positive Authentication and 
  # Associate the created Principal to the HTTPServletRequest (available as "getUserPrincipal()"
  # Add the ability to do Cached Authorizations to HTTPServletRequest (available as "isUserInRole(String s)" (see Authorization Section)
  # Send to next element in the Filter Chain
 
Responding to Failed Authentication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Finding different kinds of Authentication Schemes and Protocols on an incoming transaction are the relatively easy parts.  Responses are a bit harder.
 
X509 – Just because the TLS doesn’t include a valid and TRUSTED X509 certificate, doesn’t mean it is actually an invalid client certificate and should be rejected.  If you did, you would never actually try to find a BasicAuth “Authorization” header.  The cert might be from 1 way TLS or 2-way TLS with a valid Client cert, just not one where we can derive the Organizational Identity.   In this case, we let the other TAFs handle the return response.
 
Cookies – Cookies are only designed to work with Browsers.  The appropriate response is to redirect the browser to the Organization's SSO Logon Page, and type in your password on a form, this, of course, cannot be done by a App-to-Apps… Therefore, this is only sent back for Browsers.
 
BasicAuth – the fallback for App-to-App or when nothing else applies.  If all else fails, and there is either a bad password, or there is nothing else to try, we send back a 401.  Again, the Cookie Logon “Browser Redirect” takes precedence for Browser traffic, but if Cookie TAF not included or it’s APP to APP, then 401 is returned.
 
AAF is designed to cover Fine-Grained Authorization, meaning that the Authorizations provided are able to used an Application’s detailed authorizations, such as whether a user may be on a particular page, or has access to a particular Pub-SUB topic controlled within the App.

This is a critical function for Cloud environments, as Services need to be able to be installed and running in a very short time, and should not be encumbered with local configurations of Users, Permissions and Passwords.

To be effective during a computer transaction, Security must not only be secure, but very fast. Given that each transaction must be checked and validated for Authorization and Authentication, it is critical that all elements on this path perform optimally.

Authorization
--------------

The parallel interface for Authorization Modules is the "LUR" (Localized User Repository).

There are not typically use cases for defining more than one LUR source at a time, though it is possible. Caching, however, is still critical for overal System Performance.

For J2EE, the typical access method is on the HttpServletRequest "boolean isUserInRole(String s)", however, when the LUR is AAF (standard usage), then the use of the call should be taken to mean "boolean doesUserHavePermission(String)".  J2EE was created a long time ago before more Fine Grained Permission concepts. With AAF, you still make the call but pass in a single String Representation of the 3 part Permission concatenated with '|' sign.  Example:
  if(httpServletRequest.isUserInRole("org.onap.aai.access|*|read")) {
     // Do something that requires this permission to function

Used in this way, with the AAF LUR integrated in, you can easily perform cached Authorization checks on a variety of Permissions your Application has defined.

Special Enforcement Point Case
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Typically, the CADI Filter's job is done when Authenticating and passing on to the next Filter.  However, CADI also provides an API Enforcement Point, if configured.

This Enforcement Point, if configured by the "cadi_api_enforcement" property, is also a Filter.  It will use standard AAF Permissions to allow/disallow access up the filter chain based on the HTTP Path.  This eliminates the need for engaging additional Authorization libraries, plugins, etc, and stick with highly efficient AAF model already in place.

Example:

  HTTP Call GET /readX

  Given property "cadi_enforcement_point=org.onap.aai.api", and a permission "org.onap.aai.api|/readX|GET" defined in AAF, CADI will enforce that the incoming call must have been granted this permission before proceding to the remainder of the Servlet Code.
