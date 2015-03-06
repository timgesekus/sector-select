var exerciseSelectApp = angular.module('exerciseSelectApp',  ['websocketmodule']);

exerciseSelectApp.controller('exerciseSelectController',['$scope','websocketService', function($scope, websocketService) {
  $scope.sectors = [
       ];
 
  
  websocketService.subscribe("redirect",function(event) {
  	window.location.href = event.url;
  });

  websocketService.subscribe("exercises",function(event) {
    	$scope.groups = event.groups;
    	$scope.$apply();
    });
	
	$scope.startExercise = function (exerciseId) {
		console.log("Start exercise:" + exerciseId);
		var startExercise = {
				topic : "startExercise",
				id : 1
		};
		websocketService.send(startExercise)

	};
	websocketService.connect(jsRoutes.controllers.ExerciseSelection.groups().webSocketURL());
}]);