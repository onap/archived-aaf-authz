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
function getCommand() {
	if(typeof String.prototype.trim !== 'function') {
		String.prototype.trim = function() {
			return this.replace(/^\s+|\s+$/g, ''); 
		};
	}

	var cmds = [];
	cmds = document.querySelector("#command_field").value.split(" ");
	var cleanCmd = "";
	if (document.querySelector("#details_img").getAttribute("class") == "selected") 
		cleanCmd += "details ";
	for (var i = 0; i < cmds.length;i++) {
		var trimmed = cmds[i].trim();
		if (trimmed != "")
			cleanCmd += trimmed + " ";
	}
	
	return cleanCmd.trim();
}

function moveCommandToDiv() {

	var textInput = document.querySelector("#command_field");
	var content = document.createTextNode(textInput.value);
	var parContent = document.createElement("p");
	var consoleDiv = document.querySelector("#console_area");
	var commandCount = consoleDiv.querySelectorAll(".command").length;
	parContent.setAttribute("class", "command");
	parContent.appendChild(content);
	consoleDiv.appendChild(parContent);

	textInput.value = "";
}

function printResponse(response) {
	var parContent = document.createElement("p");
	parContent.setAttribute("class", "response");
	var preTag = document.createElement("pre");
	parContent.appendChild(preTag);
	var content = document.createTextNode(response);
	preTag.appendChild(content);
	var consoleDiv = document.querySelector("#console_area");
	consoleDiv.appendChild(parContent);
	
	consoleDiv.scrollTop = consoleDiv.scrollHeight;
}

function clearHistory() {
	var consoleDiv = document.querySelector("#console_area");
	var curr;
	while (curr=consoleDiv.firstChild) {
		consoleDiv.removeChild(curr);
	}
	document.querySelector("#command_field").value = "";
	currentCmd = 0;
}

function buttonChangeFontSize(direction) {
	var slider = document.querySelector("#text_size_slider");
	var currentSize = parseInt(slider.value);
	var newSize;
	if (direction == "inc") {
		newSize = currentSize + 10;
	} else {
		newSize = currentSize - 10;
	}
	if (newSize > slider.max) newSize = parseInt(slider.max);
	if (newSize < slider.min) newSize = parseInt(slider.min);
	slider.value = newSize;
	changeFontSize(newSize);
}

function changeFontSize(size) {
	var consoleDiv = document.querySelector("#console_area");
	consoleDiv.style.fontSize = size + "%";
}

function handleDivHiding(id, img) {
	var options_link = document.querySelector("#options_link");
	var divHeight = toggleVisibility(document.querySelector("#"+id));

	if (id == 'options') {
		if (options_link.getAttribute("class") == "open") {
			changeImg(document.querySelector("#options_img"), "../../theme/onap/options_down.png");
			options_link.setAttribute("class", "closed");
		} else {
			changeImg(document.querySelector("#options_img"), "../../theme/onap/options_up.png");
			options_link.setAttribute("class", "open");
		}
		moveToggleImg(options_link, divHeight);
	} else { //id=text_slider
		selectOption(img,divHeight);
	}

}

function selectOption(img, divHeight) {
	var options_link = document.querySelector("#options_link");
	var anySelected;
	if (img.getAttribute("class") != "selected") {
		anySelected = document.querySelectorAll(".selected").length>0;
		if (anySelected == false)
			divHeight += 4;
		img.setAttribute("class", "selected");
	} else {
		img.setAttribute("class", "");
		anySelected = document.querySelectorAll(".selected").length>0;
		if (anySelected == false)
			divHeight -= 4;

	}

	moveToggleImg(options_link, divHeight);
}

function toggleVisibility(element) {
	var divHeight;
    if(element.style.display == 'block') {
    	divHeight = 0 - element.clientHeight;
    	element.style.display = 'none';
    } else { 
    	element.style.display = 'block';
    	divHeight = element.clientHeight;
    }
    return divHeight;
}

function moveToggleImg(element, height) {
	var curTop = (element.style.top == "" ? 0 : parseInt(element.style.top));
	element.style.top = curTop + height;   
}

function changeImg(img, loc) {
	img.src = loc;
}

var currentCmd = 0;
function keyPressed() {
	document.querySelector("#command_field").onkeyup=function(e) {
		if (!e) e = window.event;
		var keyCode = e.which || e.keyCode;
		if (keyCode == 38 || keyCode == 40 || keyCode == 13 || keyCode == 27) {
			var cmdHistoryList = document.querySelectorAll(".command");
			switch (keyCode) {
			case 13:
				// press enter 

				if (getCommand().toLowerCase()=="clear") {
					clearHistory();
				} else {
					currentCmd = cmdHistoryList.length + 1;
					document.querySelector("#submit").click();
				}
				break;
				
			case 27:
				//press escape
				currentCmd = cmdHistoryList.length;
				document.querySelector("#command_field").value = "";
				break;
	
			case 38:
				// press arrow up	
				if (currentCmd != 0)
					currentCmd -= 1;
				if (cmdHistoryList.length != 0) 
					document.querySelector("#command_field").value = cmdHistoryList[currentCmd].innerHTML;
				break;
			case 40:
				// press arrow down
				var cmdText = "";
				currentCmd = (currentCmd == cmdHistoryList.length) ? currentCmd : currentCmd + 1;
				if (currentCmd < cmdHistoryList.length) 
					cmdText = cmdHistoryList[currentCmd].innerHTML;
				
				document.querySelector("#command_field").value = cmdText;
				break;
			}
		}
	}
}

function saveToFile() {
	var commands = document.querySelectorAll(".command");
	var responses = document.querySelectorAll(".response");
	var textToWrite = "";
	for (var i = 0; i < commands.length; i++) {
		textToWrite += "> " + commands[i].innerHTML + "\r\n";
		textToWrite += prettyResponse(responses[i].firstChild.innerHTML);
	}
	
    var ie = navigator.userAgent.match(/MSIE\s([\d.]+)/);
    var ie11 = navigator.userAgent.match(/Trident\/7.0/) && navigator.userAgent.match(/rv:11/);
    var ieVer=(ie ? ie[1] : (ie11 ? 11 : -1));
    
//    if (ie && ieVer<10) {
//        console.log("No blobs on IE ver<10");
//        return;
//    }

	var textFileAsBlob = new Blob([textToWrite], {type:'text/plain'});
	var fileName = "AAFcommands.log";
	
	if (ieVer >= 10) {
//		window.navigator.msSaveBlob(textFileAsBlob, fileName);
		window.navigator.msSaveOrOpenBlob(textFileAsBlob, fileName); 
	} else {
		var downloadLink = document.createElement("a");
		downloadLink.download = fileName;
		downloadLink.innerHTML = "Download File";
		if (window.webkitURL != null) {
			// Chrome allows the link to be clicked
			// without actually adding it to the DOM.
			downloadLink.href = window.webkitURL.createObjectURL(textFileAsBlob);
		} else {
			// Firefox requires the link to be added to the DOM
			// before it can be clicked.
			downloadLink.href = window.URL.createObjectURL(textFileAsBlob);
			downloadLink.onclick = destroyClickedElement;
			downloadLink.style.display = "none";
			document.body.appendChild(downloadLink);
		}
	
		downloadLink.click();
	}
}

function prettyResponse(response) {
	var lines = response.split('\n');
	var cleanResponse = "";
	for (var i=0; i < lines.length; i++) {
		cleanResponse += lines[i] + "\r\n";
	}
	cleanResponse = cleanResponse.replace(/(&lt;)/g,"<").replace(/(&gt;)/g,">");
	return cleanResponse;
}

function destroyClickedElement(event){
	document.body.removeChild(event.target);
}

function fakePlaceholder() {
	document.querySelector("#command_field").setAttribute("value", "Type your AAFCLI commands here");
}

function maximizeConsole(img) {
	var footer = document.querySelector("#footer");
	var console_area = document.querySelector("#console_area");
	var content = document.querySelector("#content");
	var input_area = document.querySelector("#input_area");
	var help_msg = document.querySelector("#help_msg");
	var console_space = document.documentElement.clientHeight;
	console_space -= input_area.outerHeight;
	console_space -= help_msg.outerHeight;
    var height = getStyle(console_area,'paddingTop') + getStyle(console_area,'paddingBottom');
	console_space -= height;
	
	
	if (content.getAttribute("class") != "maximized") {
		content.setAttribute("class", "maximized");
		footer.style.display="none";
		console_area.style.resize="none";
		console_area.style.height=console_space.toString()+"px";
	} else {
		content.removeAttribute("class");
		footer.style.display="";
		console_area.style.resize="vertical";
		console_area.style.height="600px";
	}
	selectOption(img,0);
}
