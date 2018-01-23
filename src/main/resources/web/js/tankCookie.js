var restport = "";
var ipaddress = "";
$(document).ready(function () {
	var hostWithProt=window.location.host;
	 
	var restport=window.location.port;
	 
	var ipaddress = hostWithProt.replace(':'+restport,"");
	
	
});


function getRestport(){
	return restport;
};

function getIpaddress(){
	return ipaddress;
};
	