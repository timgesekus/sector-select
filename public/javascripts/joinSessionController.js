var joinSessionApp = angular.module('joinSessionApp',  ['ngWebSocket']);

joinSessionApp.controller('joinSessionController',['$scope', '$websocket', function($scope,$websocket) {
	$scope.workspaces = [];
	$scope.input = "" ;
	var workspacesId = $("#workspacesId").data("workspacesid");
	var socket = $websocket(jsRoutes.controllers.Workspaces.workspacesWS(workspacesId).webSocketURL());
	socket.onMessage(function(event) {
		var res = 	JSON.parse(event.data);
		console.log("message received " + res.topic);
		console.log("message received " + res.assignements);
	    
		$scope.workspaces = res.assignements;
	   	$scope.$apply();
		});
		
}]);

joinSessionApp.controller('chatController',['$scope', '$websocket',function($scope, $websocket) {
	$scope.chat = [];
	$scope.input = "" ;
	var chatId = $("#chatId").data("chatid");
	var socket = $websocket(jsRoutes.controllers.Chat.chatWS(chatId).webSocketURL());
  
	
	socket.onMessage(function(event) {
	var res = 	JSON.parse(event.data);
	console.log("message received " + res.topic);
	console.log("message received " + res.messages);
    
	$scope.chat = res.messages;
   	$scope.$apply();
	});
	
	$scope.submit = function () {
		console.log("New message: " + $scope.input);
		var chatEvent = {
				topic : "chatMessage",
				message : $scope.input
		}
		socket.send(chatEvent)
		$scope.input = "";
	}
}]);
	