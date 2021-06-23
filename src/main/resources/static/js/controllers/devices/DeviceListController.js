/**
 * Controller for the device list page.
 */
app.controller('DeviceListController',
    ['$scope', '$controller', '$interval', 'DeviceService', 'DefaultComponentsService', 'deviceList', 'addDevice',
        'updateDevice', 'deleteDevice', 'keyPairList', 'accessControlPolicyList', 'deviceTypesList',
        'NotificationService',
        function ($scope, $controller, $interval, DeviceService, DefaultComponentsService, deviceList, addDevice,
                  updateDevice, deleteDevice, keyPairList, accessControlPolicyList, deviceTypesList,
                  NotificationService) {
            let vm = this;

            //Set field for password visibility
            vm.hidePassword = true;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                loadDeviceStates();

                $scope.simExists = DefaultComponentsService.getListWoSimulators(deviceList);

                //Interval for updating device states  on a regular basis
                let interval = $interval(function () {
                    loadDeviceStates();
                }, 5 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

                //Provide details link for a given device
                $scope.detailsLink = function (device) {
                    if (device.id) {
                        return "view/devices/" + device.id;
                    }
                    return "#";
                };

                // Refresh policy select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function (e) {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            //Extend each device in deviceList for a state and a reload function
            for (let i in deviceList) {
                deviceList[i].state = 'LOADING';
                deviceList[i].reloadState = createReloadStateFunction(deviceList[i].id);
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain device. In case
             * other components are affected by this deletion, an error message is shown.
             *
             * @param data A data object that contains the id of the device that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let deviceId = data.id;
                let deviceName = "";

                //Determines the device's name by checking all devices in the device list
                for (let i = 0; i < deviceList.length; i++) {
                    if (deviceId === deviceList[i].id) {
                        deviceName = deviceList[i].name;
                        break;
                    }
                }

                //Ask the server for all components that use this device
                return DeviceService.getUsingComponents(data.id).then(function (result) {
                    //Check if list is empty
                    if (result.length > 0) {
                        //Not empty, entity cannot be deleted
                        let errorText = "The device <strong>" + deviceName + "</strong> is still used by the " +
                            "following components and thus cannot be deleted:<br/><br/>";

                        //Iterate over all affected entities
                        for (let i = 0; i < result.length; i++) {
                            errorText += "- " + result[i].name + " (" + result[i].component + ")<br/>";
                        }

                        // Show error message
                        Swal.fire({
                            icon: 'error',
                            title: 'Deletion impossible',
                            html: errorText
                        })

                        // Return new promise as result
                        return Promise.resolve({value: false});
                    }

                    //Show confirm prompt to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete device',
                        icon: 'warning',
                        html: "Are you sure you want to delete the device \"<strong>" + deviceName + "</strong>\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            /**
             * [Public]
             * Prepares the editing of a certain device, given by its ID.
             *
             * @param deviceId The ID of the device
             */
            function editDevice(deviceId) {
                //Find the device template with this ID
                let device = null;
                for (let i = 0; i < deviceList.length; i++) {
                    if (deviceId === deviceList[i].id) {
                        //Found, remember device
                        device = deviceList[i];
                        break;
                    }
                }

                //Sanity check
                if (device == null) return;

                //Set copy of location template
                vm.addDeviceCtrl.item = Object.assign({}, device)
            }

            /**
             * [Public]
             * Saves the currently modified device, either by creating a new one or by updating an
             * existing one.
             */
            function saveDevice() {
                //Check if device ID is set
                if (vm.addDeviceCtrl.item.hasOwnProperty("id") && (vm.addDeviceCtrl.item.id.length > 0)) {
                    //Copy device data to update controller
                    vm.updateDeviceCtrl.item = vm.addDeviceCtrl.item;

                    //Update existing device
                    vm.updateDeviceCtrl.updateItem().then((data) => {
                        $scope.$apply();
                    });
                } else {
                    //Create new device
                    vm.addDeviceCtrl.addItem();
                }
            }

            /**
             * [Private]
             * Returns a function that retrieves the state for a device with a certain id.
             * @param id The id of the device
             * @returns {Function}
             */
            function createReloadStateFunction(id) {
                //Create function and return it
                return function () {
                    getDeviceState(id);
                };
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the availability state of a device with a certain id.
             * The state is then stored in the corresponding device object in deviceList.
             *
             * @param id The id of the device whose state is supposed to be retrieved
             */
            function getDeviceState(id) {
                //Resolve device object of the affected device
                let device = null;
                for (let i = 0; i < deviceList.length; i++) {
                    if (deviceList[i].id === id) {
                        device = deviceList[i];
                    }
                }

                //Check if device could be found
                if (device == null) {
                    return;
                }

                //Enable spinner
                device.state = 'LOADING';

                //Perform server request and set state of the device object accordingly
                DeviceService.getDeviceState(device.id).then(function (response) {
                    device.state = response.content;
                }, function (response) {
                    device.state = 'UNKNOWN';
                    NotificationService.notify("Could not retrieve the device state.", "error");
                }).then(function () {
                    $scope.$apply();
                });
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the availability states of all registered devices.
             * The states are then stored in the corresponding device objects in deviceList.
             */
            function loadDeviceStates() {
                //Perform server request
                DeviceService.getAllDeviceStates().then(function (statesMap) {
                    //Iterate over all devices in deviceList and update the states of all devices accordingly
                    for (let i in deviceList) {
                        let deviceId = deviceList[i].id;
                        deviceList[i].state = statesMap[deviceId];
                    }
                }, function (response) {
                    for (let i in deviceList) {
                        deviceList[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve device states.", "error");
                }).then(function () {
                    $scope.$apply();
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
                    entity: 'device',
                    addItem: function (data) {
                        let deviceObject = {};

                        for (let property in data) {
                            if (data.hasOwnProperty(property)) {
                                deviceObject[property] = data[property];
                            }
                        }

                        return addDevice(deviceObject);
                    }
                }),
                updateDeviceCtrl: $controller('UpdateItemController as updateDeviceCtrl', {
                    $scope: $scope,
                    updateItem: function (data) {
                        let deviceObject = {};

                        for (let property in data) {
                            if (data.hasOwnProperty(property)) {
                                deviceObject[property] = data[property];
                            }
                        }

                        return updateDevice(deviceObject);
                    }
                }),
                deleteDeviceCtrl: $controller('DeleteItemController as deleteDeviceCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDevice,
                    confirmDeletion: confirmDelete
                }),
                accessControlPolicyList: accessControlPolicyList,
                deviceTypesList: deviceTypesList,
                keyPairList: keyPairList,
                editDevice: editDevice,
                saveDevice: saveDevice
            });

            // $watch 'addItem' result and add to 'itemList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addDeviceCtrl.result;
                },
                function () {
                    //Callback
                    let device = vm.addDeviceCtrl.result;

                    if (device) {
                        //Close modal on success
                        $("#addDeviceModal").modal('toggle');

                        //Add state and reload function to the new object
                        device.state = 'LOADING';
                        device.reloadState = createReloadStateFunction(device.id);

                        //Add device to device list
                        vm.deviceListCtrl.pushItem(device);
                        $scope.simExists = DefaultComponentsService.getListWoSimulators(deviceList);

                        //Retrieve state of the new device
                        getDeviceState(device.id);
                    }
                }
            );

            //Watch controller result of device updates
            $scope.$watch(() => vm.updateDeviceCtrl.result, (device) => {
                //Sanity check
                if (!device) return;

                //Close modal on success
                $("#addDeviceModal").modal('toggle');

                //Add state and reload function to the new object
                device.state = 'LOADING';
                device.reloadState = createReloadStateFunction(device.id);

                //Update device list
                vm.deviceListCtrl.updateItem(device);

                //Retrieve state of the new device
                getDeviceState(device.id);
            });

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteDeviceCtrl.result;
                },
                function () {
                    let id = vm.deleteDeviceCtrl.result;

                    vm.deviceListCtrl.removeItem(id);
                    $scope.simExists = DefaultComponentsService.getListWoSimulators(deviceList);
                }
            );
        }
    ]);