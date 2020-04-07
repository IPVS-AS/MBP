/* global app */

app.controller('HomeController',
    ['$scope', '$timeout', 'CrudService', 'ComponentService', 'DeviceService', 'countEnvModels', 'countActuators', 'countSensors', 'countDevices', 'countAdapters', 'countMonitoringAdapters',
        function ($scope, $timeout, CrudService, ComponentService, DeviceService, countEnvModels, countActuators, countSensors, countDevices, countAdapters, countMonitoringAdapters) {
            var vm = this;
            vm.loader = {};

            // expose
            angular.extend(vm, {
                countEnvModels: countEnvModels,
                countActuators: countActuators,
                countSensors: countSensors,
                countDevices: countDevices,
                countAdapters: countAdapters,
                countMonitoringAdapters: countMonitoringAdapters
            });
        }]);

