function sensorIdCtrl($scope, $timeout, Device, Sensor) {
    var vm = this;
    // init
    vm.sensor = {};
    // Load Sensor
    Sensor.getSensor().then(function (data) {
        $timeout(function () {
            vm.sensor = data;
        });
    }, function (response) {
        alert("Couldn't retrieve Sensor.");
    });
}

angular.module('app')
        .controller('sensorIdCtrl', sensorIdCtrl);



