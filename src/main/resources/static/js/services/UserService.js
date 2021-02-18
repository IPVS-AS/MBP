/**
 * Provides services for managing users.
 */
app.factory('UserService', ['$rootScope', 'HttpService', 'ENDPOINT_URI', 'BASE_URI',
    function ($rootScope, HttpService, ENDPOINT_URI, BASE_URI) {

        const URL_PROMOTE_SUFFIX = '/promote';
        const URL_DEGRADE_SUFFIX = '/degrade';
        const URL_CHANGE_PASSWORD_SUFFIX = '/change_password';

        function authenticate(user) {
            return HttpService.postRequest(ENDPOINT_URI + '/users/authenticate', user);
        }

        function logout() {
            return HttpService.getRequest(BASE_URI + 'logout');
        }

        function getAll() {
            return HttpService.getRequest(ENDPOINT_URI + '/users');
        }

        function getByUsername(username) {
            return HttpService.getRequest(ENDPOINT_URI + '/users/' + username);
        }

        function createUser(user) {
            return HttpService.postRequest(ENDPOINT_URI + '/users', user);
        }

        function updateUser(user) {
            return HttpService.putRequest(ENDPOINT_URI + '/users/', user);
        }

        function deleteUser(username) {
            return HttpService.deleteRequest(ENDPOINT_URI + '/users/' + username);
        }

        function promoteUser(userId) {
            return HttpService.postRequest(ENDPOINT_URI + '/users/' + userId + URL_PROMOTE_SUFFIX);
        }

        function degradeUser(userId) {
            return HttpService.postRequest(ENDPOINT_URI + '/users/' + userId + URL_DEGRADE_SUFFIX);
        }

        function changeUserPassword(userId, newPassword) {
            return HttpService.postRequest(ENDPOINT_URI + '/users/' + userId + URL_CHANGE_PASSWORD_SUFFIX, {'password': newPassword});
        }

        //Expose
        return {
            Authenticate: authenticate,
            Logout: logout,
            GetAll: getAll,
            GetByUsername: getByUsername,
            Create: createUser,
            Update: updateUser,
            Delete: deleteUser,
            promoteUser: promoteUser,
            degradeUser: degradeUser,
            changeUserPassword: changeUserPassword
        };
    }]);
