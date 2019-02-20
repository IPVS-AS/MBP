/* global app */

app.controller('ActuatorDetailsController',
    ['$scope', '$controller', 'actuatorDetails',
        function ($scope, $controller, actuatorDetails) {

            var vm = this;

            angular.extend(vm, $controller('ComponentDetailsController as componentDetailsCtrl',
                    {
                        $scope: $scope,
                        componentDetails: actuatorDetails
                    })
            );
        }]
);