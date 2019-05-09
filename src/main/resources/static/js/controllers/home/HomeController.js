/* global app */

app.controller('HomeController',
        ['$scope', '$timeout', 'CrudService', 'ComponentService', 'DeviceService', 'countActuators', 'countSensors', 'countDevices', 'countAdapters', 'countMonitoringAdapters',
            function ($scope, $timeout, CrudService, ComponentService, DeviceService, countActuators, countSensors, countDevices, countAdapters, countMonitoringAdapters) {
                var vm = this;
                vm.loader = {};

                // expose
                angular.extend(vm, {
                    countActuators: countActuators,
                    countSensors: countSensors,
                    countDevices: countDevices,
                    countAdapters: countAdapters,
                    countMonitoringAdapters: countMonitoringAdapters
                });
            }]);

