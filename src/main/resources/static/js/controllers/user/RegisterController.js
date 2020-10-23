/**
 * Controller for the rules list page.
 */
app.controller('RegisterController', ['$scope', 'UserService', '$location', 'NotificationService',
    function ($scope, UserService, $location, NotificationService) {
        let vm = this;

        function register() {
            vm.dataLoading = true;
            UserService.Create(vm.user).then(function (response) {
                NotificationService.showSuccess("Registration was successful!");

                //Redirect
                $location.path('/login');
            }, function (response) {
                //Check why the request failed
                /*
                if (response.status === 409) {
                    NotificationService.showError("The name is already in use.")
                } else {
                    NotificationService.showError("Login failed.")
                }*/
            }).then(function () {
                vm.dataLoading = false;
                $scope.$apply();
            });
        }

        //Expose functions
        angular.extend(vm, {
            register: register
        });
    }]
);