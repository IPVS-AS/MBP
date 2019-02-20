/* global app */

app.controller('SensorListController',
    ['$scope', '$controller', '$interval', 'sensorList', 'addSensor', 'deleteSensor',
        'deviceList', 'addDevice', 'deleteDevice', 'adapterList', 'ComponentService',
        'ComponentTypeService', 'NotificationService',
        function ($scope, $controller, $interval, sensorList, addSensor, deleteSensor,
                  deviceList, addDevice, deleteDevice, adapterList, ComponentService,
                  ComponentTypeService, NotificationService) {
            var vm = this;

            (function initController() {
                loadSensorTypes();
                loadSensorStates();

                //Interval for updating sensor states on a regular basis
                var interval = $interval(function () {
                    loadSensorStates();
                }, 5 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });
            })();

            //Extend each sensor in sensorList for a state and a reload function
            for (var i in sensorList) {
                sensorList[i].state = 'LOADING';
                sensorList[i].reloadState = createReloadStateFunction(i);
            }

            /**
             * [Public]
             * @param sensor
             * @returns {*}
             */
            $scope.detailsLink = function (sensor) {
                if (sensor.id) {
                    return "view/sensors/" + sensor.id;
                }
                return "#";
            };

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain sensor.
             *
             * @param data A data object that contains the id of the sensor that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var sensorId = data.id;
                var sensorName = "";

                //Determines the sensor's name by checking all sensors in the sensor list
                for (var i = 0; i < sensorList.length; i++) {
                    if (sensorId == sensorList[i].id) {
                        sensorName = sensorList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete sensor',
                    type: 'warning',
                    html: "Are you sure you want to delete sensor \"" + sensorName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            /**
             * [Private]
             * Returns a function that retrieves the state for a sensor at a certain index in sensorList.
             * @param index The index of the sensor in the sensorList
             * @returns {Function}
             */
            function createReloadStateFunction(index) {
                //Create function and return it
                return function () {
                    getSensorState(index);
                };
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment state of a sensor at a certain index
             * in sensorList. The state is then stored in the corresponding sensor object in sensorList.
             *
             * @param index The index of the sensor whose state is supposed to be retrieved in sensorList
             */
            function getSensorState(index) {
                //Enable spinner
                sensorList[index].state = 'LOADING';

                //Perform server request and set state of the sensor object accordingly
                ComponentService.getComponentState(sensorList[index].id, 'sensors').then(function (response) {
                    sensorList[index].state = response.data;
                }, function (response) {
                    sensorList[index].state = 'UNKNOWN';
                    NotificationService.notify("Could not retrieve the sensor state.", "error");
                });
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment states of all registered sensors.
             * The states are then stored in the corresponding sensor objects in sensorList.
             */
            function loadSensorStates() {//Perform server request

                ComponentService.getAllComponentStates('sensors').then(function (response) {
                    var statesMap = response.data;

                    //Iterate over all sensors in sensorList and update the states of all sensors accordingly
                    for (var i in sensorList) {
                        var sensorId = sensorList[i].id;
                        sensorList[i].state = statesMap[sensorId];
                    }
                }, function (response) {
                    for (var i in sensorList) {
                        sensorList[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve sensor states.", "error");
                });
            }

            //Expose
            angular.extend(vm, {
                registeringDevice: false
            });

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                sensorListCtrl: $controller('ItemListController as sensorListCtrl', {
                    $scope: $scope,
                    list: sensorList
                }),
                addSensorCtrl: $controller('AddItemController as addSensorCtrl', {
                    $scope: $scope,
                    addItem: addSensor
                }),
                deleteSensorCtrl: $controller('DeleteItemController as deleteSensorCtrl', {
                    $scope: $scope,
                    deleteItem: deleteSensor,
                    confirmDeletion: confirmDelete
                }),
                deviceCtrl: $controller('DeviceListController as deviceCtrl', {
                    $scope: $scope,
                    deviceList: deviceList,
                    addDevice: addDevice,
                    deleteDevice: deleteDevice
                }),
                adapterListCtrl: $controller('ItemListController as adapterListCtrl', {
                    $scope: $scope,
                    list: adapterList
                })
            });

            // $watch 'addSensor' result and add to 'sensorList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.addSensorCtrl.result;
                },
                function () {
                    // callback
                    console.log('addSensorCtrl.result modified.');

                    var data = vm.addSensorCtrl.result;
                    if (data) {
                        vm.sensorListCtrl.pushItem(vm.addSensorCtrl.result);
                    }
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteSensorCtrl.result;
                },
                function () {
                    var id = vm.deleteSensorCtrl.result;

                    vm.sensorListCtrl.removeItem(id);
                }
            );

            // $watch 'addDevice' result and select on sensor form
            $scope.$watch(
                function () {
                    // value being watched
                    return $scope.addDeviceCtrl.result;
                },
                function () {
                    // callback
                    console.log('addDeviceCtrl.result modified.');

                    var data = $scope.addDeviceCtrl.result;
                    if (data) {
                        $scope.addSensorCtrl.item.device = data._links.self.href;
                        vm.registeringDevice = false;
                    }
                }
            );

            function loadSensorTypes() {
                ComponentTypeService.GetByComponent('SENSOR')
                    .then(function (response) {
                        if (response.success) {
                            vm.sensorTypes = response.data;
                        } else {
                            console.log("Error loading sensor types!");
                        }
                    });
            };

        }
    ]);