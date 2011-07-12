function stackTrace(e) {
	var callstack = [];
	var isCallstackPopulated = false;
	if (e.stack) { //Firefox
		var lines = e.stack.split("\n");
		//console.log("Stk:"+lines);
		for (var i=0, len=lines.length; i<len; i++) {
                       //debug(lines[i]);
			//if (lines[i].match(/^\s*[A-Za-z0-9\-_\$]+\(/)) {
				callstack.push(lines[i]);
		//	}
		}
		//Remove call to printStackTrace()
		//callstack.shift();
		isCallstackPopulated = true;
	}
	else if (window.opera && e.message) { //Opera
		var lines = e.message.split("\n");
		//console.log("Stkw:"+lines);
		for (var i=0, len=lines.length; i<len; i++) {
			if (lines[i].match(/^\s*[A-Za-z0-9\-_\$]+\(/)) {
				var entry = lines[i];
				//Append next line also since it has the file info
				if (lines[i+1]) {
					entry += " at ‘"+ lines[i+1];
					i++;
				}
				callstack.push(entry);
			}
		}
		//Remove call to printStackTrace()
		callstack.shift();
		isCallstackPopulated = true;
	}
	if (!isCallstackPopulated) { //IE and Safari
		var currentFunction = arguments.callee.caller;
		while (currentFunction) {
			var fn = currentFunction.toString();
			var fname = fn.substring(fn.indexOf("function") + 8, fn.indexOf("")) ||
			"anonymous";
			callstack.push(fname);
			currentFunction = currentFunction.caller;
		}
	}
	return callstack;
}