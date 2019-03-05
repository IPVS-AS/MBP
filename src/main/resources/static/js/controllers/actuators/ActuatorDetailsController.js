/* global app */

/**
 * Controller for the actuator details page that extends the ComponentDetailsController.
 */
app.controller('ActuatorDetailsController',
    ['$scope', '$controller', 'actuatorDetails',
        function ($scope, $controller, actuatorDetails) {

            var vm = this;

            //Extend the controller for the ComponentDetailsController and pass all relevant data
            angular.extend(vm, $controller('ComponentDetailsController as componentDetailsCtrl',
                {
                    $scope: $scope,
                    componentDetails: actuatorDetails,
                    liveChartContainer: 'liveValues',
                    historicalChartContainer: 'historicalValues',
                    historicalChartSlider: 'historicalChartSlider'
                })
            );
        }]
);