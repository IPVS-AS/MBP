app.controller('LoginController', ['$scope', '$location', 'UserService',
    function ($scope, $location, UserService) {
        let vm = this;

        /**
         * Initializing function, sets up basic things.
         */
        (function initController() {
            //Reset login status
            UserService.logoutUser();

            //Hide loader
            vm.dataLoading = false;
        })();

        /**
         * [Public]
         * Performs a server request in order to login a user with the entered form data.
         */
        function login() {
            //Show loader
            vm.dataLoading = true;

            //Perform login
            UserService.loginUser(vm.username, vm.password).always(function () {
                //Hide loader
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
