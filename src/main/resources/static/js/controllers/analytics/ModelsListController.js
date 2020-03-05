/* global app */

/**
 * Controller for the Models list page
 */
app.controller('ModelsListController',
    ['$scope','sensorList', 'AnalyticsService',
        function ($scope, sensorList, AnalyticsService) {   

            AnalyticsService.getExistingModels().then(function (response) {
                            $scope.existingmodels = response.data;
                    });

            $scope.modelType = AnalyticsService.getModelTypes();

            $scope.algorithms = [];
            $scope.selectedAlgorithm = '';
            $scope.sensors = sensorList;

            $scope.showTimeTextbox = false;
            $scope.showPredictButton = false;
            $scope.showPredictionForm =false;
            $scope.showPredictionResult = false;

            $scope.onChange = function(){
                $scope.showTimeTextbox = $scope.modelselected.name;
                if ($scope.modelselected.name === 'Stream Mining') {
                       AnalyticsService.getStreamAlgorithms().then(function (response) {
                            $scope.algorithms = response.data;
                    });
                       $scope.showTimeTextbox = true;
                  } 
                  else {
                    AnalyticsService.getBatchAlgorithms().then(function (response) {
                            $scope.algorithms = response.data;
                    });
                    $scope.showTimeTextbox = false;
                  }
            }

            $scope.addModel = function(){

                if ($scope.modelselected.name === 'Batch Processing') {
                    AnalyticsService.createBatchModel($scope.modelname, $scope.modeldescription, $scope.algorithmsselected.name, $scope.sensorselected.id);   
                  } 
                else if ($scope.modelselected.name === 'Stream Mining'){
                    AnalyticsService.createStreamModel($scope.modelname, $scope.modeldescription, $scope.algorithmsselected.name, $scope.sensorselected.id, $scope.timeInDays);
                  }                
            }

            $scope.getStatistics = function(name, algorithm){
                $scope.selectedAlgorithm = name;
                $scope.showPredictionForm =false;
                $scope.showPredictionResult = false;
                if(algorithm === 'Regression' || algorithm === 'Classification' || algorithm === 'Stream KNN classification' || 
                    algorithm === 'Stream Hoeffding Tree Classifier' ){
                    $scope.showPredictButton = true;
                }
                else{
                    $scope.showPredictButton = false;
                }
                AnalyticsService.getModelStatistics(name).then(function (response) {
                            $scope.statistics=response.data[0];
                            console.log($scope.statistics);
                    });          
            }

            $scope.predictValue = function(){
                $scope.showPredictButton = false;
                $scope.showPredictionForm =true;                
            }

            $scope.getPredictedValue = function(){
                $scope.prediction = '';
                AnalyticsService.getPrediction($scope.selectedAlgorithm, $scope.valuetopredict).then(function (response) {
                            $scope.prediction=response.data[0];
                            console.log($scope.prediction);
                    });   
                $scope.showPredictionResult = true;
            }   

            $scope.deleteModel = function(name){
  
                return Swal.fire({
                        title: 'Delete Model',
                        type: 'warning',
                        html: "Are you sure you want to delete Model: \"" + name + "\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    }).then((result) => {
                          if (result.value) {
                            AnalyticsService.deletemodel(name);
                            AnalyticsService.getExistingModels().then(function (response) {
                            $scope.existingmodels = response.data;
                            });
                            Swal.fire(
                              'Deleted!',
                              'Model has been deleted.',
                              'success'
                            )
                          }
                        });
            }         

        }
    ]);