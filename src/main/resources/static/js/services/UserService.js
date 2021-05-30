/**
 * Provides services for managing users.
 */
app.factory('UserService', ['$rootScope', '$location', '$cookieStore', 'HttpService', 'ENDPOINT_URI',
    function ($rootScope, $location, $cookieStore, HttpService, ENDPOINT_URI) {

        const URL_LOGIN_SUFFIX = '/users/login';
        const URL_PROMOTE_SUFFIX = '/promote';
        const URL_DEGRADE_SUFFIX = '/degrade';
        const URL_CHANGE_PASSWORD_SUFFIX = '/change_password';

        function loginUser(username, password) {
            return HttpService.postRequest(ENDPOINT_URI + URL_LOGIN_SUFFIX, {
                "username": username,
                "password": password
            }).then(function (userData) {
                //Sanitize user data
                userData = userData || [];

                //Enable authorization locally
                setUserData(username, userData);

                //Redirect
                $location.path('/');
            });
        }

        function logoutUser() {
            //Clear user data
            clearUserData();
        }

        function getAllUsers() {
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
            return HttpService.postRequest(ENDPOINT_URI + '/users/' + userId + URL_CHANGE_PASSWORD_SUFFIX,
                {'password': newPassword});
        }

        /**
         * [Private]
         * Takes data about the current user and stores it in the root scope for easy access. In addition,
         * a cookie is created to remember to user data across switching pages.
         *
         * @param username The username of the current user
         * @param userData Additional data about the current user
         */
        function setUserData(username, userData) {
            //Sanitize user data
            userData = userData || {};

            //Store user data in root scope for easy access
            $rootScope.globals = {
                currentUser: {
                    username: username,
                    userData: userData
                }
            };

            // Store user details in globals cookie that keeps user logged in for one week
            let cookieExp = new Date();
            cookieExp.setDate(cookieExp.getDate() + 7);
            $cookieStore.put('globals', $rootScope.globals, {
                expires: cookieExp
            });
        }

        /**
         * [Private]
         * Clears the data about the current user from the root scope and also removes to corresponding cookie.
         */
        function clearUserData() {
            $rootScope.globals = {};
            $cookieStore.remove('globals');
        }

        //Expose
        return {
            loginUser: loginUser,
            logoutUser: logoutUser,
            getAllUsers: getAllUsers,
            getByUsername: getByUsername,
            createUser: createUser,
            updateUser: updateUser,
            deleteUser: deleteUser,
            promoteUser: promoteUser,
            degradeUser: degradeUser,
            changeUserPassword: changeUserPassword
        };
    }]);
