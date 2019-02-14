/* global app */

app.controller('DeviceListController',
    ['$scope', '$controller', '$interval', 'DeviceService', 'deviceList', 'addDevice', 'deleteDevice', 'ComponentTypeService',
        function ($scope, $controller, $interval, DeviceService, deviceList, addDevice, deleteDevice, ComponentTypeService) {
            var vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                loadDeviceTypes();
                $interval(function () {
                        loadDeviceStates();
                    }, 5 * 60 * 1000);
                loadDeviceStates();
            })();

            /**
             * Returns a function that retrieves the status for a device at a certain index in the deviceList.
             * @param index The index of the device in the deviceList
             * @returns {Function}
             */
            function createReloadStateFunction(index) {
                //Create function and return it
                return function () {
                    getDeviceState(index);
                };
            }

            //Extend each device in deviceList for the formatted mac address, a state and a reload function
            for (var i in deviceList) {
                deviceList[i].formattedMacAddress = DeviceService.formatMacAddress(deviceList[i].macAddress);
                deviceList[i].state = 'LOADING';
                deviceList[i].reloadState = createReloadStateFunction(i);
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the availability state of a device at a certain index
             * in the deviceList. The state is then stored in the corresponding device object in deviceList.
             *
             * @param index The index of the device which state is supposed to be retrieved in deviceList
             */
            function getDeviceState(index){
                //Enable spinner
                deviceList[index].state = 'LOADING';

                //Perform server request and set state of the device object accordingly
                DeviceService.getDeviceState(deviceList[index].id).then(function (response) {
                    deviceList[index].state = response.data;
                });
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain device. It also
             * shows a list of all components that are affected by this deletion.
             *
             * @param data A data object that contains the id of the device that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var deviceId = data.id;
                var deviceName = "";

                //Determines the device's name by checking all devices in the device list
                for (var i = 0; i < deviceList.length; i++) {
                    if (deviceId == deviceList[i].id) {
                        deviceName = deviceList[i].name;
                        break;
                    }
                }

                //Ask the server for all components that use this device
                return DeviceService.getUsingComponents(data.id).then(function (result) {
                    var affectedWarning = "";

                    //If the list is not empty, create a message that contains the names of all affected components
                    if (result.success && (result.data.length > 0)) {
                        affectedWarning = "<br/><br/><strong>The following components are currently " +
                            "using this device and will be deleted as well:</strong><br/>";

                        for (var i = 0; i < result.data.length; i++) {
                            affectedWarning += "- ";
                            affectedWarning += result.data[i].name;
                            affectedWarning += " (" + result.data[i].component + ")";
                            affectedWarning += "<br/>";
                        }
                    }

                    //Show the alert to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete device',
                        type: 'warning',
                        html: "Are you sure you want to delete the device \"" +
                        deviceName + "\"?" + affectedWarning,
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the availability states of all registered devices.
             * The states are then stored in the corresponding device objects in deviceList.
             */
            function loadDeviceStates(){
                //Perform server request
                DeviceService.getAllDeviceStates().then(function(response){
                    var statesMap = response.data;

                    //Iterate over all devices in deviceList and update the states of all devices accordingly
                    for (var i in deviceList) {
                        var deviceId = deviceList[i].id;
                        deviceList[i].state = statesMap[deviceId];
                    }
                });
            }

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                deviceListCtrl: $controller('ItemListController as deviceListCtrl', {
                    $scope: $scope,
                    list: deviceList
                }),
                addDeviceCtrl: $controller('AddItemController as addDeviceCtrl', {
                    $scope: $scope,
                    addItem: function (data) {
                        var deviceObject = {};

                        for (var property in data) {
                            if (data.hasOwnProperty(property)) {
                                deviceObject[property] = data[property];
                            }
                        }
                        delete deviceObject.formattedMacAddress;
                        deviceObject.macAddress = DeviceService.normalizeMacAddress(data.formattedMacAddress);

                        return addDevice(deviceObject);
                    }
                }),
                deleteDeviceCtrl: $controller('DeleteItemController as deleteDeviceCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDevice,
                    confirmDeletion: confirmDelete
                })
            });

            // $watch 'addItem' result and add to 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.addDeviceCtrl.result;
                },
                function () {
                    // callback
                    console.log('addDeviceCtrl.result modified.');

                    var data = vm.addDeviceCtrl.result;
                    if (data) {
                        data.formattedMacAddress = DeviceService.formatMacAddress(data.macAddress);
                        vm.deviceListCtrl.pushItem(data);
                    }
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteDeviceCtrl.result;
                },
                function () {
                    var id = vm.deleteDeviceCtrl.result;

                    vm.deviceListCtrl.removeItem(id);
                }
            );

            function loadDeviceTypes() {
                ComponentTypeService.GetByComponent('DEVICE')
                    .then(function (response) {
                        if (response.success) {
                            vm.deviceTypes = response.data;
                        } else {
                            console.log("Error loading device types!");
                        }
                    });
            };

        }
    ]);