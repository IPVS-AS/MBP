/* global app */

/**
 * Controller for the Models list page
 */
app.controller('ModelsListController',
    ['$scope','sensorList', 'AnalyticsService',
        function ($scope, sensorList, AnalyticsService) {   

            AnalyticsService.getExistingModels().then(function (response) {
                            $scope.existingmodels = response.data;
                            console.log($scope.existingmodels);
                    });

            $scope.modelType = AnalyticsService.getModelTypes();

            //var batchAlgo = AnalyticsService.getBatchAlgorithms();

            //var streamAlgo = AnalyticsService.getStreamAlgorithms();

            $scope.algorithms = [];

            $scope.sensors = sensorList;

            $scope.showTimeTextbox = false;

            $scope.onChange = function(){
                $scope.showTimeTextbox = $scope.modelselected.name;
                if ($scope.modelselected.name === 'Stream Mining') {
                       AnalyticsService.getStreamAlgorithms().then(function (response) {
                            $scope.algorithms = response.data;
                    });
                       $scope.showTimeTextbox = true;
                  } else {
                    AnalyticsService.getBatchAlgorithms().then(function (response) {
                            $scope.algorithms = response.data;
                    });
                    $scope.showTimeTextbox = false;
                  }
            }

            $scope.addModel = function(){
                if ($scope.modelselected.name === 'Batch Processing') {
                    AnalyticsService.createBatchModel($scope.modelname, $scope.algorithmsselected.name, $scope.sensorselected.id);   
                  } else if ($scope.modelselected.name === 'Stream Mining'){
                    AnalyticsService.createStreamModel($scope.modelname, $scope.algorithmsselected.name, $scope.sensorselected.id, $scope.timeInDays);
                  }                
            }
            $scope.getStatistics = function(name){
                console.log(name);
                AnalyticsService.getModelStatistics(name).then(function (response) {
                            $scope.statistics=response.data;
                            console.log($scope.statistics);
                    });          
            }            

        }
    ]);