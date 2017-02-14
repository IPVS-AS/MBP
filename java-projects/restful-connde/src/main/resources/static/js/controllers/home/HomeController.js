/* global app */

app.controller('HomeController',
        ['$scope', '$timeout', 'CrudService', 'ComponentService', 'DeviceService', 'countActuators', 'countSensors', 'countDevices', 'countTypes',
            function ($scope, $timeout, CrudService, ComponentService, DeviceService, countActuators, countSensors, countDevices, countTypes) {
                console.log('CONTORLLER HOMEMOEMEMEMEMEME');
                
                var vm = this;
                vm.loader = {};

                vm.formatMacAddress = DeviceService.formatMacAddress;

                // addresses
                var loadAddresses = function () {
                    vm.loader.addresses = true;
                    $timeout(
                            function () {
                                CrudService.getPage('addresses').then(
                                        function (data) {
                                            vm.loader.addresses = false;
                                            vm.addresses = {
                                                data: data._embedded.addresses
                                            };
                                        },
                                        function (response) {
                                            vm.loader.addresses = false;
                                            vm.addresses = {
                                                error: 'Could not load addresses',
                                                response: response
                                            };
                                        }
                                );
                            }, 1000);
                };

                // actuator values
                var loadActuatorValues = function (tableState) {
                    vm.loader.actuatorValues = true;
                    
                    var pagination = tableState.pagination || {};

                    var start = pagination.start || 0; // This is NOT the page number, but the index of item in the list that you want to use to display the table.
                    var size = pagination.number || 10; // Number of entries showed per page.
                    
                    $timeout(
                            function () {
                                var query = 'findAllByComponent';
                                var params = {
                                    component: ComponentService.COMPONENT.ACTUATOR,
                                    sort: 'date,desc',
                                    size: size,
                                    page: start
                                };

                                CrudService.searchPage('valueLogs', query, params).then(
                                        function (data) {
                                            vm.loader.actuatorValues = false;
                                            
                                            tableState.pagination.numberOfPages = data.page.totalPages; //set the number of pages so the pagination can update
                                            
                                            vm.actuatorValues = {
                                                data: data._embedded.valueLogs
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
                var loadSensorValues = function (tableState) {
                    vm.loader.sensorValues = true;
                    
                    var pagination = tableState.pagination || {};

                    var start = pagination.start || 0; // This is NOT the page number, but the index of item in the list that you want to use to display the table.
                    var size = pagination.number || 10; // Number of entries showed per page.

                    //console.log(start);

                    $timeout(
                            function () {
                                var query = 'findAllByComponent';
                                var params = {
                                    component: ComponentService.COMPONENT.SENSOR,
                                    sort: 'date,desc',
                                    size: size,
                                    page: Math.floor(start/size)
                                };

                                CrudService.searchPage('valueLogs', query, params).then(
                                        function (data) {
                                            console.log(data);
                                            vm.loader.sensorValues = false;
                                            
                                            tableState.pagination.numberOfPages = data.page.totalPages; //set the number of pages so the pagination can update
                                            
                                            vm.sensorValues = {
                                                data: data._embedded.valueLogs
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
                
                vm.loadActuatorValues = loadActuatorValues;
                vm.loadSensorValues = loadSensorValues;
                vm.loadAddresses = loadAddresses;

                vm.reloadValues = function () {
                    $scope.$broadcast('refreshActuatorValues');
                    $scope.$broadcast('refreshSensorValues');
                    $scope.$broadcast('refreshAddresses');
                    //loadActuatorValues();
                    //loadSensorValues();
                    //loadAddresses();

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

