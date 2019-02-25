/* global app */

/**
 * Controller for the sensor details page that implements the ComponentDetailsController.
 */
app.controller('SensorDetailsController',
    ['$scope', '$controller', 'sensorDetails',
        function ($scope, $controller, sensorDetails) {

            var vm = this;

            //Extend the controller for the ComponentDetailsController and pass all relevant data
            angular.extend(vm, $controller('ComponentDetailsController as componentDetailsCtrl',
                {
                    $scope: $scope,
                    componentDetails: sensorDetails,
                    liveChartContainer: 'liveValues',
                    historicalChartContainer: 'historicalValues'
                })
            );
        }]
);