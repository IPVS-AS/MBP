/**
 * Controller for the rules list page.
 */
app.controller('RegisterController', ['$scope', '$location', 'UserService', 'NotificationService',
    function ($scope, $location, UserService, NotificationService) {
        let vm = this;

        function registerUser() {
            vm.dataLoading = true;
            UserService.createUser(vm.user).then(function (response) {
                NotificationService.showSuccess("Registration was successful!");

                //Redirect
                $location.path('/login');
            }, function (response) {

            }).then(function () {
                vm.dataLoading = false;
                $scope.$apply();
            });
        }

        //Expose functions
        angular.extend(vm, {
            register: registerUser
        });
    }]
);