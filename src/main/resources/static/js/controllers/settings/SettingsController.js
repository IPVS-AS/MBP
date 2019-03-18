/* global app */

/*
 * Controller for the settings page.
 */
app.controller('SettingsController',
    ['$scope', '$http', '$q', 'ENDPOINT_URI', 'NotificationService',
        function ($scope, $http, $q, ENDPOINT_URI, NotificationService) {
            var vm = this;

            //Full URL for REST requests
            var url = ENDPOINT_URI + "/settings";

            //Settings objects that contains all application settings for this page
            vm.settings = {
                brokerLocation: null,
                brokerIPAddress: ""
            };

            /**
             * Creates a REST request in order to save the settings on the server.
             *
             * @returns The created REST request
             */
            function saveSettings() {
                //Create REST request with the settings object as payload
                return $http({
                    data: vm.settings,
                    method: 'POST',
                    url: url
                }).then(
                    //Success callback
                    function (response) {
                        NotificationService.notify('The settings were saved successfully.', 'success');
                    },
                    //Error callback
                    function (response) {
                        NotificationService.notify('The settings could not be saved.', 'error');
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
