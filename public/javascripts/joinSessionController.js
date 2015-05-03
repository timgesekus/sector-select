var joinSessionApp = angular.module('joinSessionApp',  ['ngWebSocket']);

joinSessionApp.controller('JoinSessionController',['$scope', function($scope) {
  $scope.sectors = [];
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
	