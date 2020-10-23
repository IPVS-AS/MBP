app.controller('LoginController', ['$scope', '$location', '$rootScope', 'AuthenticationService', 'NotificationService',
    function ($scope, $location, $rootScope, AuthenticationService, NotificationService) {
        let vm = this;

        vm.dataLoading = false;

        /**
         * Initializing function, sets up basic things.
         */
        (function initController() {
            //Reset login status
            AuthenticationService.ClearCredentials();
            AuthenticationService.Logout();
        })();

        /**
         * [Public]
         * Performs user login with the form data from the template.
         */
        function login() {
            vm.dataLoading = true;
            AuthenticationService.Login(vm.username, vm.password).then(function (userData) {
                //Sanitize user data
                userData = userData || [];

                //Enable authorization locally
                AuthenticationService.SetCredentials(vm.username, vm.password, userData);

                //Redirect
                $location.path('/');
            }, function (response) {
                /*
                //Check why the request failed
                if (response.status === 403) {
                    NotificationService.showError("Incorrect password.")
                } else if (response.status === 404) {
                    NotificationService.showError("User does not exist.")
                } else {
                    NotificationService.showError("Login failed.")
                }*/
            }).then(function () {
                vm.dataLoading = false;
                $scope.$apply();
            });
        }

        //Expose
        angular.extend(vm, {
            login: login
        });
    }]
);
