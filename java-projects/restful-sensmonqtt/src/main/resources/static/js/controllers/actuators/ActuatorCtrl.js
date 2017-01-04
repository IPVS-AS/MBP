function actuatorCtrl($scope, $timeout, Device, Actuator) {
    var vm = this;
    // init
    vm.actuator = [];
    vm.devices = [];
    vm.actuator = {};
    vm.device = {};
    // Load Actuators
    Actuator.getActuators().then(function (data) {
        $timeout(function () {
            vm.actuators = data;
        });
    }, function (response) {
        alert("Couldn't retrieve Actuators. " + JSON.stringify(response));
    });
    // Load Devices
    Device.getDevices().then(function (data) {
        $timeout(function () {
            vm.devices = data;
        });
    }, function (response) {
        alert("Couldn't retrieve Devices. " + JSON.stringify(response));
    });
    // Add Actuator
    vm.addActuator = function () {
        Actuator.add(vm.actuator).then(
                function (data) {
                    // this callback will be called asynchronously
                    // when the response is available
                    data["newborn"] = true;
                    vm.actuators.splice(0, 0, data);
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
                    vm.actuator.errors = errors;
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
                    vm.actuator.device = data._links.self.href;
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
        .controller('actuatorCtrl', actuatorCtrl);



