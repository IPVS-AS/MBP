function sensorsCtrl($scope, $timeout, Device, Sensor) {
    var vm = this;
    // init
    vm.sensors = [];
    vm.devices = [];
    vm.sensor = {};
    vm.device = {};
    // Load Sensors
    Sensor.getSensors().then(function (data) {
        $timeout(function () {
            vm.sensors = data;
        });
    }, function (response) {
        alert("Couldn't retrieve Sensors. " + JSON.stringify(response));
    });
    // Load Devices
    Device.getDevices().then(function (data) {
        $timeout(function () {
            vm.devices = data;
        });
    }, function (response) {
        alert("Couldn't retrieve Devices. " + JSON.stringify(response));
    });
    // Add Sensor
    vm.addSensor = function () {
        vm.errorSensorName = null;
        Sensor.add(vm.sensor).then(
                function (data) {
                    // this callback will be called asynchronously
                    // when the response is available
                    data["newborn"] = true;
                    vm.sensors.splice(0, 0, data);
                },
                function (errors) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    if (!errors.parsed) {
                        errors.global = "Something went wrong.";
                        if (errors.response.status) {
                            errors.global += " Server returned status " + errors.response.status;
                        }
                    }
                    vm.sensor.errors = errors;
                });
    };
    // Add Device
    vm.addDevice = function () {
        vm.device.errors = null;
        if (vm.device.formattedMacAddress) {
            vm.device.macAddress = normalizeMacAddress(vm.device.formattedMacAddress);
        }

        Device.add(vm.device).then(
                function (data) {
                    // this callback will be called asynchronously
                    // when the response is available
                    data.formattedMacAddress =
                            formatMacAddress(data.macAddress);
                    vm.devices.push(data);
                    vm.sensor.device = data._links.self.href;
                    detoggleDeviceForm();
                },
                function (errors) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    if (!errors.parsed) {
                        errors.global = "Something went wrong.";
                        if (errors.response.status) {
                            errors.global += " Server returned status " + errors.response.status;
                        }
                    }
                    vm.device.errors = errors;
                });
    };
}

angular.module('app')
        .controller('sensorsCtrl', sensorsCtrl);

