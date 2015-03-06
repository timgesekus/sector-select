
angular.module('websocketmodule', [])
	.factory('websocketService', function() {
  var service = {};
	
  service.callback = {};
  
  service.connect = function($url) {
    if(service.ws) { return; }
    var ws = new WebSocket($url);
 
    ws.onopen = function() {
    	console.log("Connected");
    	// service.callback("Succeeded to open a connection");
    };
 
    ws.onerror = function() {
      // service.callback("Failed to open a connection");
    }
 
    ws.onmessage = function(message) {
    	console.log("Got message:" + message.data);
    	var data = JSON.parse(message.data);
    	service.callback[data.topic](data);
    };
 
    service.ws = ws;
  }
 
  service.send = function(message) {
    service.ws.send(JSON.stringify(message));
  }
 
  service.subscribe = function( topic, callback) {
    service.callback[topic] = callback;
  }

  return service;
});