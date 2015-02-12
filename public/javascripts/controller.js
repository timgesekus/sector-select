var sectorSelectApp = angular.module('sectorSelectApp', []);

sectorSelectApp.factory('SectorListService', function() {
  var service = {};
 
  service.connect = function() {
    if(service.ws) { return; }
    var websocketUrl = jsRoutes.controllers.SectorList.sectors("Tim Gesekus").webSocketURL();
    var ws = new WebSocket(websocketUrl);
 
    ws.onopen = function() {
    	console.log("Connected");
    	// service.callback("Succeeded to open a connection");
    };
 
    ws.onerror = function() {
      // service.callback("Failed to open a connection");
    }
 
    ws.onmessage = function(message) {
    	console.log("Got message:" + message.data);
    	service.callback(JSON.parse(message.data));
    };
 
    service.ws = ws;
  }
 
  service.send = function(message) {
    service.ws.send(JSON.stringify(message));
  }
 
  service.subscribe = function(callback) {
    service.callback = callback;
  }
 
  return service;
});

sectorSelectApp.controller('SectorSelectController', function($scope, SectorListService) {
  $scope.sectors = [
       ];
 
  SectorListService.subscribe(function(event) {
    	$scope.sectors = event.sectors;
      $scope.$apply();  	
 });

	$scope.select = function(sectorName) {
		
		console.log("event:" +  sectorName);
		var selectEvent = {
				eventName : "select",
				sector :sectorName
		};
		
		SectorListService.send(selectEvent)
	};
	
	$scope.delete = function ( idx ) {
		var item = $scope.items[idx]
  	var event = {
				eventName : "Remove",
				"item" : item
		};
		SectorListService.send(event)
	};
	
	SectorListService.connect();
});