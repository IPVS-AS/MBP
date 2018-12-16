/* global app */

/*
 * Controller for the settings page.
 */
app.controller('SettingsController',
    ['$scope', '$http', '$q', 'ENDPOINT_URI',
        function ($scope, $http, $q, ENDPOINT_URI) {
            var vm = this;

            //Full URL for REST requests
            var url = ENDPOINT_URI + "/settings";

            //Settings objects that contains all application settings for this page
            vm.settings = {
                brokerLocation: null,
                brokerIPAddress: ""
            };

            //Remembers the server responses and controls whether success/error messages are displayed
            vm.response = {
                success: false,
                error: false
            };

            /**
             * Creates a REST request in order to save the settings on the server.
             *
             * @returns The created REST request
             */
            function saveSettings() {
                //Nothing to display so far
                vm.response.success = false;
                vm.response.error = false;

                //Create REST request with the settings object as payload
                return $http({
                    data: vm.settings,
                    method: 'POST',
                    url: url
                }).then(
                    //Success callback
                    function (response) {
                        vm.response.success = true;
                        vm.response.error = false;
                    },
                    //Error callback
                    function (response) {
                        vm.response.success = false;
                        vm.response.error = true;
                        return $q.reject(response);
                    });
            }

            /**
             * Creates a REST request in order to retrieve the current settings from the server.
             *
             * @returns The created REST request
             */
            function getSettings() {
                //Create REST request without payload
                return $http({
                    method: 'GET',
                    url: url
                }).then(
                    //Success callback
                    function (response) {
                        //Check for valid response payload
                        if (response.data !== undefined) {
                            vm.settings = response.data;
                        } else {
                            return $q.reject(response);
                        }
                    },
                    //Error callback
                    function (response) {
                        return $q.reject(response);
                    });
            }

            //Expose functions that are triggered externally
            angular.extend(vm, {
                saveSettings: saveSettings
            });

            //Retrieve settings when loading the page
            getSettings();
        }]);
