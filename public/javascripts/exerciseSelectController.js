var exerciseSelectApp = angular.module('exerciseSelectApp',   ['ngWebSocket']);

exerciseSelectApp.controller('exerciseSelectController',['$scope', '$websocket', function($scope, $websocket) {
  $scope.exercises = [
       ];
 
	var socket = $websocket(jsRoutes.controllers.ExerciseSelection.exerciseSelectionWS().webSocketURL()); 
	socket.onMessage(function(event) {
		var res = 	JSON.parse(event.data);
		console.log("message received " + res.assignements);
	    
		$scope.exercises = res.exercises;
	   	$scope.$apply();
	});
	
	$scope.startExercise = function (exerciseId) {
		window.location.href=jsRoutes.controllers.JoinSession.createSession(exerciseId).absoluteURL();
		console.log("Start exercise:" + exerciseId);
		//var startExercise = {
		//		topic : "startExercise",
		//		id : 1
		//};
		//websocketService.send(startExercise)

	};
}]);

