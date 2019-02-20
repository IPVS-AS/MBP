/* global app */

app.controller('SensorDetailsController',
    ['$scope', '$controller', 'sensorDetails',
        function ($scope, $controller, sensorDetails) {

            var vm = this;

            angular.extend(vm, $controller('ComponentDetailsController as componentDetailsCtrl',
                    {
                        $scope: $scope,
                        componentDetails: sensorDetails
                    })
            );
        }]
);