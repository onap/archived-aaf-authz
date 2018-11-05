/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
 function http(meth, sURL, sInput, func) {
	if (sInput != "") { 
		var http;
		if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		  http=new XMLHttpRequest();
		} else {// code for IE6, IE5
		  http=new ActiveXObject('Microsoft.XMLHTTP');
		}
	
		http.onreadystatechange=function() {
		  if(http.readyState==4 && http.status == 200) {
			 func(http.responseText)
		  }
		  // Probably want Exception code too.
		}
		
		http.open(meth,sURL,false);
		http.setRequestHeader('Content-Type','text/plain;charset=UTF-8');
		http.send(sInput);
	}
}