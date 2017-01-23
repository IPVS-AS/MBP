/* global app */

app.controller('HomeController',
        ['countActuators', 'countSensors', 'countDevices', 'countTypes',
            function (countActuators, countSensors, countDevices, countTypes) {
                var vm = this;

                // expose
                angular.extend(vm, {
                    countActuators: countActuators,
                    countSensors: countSensors,
                    countDevices: countDevices,
                    countTypes: countTypes
                });
            }]);

