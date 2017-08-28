/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
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