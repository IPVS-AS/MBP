/* global app */

app.controller('SettingsController',
    ['$scope', '$http', '$q', 'ENDPOINT_URI',
        function ($scope, $http, $q, ENDPOINT_URI) {
            var vm = this;

            var url = ENDPOINT_URI + "/settings";

            vm.settings = {
                brokerLocation: "",
                brokerUrl: ""
            };


            // public
            function saveSettings() {
                alert("howdy");
                console.log(vm.broker_location);
                console.log(vm.broker_url);
            }

            //public
            function getSettings() {
                return $http({
                    method: 'GET',
                    url: url
                }).then(
                    function (response) {
                        if (response.data !== undefined) {
                            vm.settings = response.data;
                        } else {
                            console.log('Invalid response');
                            console.log(response);
                            return $q.reject(response);
                        }
                    },
                    function (response) {
                        console.log('Error');
                        return $q.reject(response);
                    });
            }

            angular.extend(vm, {
                saveSettings: saveSettings
            });

            getSettings();
        }]);

