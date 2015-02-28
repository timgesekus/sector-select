var exerciseSelectApp = angular.module('exerciseSelectApp',  ['websocketmodule']);

exerciseSelectApp.controller('exerciseSelectController',['$scope','websocketService', function($scope, websocketService) {
  $scope.sectors = [
       ];
 
  websocketService.subscribe("exerciseViewModel",function(event) {
    	$scope.heading = event.heading;
    	$scope.breadcrumb = event.breadcrumb;
    	$scope.selectableItems = event.selectableItems;
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
	
	websocketService.connect(jsRoutes.controllers.ExerciseSelection.groups().webSocketURL());
}]);