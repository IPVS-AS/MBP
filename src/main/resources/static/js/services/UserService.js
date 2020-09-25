(function () {
    'use strict';

    angular
        .module('app')
        .factory('UserService', UserService);

    UserService.$inject = ['$rootScope', '$http', 'ENDPOINT_URI', 'BASE_URI'];

    function UserService($rootScope, $http, ENDPOINT_URI, BASE_URI) {
        var service = {};

        service.Authenticate = Authenticate;
        service.Logout = Logout;
        service.GetAll = GetAll;
        service.GetByUsername = GetByUsername;
        service.Create = Create;
        service.Update = Update;
        service.Delete = Delete;
        service.getUserAttributes = getUserAttributes;

        return service;

        /**
         * [Public]
         */
        function getUserAttributes() {
            return "firstName=" + $rootScope.globals.currentUser.userData.firstName + ";;"
                + "lastName=" + $rootScope.globals.currentUser.userData.lastName + ";;"
                + "username=" + $rootScope.globals.currentUser.username;
        }

        function Authenticate(user) {
            return $http.post(ENDPOINT_URI + '/authenticate', user).then(handleSuccess, handleError);
        }

        function Logout() {
            return $http.get(BASE_URI + 'logout').then(handleSuccess, handleError);
        }

        function GetAll() {
            return $http.get(ENDPOINT_URI + '/users').then(handleSuccess, handleError);
        }

        function GetByUsername(username) {
            return $http.get(ENDPOINT_URI + '/users/' + username).then(handleSuccess, handleError);
        }

        function Create(user) {
            return $http.post(ENDPOINT_URI + '/users', user).then(handleSuccess, handleError);
        }

        function Update(user) {
            return $http.put(ENDPOINT_URI + '/users/', user).then(handleSuccess, handleError);
        }

        function Delete(username) {
            return $http.delete(ENDPOINT_URI + '/users/' + username).then(handleSuccess, handleError);
        }

        function handleSuccess(res) {
            return {
                success: true,
                message: res.headers('X-MBP-alert'),
                data: res.data
            };
        }

        function handleError(res) {
            return {
                success: false,
                message: res.headers('X-MBP-error'),
                status: res.status
            };
        }
    }

})();