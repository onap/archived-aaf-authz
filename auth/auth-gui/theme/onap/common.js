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
Object.defineProperty(Element.prototype, 'outerHeight', {
    'get': function(){
        var height = this.clientHeight;
        height += getStyle(this,'marginTop');
        height += getStyle(this,'marginBottom');
        height += getStyle(this,'borderTopWidth');
        height += getStyle(this,'borderBottomWidth');
        return height;
    }
});

if (document.addEventListener) {
	document.addEventListener('DOMContentLoaded', function () {
		var height = document.querySelector("#footer").outerHeight;
		document.querySelector("#inner").setAttribute("style",
				"margin-bottom:" + height.toString()+ "px");
	});
} else {
	window.attachEvent("onload", function () {
		var height = document.querySelector("#footer").outerHeight;
		document.querySelector("#inner").setAttribute("style",
				"margin-bottom:" + height.toString()+ "px");
	});
}



function getStyle(el, prop) {
	var result = el.currentStyle ? el.currentStyle[prop] :
		document.defaultView.getComputedStyle(el,"")[prop];
	if (parseInt(result,10))
		return parseInt(result,10);
	else
		return 0;
}

function divVisibility(divID) {
	var element = document.querySelector("#"+divID);
	if (element.style.display=="block")
		element.style.display="none";
	else
		element.style.display="block";
}

function datesURL(histPage) {
	var validated=true;
	var yearStart = document.querySelector('#yearStart').value;
	var yearEnd = document.querySelector('#yearEnd').value;
	var monthStart = document.querySelector('#monthStart').value;
	var monthEnd = document.querySelector('#monthEnd').value;
	if (monthStart.length == 1) monthStart = 0 + monthStart;
	if (monthEnd.length == 1) monthEnd = 0 + monthEnd;

	validated &= validateYear(yearStart);
	validated &= validateYear(yearEnd);
	validated &= validateMonth(monthStart);
	validated &= validateMonth(monthEnd);
	
	if (validated) window.location=histPage+"&dates="+yearStart+monthStart+"-"+yearEnd+monthEnd;
	else alert("Please correct your date selections");
}

function userFilter(approvalPage) {
	var user = document.querySelector('#userTextBox').value;
	if (user != "")
		window.location=approvalPage+"?user="+user;
	else
		window.location=approvalPage;
}

function validateYear(year) {
	var today = new Date();
	if (year >= 1900 && year <= today.getFullYear()) return true;
	else return false;
}

function validateMonth(month) {
	if (month) return true;
	else return false;
}

function alterLink(breadcrumbToFind, newTarget) {
	var breadcrumbs = document.querySelector("#breadcrumbs").getElementsByTagName("A");
	for (var i=0; i< breadcrumbs.length;i++) {
		var breadcrumbHref = breadcrumbs[i].getAttribute('href');
		if (breadcrumbHref.indexOf(breadcrumbToFind)>-1) 
			breadcrumbs[i].setAttribute('href', newTarget);
	}
}

// clipBoardData object not cross-browser supported. Only IE it seems
function copyToClipboard(controlId) { 
    var control = document.getElementById(controlId); 
    if (control == null) { 
    	alert("ERROR - control not found - " + controlId); 
    } else { 
    	var controlValue = control.href; 
    	window.clipboardData.setData("text/plain", controlValue); 
    	alert("Copied text to clipboard : " + controlValue); 
    } 
}
