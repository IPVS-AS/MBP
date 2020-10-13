app.factory('AuthenticationService', ['HttpService', '$cookieStore', '$rootScope', '$timeout', 'UserService',
        function (HttpService, $cookieStore, $rootScope, $timeout, UserService) {

            function encodeBase64(rawString) {
                let wordArray = CryptoJS.enc.Utf8.parse(rawString);
                return CryptoJS.enc.Base64.stringify(wordArray);
            }

            function login(username, password, callback) {
                let user = {
                    username: username,
                    password: password
                };

                return UserService.Authenticate(user);
            }

            function logout() {
                UserService.Logout().then(function (response) {
                }, function (response) {
                    console.log("Log out error!");
                });
            }

            /*
             * Set the authorization header and save the data in a cookie
             */
            function setCredentials(username, password, userData) {
                //Encrypt username and password via Base64 to auth data
                let authData = encodeBase64(username + ':' + password);

                //Sanitize user data
                userData = userData || {};

                $rootScope.globals = {
                    currentUser: {
                        username: username,
                        userData: userData,
                        authdata: authData
                    }
                };

                // store user details in globals cookie that keeps user logged in for 1 week (or until they logout)
                let cookieExp = new Date();
                cookieExp.setDate(cookieExp.getDate() + 7);
                $cookieStore.put('globals', $rootScope.globals, {
                    expires: cookieExp
                });
            }

            /*
             * Remove the header and delete the cookie
             */
            function clearCredentials() {
                $rootScope.globals = {};
                $cookieStore.remove('globals');
            }

            //Expose
            return {
                Login: login,
                Logout: logout,
                SetCredentials: setCredentials,
                ClearCredentials: clearCredentials
            };
        }
    ]
);
