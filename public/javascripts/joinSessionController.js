var joinSessionApp = angular.module('joinSessionApp',  ['websocketmodule']);

joinSessionApp.controller('JoinSessionController',['$scope','websocketService', function($scope, websocketService) {
  $scope.sectors = [
       ];
 
  websocketService.subscribe("workspaceAssignement",function(event) {
    	$scope.sectors = event.workspaceAssignements;
      $scope.$apply();  	
 });

	$scope.select = function(sectorName) {
		
		console.log("event:" +  sectorName);
		var selectEvent = {
				topic : "select",
				sector :sectorName
		};
		
		websocketService.send(selectEvent)
	};
	
	$scope.delete = function ( idx ) {
		var item = $scope.items[idx]
  	var event = {
				eventName : "Remove",
				"item" : item
		};
		websocketService.send(event)
	};
	var sessionId = $("#session").data("sessionid");
	websocketService.connect(jsRoutes.controllers.JoinSession.joinSessionWS(sessionId).webSocketURL());
}]);

joinSessionApp.controller('chatController',['$scope','websocketService', function($scope, websocketService) {
	$scope.chat = [];
	$scope.input = "" ;
	
	websocketService.subscribe("ChatViewModel",function(event) {
	console.log("message received " + event.topic);
	console.log("message received " + event.messages);
    
	$scope.chat = event.messages;
   	$scope.$apply();
	});
	
	$scope.submit = function () {
		var chatEvent = {
				topic : "chatMessage",
				message : $scope.input
		}
		websocketService.send(chatEvent)
		$scope.input = "";
	}
}]);
	