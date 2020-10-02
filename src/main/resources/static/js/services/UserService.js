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
            return "requesting-entity-firstname=" + $rootScope.globals.currentUser.userData.firstName + ";;"
                + "requesting-entity-lastname=" + $rootScope.globals.currentUser.userData.lastName + ";;"
                + "requesting-entity-username=" + $rootScope.globals.currentUser.username;
        }

        function Authenticate(user) {
            return $http.post(ENDPOINT_URI + '/users/authenticate', user);
        }

        function Logout() {
            return $http.get(BASE_URI + 'logout');
        }

        function GetAll() {
            return $http.get(ENDPOINT_URI + '/users');
        }

        function GetByUsername(username) {
            return $http.get(ENDPOINT_URI + '/users/' + username);
        }

        function Create(user) {
            return $http.post(ENDPOINT_URI + '/users', user);
        }

        function Update(user) {
            return $http.put(ENDPOINT_URI + '/users/', user);
        }

        function Delete(username) {
            return $http.delete(ENDPOINT_URI + '/users/' + username);
        }
    }

})();