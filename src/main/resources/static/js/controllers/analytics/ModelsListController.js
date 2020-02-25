/* global app */

/**
 * Controller for the Models list page
 */
app.controller('ModelsListController',
    ['$scope','sensorList', 'AnalyticsService',
        function ($scope, sensorList, AnalyticsService) {   

            $scope.existingmodels = AnalyticsService.getExistingModels();

            $scope.modelType = [{"id": 1, "name": "Stream Mining"}, 
            {"id": 2, "name": "Batch Processing"}];

            var batchAlgo = [{"id": 1, "name": "Regrssion"}, 
            {"id": 2, "name": "Classification"}];

            var streamAlgo = [{"id": 1, "name": "KNN"}, 
            {"id": 2, "name": "Stream K means"}];

            $scope.algorithms = [];

            $scope.sensors = sensorList;

            $scope.showTimeTextbox = "";

            $scope.onChange = function(){
                console.log('got change');
                console.log($scope.modelselected.name);
                $scope.showTimeTextbox = $scope.modelselected.name;
                if ($scope.modelselected.name === 'Stream Mining') {
                       $scope.algorithms = streamAlgo ;
                  } else {
                    $scope.algorithms = batchAlgo;
                  }
            }
        }
    ]);