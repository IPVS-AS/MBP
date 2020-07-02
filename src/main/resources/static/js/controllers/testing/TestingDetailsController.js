/* global app */

/**
 * Controller for the actuator details page that extends the ComponentDetailsController.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails',
        function ($scope, $controller, testingDetails) {

            var vm = this;

            //Extend the controller for the ComponentDetailsController and pass all relevant data
            angular.extend(vm, $controller('ComponentDetailsController as componentDetailsCtrl',
                {
                    $scope: $scope,
                    componentDetails: testingDetails
                })
            );
        }]
);