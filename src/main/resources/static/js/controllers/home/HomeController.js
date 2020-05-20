/* global app */

app.controller('HomeController',
        ['$scope', '$timeout', 'CrudService', 'ComponentService', 'DeviceService', 'countEnvModels', 'countActuators', 'countSensors', 'countDevices', 'countAdapters', 'countMonitoringAdapters', 'countTests',
            function ($scope, $timeout, CrudService, ComponentService, DeviceService, countEnvModels, countActuators, countSensors, countDevices, countAdapters, countMonitoringAdapters, countTests) {
                vm.loader = {};

                //Expose
                    countEnvModels: countEnvModels,
                    countMonitoringAdapters: countMonitoringAdapters,
                    countTests: countTests

