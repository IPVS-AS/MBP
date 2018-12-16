/* global app */

app.controller('SettingsController',
    ['$scope', '$http', '$q', 'ENDPOINT_URI',
        function ($scope, $http, $q, ENDPOINT_URI) {
            var vm = this;

            var url = ENDPOINT_URI + "/settings";

            vm.settings = {
                brokerLocation: null,
                brokerIPAddress: ""
            };

            vm.response = {
                success: false,
                error: false
            };

            // public
            function saveSettings() {
                vm.response.success = false;
                vm.response.error = false;

                return $http({
                    data: vm.settings,
                    method: 'POST',
                    url: url
                }).then(
                    function (response) {
                        vm.response.success = true;
                        vm.response.error = false;
                    },
                    function (response) {
                        vm.response.success = false;
                        vm.response.error = true;
                        return $q.reject(response);
                    });
            }

            function getSettings() {
                return $http({
                    method: 'GET',
                    url: url
                }).then(
                    function (response) {
                        if (response.data !== undefined) {
                            vm.settings = response.data;
                        } else {
                            return $q.reject(response);
                        }
                    },
                    function (response) {
                        return $q.reject(response);
                    });
            }

            angular.extend(vm, {
                saveSettings: saveSettings
            });

            getSettings();
        }]);
