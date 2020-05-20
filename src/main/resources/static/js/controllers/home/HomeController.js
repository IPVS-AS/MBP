/* global app */

app.controller('HomeController',
    ['$scope', '$timeout', 'CrudService', 'ComponentService', 'DeviceService', 'countEnvModels',
        'countActuators', 'countSensors', 'countDevices', 'countAdapters', 'countMonitoringAdapters', 'countTests',
        function ($scope, $timeout, CrudService, ComponentService, DeviceService, countEnvModels,
                  countActuators, countSensors, countDevices, countAdapters, countMonitoringAdapters, countTests) {

            let vm = this;
            vm.loader = {};

            //Expose
            angular.extend(vm, {
                countEnvModels: countEnvModels,
                countActuators: countActuators,
                countSensors: countSensors,
                countDevices: countDevices,
                countAdapters: countAdapters,
                countMonitoringAdapters: countMonitoringAdapters,
                countTests: countTests
            });
        }]);

