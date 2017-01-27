/* global app */

app.controller('HomeController',
        ['countActuators', 'countSensors', 'countDevices', 'countTypes', 'actuatorValues', 'sensorValues',
            function (countActuators, countSensors, countDevices, countTypes, actuatorValues, sensorValues) {
                var vm = this;

                // expose
                angular.extend(vm, {
                    countActuators: countActuators,
                    countSensors: countSensors,
                    countDevices: countDevices,
                    countTypes: countTypes,
                    actuatorValues: actuatorValues,
                    sensorValues: sensorValues
                });
            }]);

