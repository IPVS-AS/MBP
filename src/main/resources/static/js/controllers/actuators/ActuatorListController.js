/* global app */

/**
 * Controller for the actuator list page.
 */
app.controller('ActuatorListController',
    ['$scope', '$controller', '$interval', 'actuatorList', 'addActuator', 'deleteActuator',
        'deviceList', 'operatorList', 'actuatorTypesList', 'accessControlPolicyList', 'ComponentService', 'NotificationService',
        function ($scope, $controller, $interval, actuatorList, addActuator, deleteActuator,
                  deviceList, operatorList, actuatorTypesList, accessControlPolicyList, ComponentService, NotificationService) {
            let vm = this;

            // Constant list of the actuator simulators, that can be included in the test
            const SIMULATOR_LIST = {
                ACTUATOR: 'TESTING_Actuator'
            };

            vm.operatorList = operatorList;
            vm.deviceList = deviceList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                loadActuatorStates();
                getListWoSimulators();

                //Interval for updating actuator states on a regular basis
                let interval = $interval(function () {
                    loadActuatorStates();
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

            //Extend each actuator in actuatorList for a state and a reload function
            for (let i in actuatorList) {
                actuatorList[i].state = 'LOADING';
                actuatorList[i].reloadState = createReloadStateFunction(actuatorList[i].id);
            }

            /**
             * [Public]
             * @param actuator
             * @returns {*}
             */
            $scope.detailsLink = function (actuator) {
                if (actuator.id) {
                    return "view/actuators/" + actuator.id;
                }
                return "#";
            };



            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain actuator.
             *
             * @param data A data object that contains the id of the actuator that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let actuatorId = data.id;
                let actuatorName = "";

                //Determines the actuator's name by checking all actuators in the actuator list
                for (let i = 0; i < actuatorList.length; i++) {
                    if (actuatorId === actuatorList[i].id) {
                        actuatorName = actuatorList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete actuator',
                    type: 'warning',
                    html: "Are you sure you want to delete actuator \"" + actuatorName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            /**
             * [Private]
             * Returns a function that retrieves the state for a actuator with a certain id.
             * @param id The id of the actuator
             * @returns {Function}
             */
            function createReloadStateFunction(id) {
                //Create function and return it
                return function () {
                    getActuatorState(id);
                };
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment state of a actuator with a certain id.
             * The state is then stored in the corresponding actuator object in actuatorList.
             *
             * @param id The id of the actuator whose state is supposed to be retrieved
             */
            function getActuatorState(id) {
                //Resolve actuator object of the affected actuator
                let actuator = null;
                for (let i = 0; i < actuatorList.length; i++) {
                    if (actuatorList[i].id === id) {
                        actuator = actuatorList[i];
                    }
                }

                //Check if actuator could be found
                if (actuator == null) {
                    return;
                }

                //Enable spinner
                actuator.state = 'LOADING';

                //Perform server request and set state of the actuator object accordingly
                ComponentService.getComponentState(actuator.id, 'actuators').then(function (response) {
                    actuator.state = response.content;
                }, function (response) {
                    actuator.state = 'UNKNOWN';
                    NotificationService.notify("Could not retrieve the actuator state.", "error");
                }).then(function () {
                    $scope.$apply();
                });
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the deployment states of all registered actuators.
             * The states are then stored in the corresponding actuator objects in actuatorList.
             */
            function loadActuatorStates() {//Perform server request

                ComponentService.getAllComponentStates('actuators').then(function (statesMap) {
                    //Iterate over all actuators in actuatorList and update the states of all actuators accordingly
                    for (let i in actuatorList) {
                        let actuatorId = actuatorList[i].id;
                        actuatorList[i].state = statesMap[actuatorId];
                    }
                }, function (response) {
                    for (let i in actuatorList) {
                        actuatorList[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve actuator states.", "error");
                }).then(function () {
                    $scope.$apply();
                });
            }

            function getListWoSimulators() {
                let tempActuatorList = actuatorList;

                angular.forEach(SIMULATOR_LIST, function (value) {
                    actuatorList.some(function (actuator) {
                        if (actuator.name === value) {
                            const index = tempActuatorList.indexOf(actuator);
                            if(index !== -1){
                                tempActuatorList.splice(index,1)
                            }
                        }
                    });

                });

                $scope.simExists = tempActuatorList.length;

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
                actuatorListCtrl: $controller('ItemListController as actuatorListCtrl', {
                    $scope: $scope,
                    list: actuatorList
                }),
                addActuatorCtrl: $controller('AddItemController as addActuatorCtrl', {
                    $scope: $scope,
                    entity: 'actuator',
                    addItem: addActuator
                }),
                deleteActuatorCtrl: $controller('DeleteItemController as deleteActuatorCtrl', {
                    $scope: $scope,
                    deleteItem: deleteActuator,
                    confirmDeletion: confirmDelete
                }),
                registeringDevice: false,
                actuatorTypes: actuatorTypesList,
                accessControlPolicyList: accessControlPolicyList
            });

            //Watch 'addActuator' result and add to 'actuatorList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addActuatorCtrl.result;
                },
                function () {
                    //Callback
                    let actuator = vm.addActuatorCtrl.result;

                    if (actuator) {
                        //Close modal on success
                        $("#addActuatorModal").modal('toggle');

                        //Add state and reload function to the new object
                        actuator.state = 'LOADING';
                        actuator.reloadState = createReloadStateFunction(actuator.id);

                        //Add actuator to actuator list
                        vm.actuatorListCtrl.pushItem(actuator);
                        getListWoSimulators();

                        //Retrieve state of the new actuator
                        getActuatorState(actuator.id);
                    }
                }
            );

            //Watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteActuatorCtrl.result;
                },
                function () {
                    let id = vm.deleteActuatorCtrl.result;
                    vm.actuatorListCtrl.removeItem(id);
                    getListWoSimulators();
                }
            );

        }
    ]);