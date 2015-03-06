var joinSessionApp = angular.module('joinSessionApp',  ['websocketmodule']);

joinSessionApp.controller('JoinSessionController',['$scope','websocketService', function($scope, websocketService) {
  $scope.sectors = [
       ];
 
  websocketService.subscribe("sectors",function(event) {
    	$scope.sectors = event.sectors;
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
	
	websocketService.subscribe("chatline",function(event) {
    $scope.chat.push(event);
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
	