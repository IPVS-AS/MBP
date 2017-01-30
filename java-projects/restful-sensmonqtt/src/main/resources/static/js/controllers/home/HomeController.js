/* global app */

app.controller('HomeController',
        ['$timeout', 'ComponentService', 'countActuators', 'countSensors', 'countDevices', 'countTypes',
            function ($timeout, ComponentService, countActuators, countSensors, countDevices, countTypes) {
                var vm = this;
                vm.loader = {};

                // actuator values
                var loadActuatorValues = function () {

                    vm.loader.actuatorValues = true;
                    $timeout(
                            function () {
                                ComponentService.getValues(ComponentService.COMPONENT.ACTUATOR, undefined).then(
                                        function (data) {
                                            vm.loader.actuatorValues = false;
                                            vm.actuatorValues = {
                                                data: data
                                            };
                                        },
                                        function (response) {
                                            vm.loader.actuatorValues = false;
                                            vm.actuatorValues = {
                                                error: 'Could not load values',
                                                response: response
                                            };
                                        }
                                );
                            }, 500);
                };

                // sensor values
                var loadSensorValues = function () {

                    vm.loader.sensorValues = true;
                    $timeout(
                            function () {
                                ComponentService.getValues(ComponentService.COMPONENT.SENSOR, undefined).then(
                                        function (data) {
                                            vm.loader.sensorValues = false;
                                            vm.sensorValues = {
                                                data: data
                                            };
                                        },
                                        function (response) {
                                            vm.loader.sensorValues = false;
                                            vm.sensorValues = {
                                                error: 'Could not load values',
                                                response: response
                                            };
                                        }
                                );
                            }, 500);
                };

                vm.reloadValues = function () {
                    loadActuatorValues();
                    loadSensorValues();

                    $timeout(function () {
                        vm.reloadValues();
                    }, 10000);
                };
                vm.reloadValues();

                // expose
                angular.extend(vm, {
                    countActuators: countActuators,
                    countSensors: countSensors,
                    countDevices: countDevices,
                    countTypes: countTypes
                });
            }]);

