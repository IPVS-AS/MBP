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