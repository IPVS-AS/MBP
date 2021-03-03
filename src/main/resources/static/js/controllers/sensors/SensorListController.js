/* global app */

/**
 * Controller for the sensor list page.
 */
app.controller('SensorListController',
    ['$scope', '$controller', '$interval', 'sensorList', 'addSensor', 'deleteSensor',
        'deviceList', 'operatorList', 'sensorTypesList', 'accessControlPolicyList', 'ComponentService', 'NotificationService', 'OperatorService',
        function ($scope, $controller, $interval, sensorList, addSensor, deleteSensor,
                  deviceList, operatorList, sensorTypesList, accessControlPolicyList, ComponentService,
                  NotificationService, OperatorService) {
            let vm = this;

            vm.operatorList = operatorList;
            vm.deviceList = deviceList;


            // Constant list of the sensor simulators, that can be included in the test
            const SIMULATOR_LIST = {
                TEMPERATURE: 'TESTING_TemperatureSensor',
                TEMPERATURE_PL: 'TESTING_TemperatureSensorPl',
                HUMIDITY: 'TESTING_HumiditySensor',
                HUMIDITY_PL: 'TESTING_HumiditySensorPl',
            };

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                loadSensorStates();
                getListWoSimulators();
                //Interval for updating sensor states on a regular basis
                let interval = $interval(function () {
                    loadSensorStates();
                }, 5 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

                // Refresh policy select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function (e) {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            //Extend each sensor in sensorList for a state and a reload function
            for (let i in sensorList) {
                sensorList[i].state = 'LOADING';
                sensorList[i].reloadState = createReloadStateFunction(sensorList[i].id);
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
                let sensorId = data.id;
                let sensorName = "";

                //Determines the sensor's name by checking all sensors in the sensor list
                for (let i = 0; i < sensorList.length; i++) {
                    if (sensorId === sensorList[i].id) {
                        sensorName = sensorList[i].name;
                        break;
                    }
                }

                return OperatorService.getUsingTests(data.id).then(function (result) {
                    var affectedWarning = "";

                    //If the list is not empty, create a message that contains the names of all affected components
                    if (result.length > 0) {

                        affectedWarning = "<br/><br/>The following Tests are currently " +
                            "<strong> using this sensor </strong>:<br/>";

                        //Iterate over all affected components
                        for (var i = 0; i < result.length; i++) {
                            affectedWarning += "- ";
                            affectedWarning += result[i].name;
                            affectedWarning += "<br/>";
                        }
                    }

                    //Show the alert to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete sensor',
                        type: 'warning',
                        html: "Are you sure you want to delete the sensor <strong>"
                            + sensorName + "</strong>?" + affectedWarning,
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
             * Returns a function that retrieves the state for a sensor with a certain id.
             * @param id The id of the sensor
             * @returns {Function}
             */
            function createReloadStateFunction(id) {
                //Create function and return it
                return function () {
                    getSensorState(id);
                };
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment state of a sensor with a certain id.
             * The state is then stored in the corresponding sensor object in sensorList.
             *
             * @param id The id of the sensor whose state is supposed to be retrieved
             */
            function getSensorState(id) {
                //Resolve sensor object of the affected sensor
                let sensor = null;
                for (let i = 0; i < sensorList.length; i++) {
                    if (sensorList[i].id === id) {
                        sensor = sensorList[i];
                    }
                }

                //Check if sensor could be found
                if (sensor == null) {
                    return;
                }

                //Enable spinner
                sensor.state = 'LOADING';

                //Perform server request and set state of the sensor object accordingly
                ComponentService.getComponentState(sensor.id, 'sensors').then(function (response) {
                    sensor.state = response.content;
                }, function (response) {
                    sensor.state = 'UNKNOWN';
                    NotificationService.notify("Could not retrieve the sensor state.", "error");
                }).then(function () {
                    $scope.$apply()
                });
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment states of all registered sensors.
             * The states are then stored in the corresponding sensor objects in sensorList.
             */
            function loadSensorStates() {//Perform server request

                ComponentService.getAllComponentStates('sensors').then(function (statesMap) {
                    //Iterate over all sensors in sensorList and update the states of all sensors accordingly
                    for (let i in sensorList) {
                        let sensorId = sensorList[i].id;
                        sensorList[i].state = statesMap[sensorId];
                    }
                }, function (response) {
                    for (let i in sensorList) {
                        sensorList[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve sensor states.", "error");
                }).then(function () {
                    $scope.$apply();
                });
            }

            function getListWoSimulators() {
                let tempSensorList = sensorList;

                angular.forEach(SIMULATOR_LIST, function (value) {
                    sensorList.some(function (sensor) {
                        if (sensor.name === value) {
                            const index = tempSensorList.indexOf(sensor);
                            if(index !== -1){
                                tempSensorList.splice(index,1)
                            }
                        }
                    });
                });

                $scope.simExists = tempSensorList.length;

            }

            /**
             * [Public]
             * @returns {function(...[*]=)}
             */
            $scope.hideSimulators = function () {
                return function (item) {
                    if (item.name.indexOf("TESTING_") === -1){
                        return true;
                    }
                    return false;
                };
            };


            //Expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                sensorListCtrl: $controller('ItemListController as sensorListCtrl', {
                    $scope: $scope,
                    list: sensorList
                }),
                addSensorCtrl: $controller('AddItemController as addSensorCtrl', {
                    $scope: $scope,
                    entity: 'sensor',
                    addItem: addSensor
                }),
                deleteSensorCtrl: $controller('DeleteItemController as deleteSensorCtrl', {
                    $scope: $scope,
                    deleteItem: deleteSensor,
                    confirmDeletion: confirmDelete
                }),
                registeringDevice: false,
                sensorTypes: sensorTypesList,
                accessControlPolicyList: accessControlPolicyList
            });

            //Watch 'addSensor' result and add to 'sensorList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addSensorCtrl.result;
                },
                function () {
                    //Callback
                    let sensor = vm.addSensorCtrl.result;

                    if (sensor) {
                        //Close modal on success
                        $("#addSensorModal").modal('toggle');

                        //Add state and reload function to the new object
                        sensor.state = 'LOADING';
                        sensor.reloadState = createReloadStateFunction(sensor.id);

                        //Add sensor to sensor list
                        vm.sensorListCtrl.pushItem(sensor);

                        getListWoSimulators();

                        //Retrieve state of the new sensor
                        getSensorState(sensor.id);
                    }
                }
            );

            //Watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteSensorCtrl.result;
                },
                function () {
                    let id = vm.deleteSensorCtrl.result;
                    vm.sensorListCtrl.removeItem(id);
                    getListWoSimulators();
                }
            );

        }
    ]);