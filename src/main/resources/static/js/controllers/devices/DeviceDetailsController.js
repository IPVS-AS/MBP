/* global app */

/**
 * Controller for the device details page.
 */
app.controller('DeviceDetailsController',
    ['$scope', '$controller', '$routeParams', '$interval', 'deviceDetails', 'compatibleOperators', 'DeviceService', 'MonitoringService', 'UnitService', 'NotificationService',
        function ($scope, $controller, $routeParams, $interval, deviceDetails, compatibleOperators, DeviceService, MonitoringService, UnitService, NotificationService) {

            //Selectors that allow the selection of different ui cards
            const DETAILS_CARD_SELECTOR = ".details-card";
            const MONITORING_CONTROL_CARD_SELECTOR = ".control-card";
            const LIVE_CHART_SELECTOR_PREFIX = "#live-chart-";
            const HISTORICAL_CHART_SELECTOR_PREFIX = "#historical-chart-";
            const STATS_SELECTOR_PREFIX = "#value-stats-";

            //Important properties of the currently considered device
            const DEVICE_ID = $routeParams.id;

            let vm = this;


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if the compatible operators were retrieved successfully
                if (compatibleOperators == null) {
                    NotificationService.notify("Could not retrieve compatible operators.", "error");
                }

                //Initialize device state
                vm.deviceState = 'UNKNOWN';

                //Make device details and list of compatible operators available
                vm.device = deviceDetails;
                vm.compatibleOperators = compatibleOperators || [];

                //Prepare monitoring operator objects
                for (let i = 0; i < vm.compatibleOperators; i++) {
                    //Retrieve operator
                    let operator = vm.compatibleOperators[i];

                    //Add required properties
                    operator.enable = false;
                    operator.state = 'LOADING';
                    operator.displayUnit = operator.unit;
                    operator.reloadState = createReloadStateFunction(compatibleOperators[i].id);
                    operator.onMonitoringToggle = createMonitoringToggleFunction(compatibleOperators[i].id);
                    operator.getData = createDataRetrievalFunction(compatibleOperators[i].id);
                    operator.isUpdateable = createUpdateCheckFunction(compatibleOperators[i].id);
                    operator.getStats = createStatsRetrievalFunction(compatibleOperators[i].id);
                    operator.loadingLive = createLoadingFunctions(compatibleOperators[i].id, LIVE_CHART_SELECTOR_PREFIX,
                        "Loading live chart...");
                    operator.loadingHistorical = createLoadingFunctions(compatibleOperators[i].id,
                        HISTORICAL_CHART_SELECTOR_PREFIX, "Loading historical chart...");
                    operator.loadingStats = createLoadingFunctions(compatibleOperators[i].id, STATS_SELECTOR_PREFIX,
                        "Loading value statistics...");
                    operator.onDisplayUnitChange = createOnDisplayUnitChangeFunction(compatibleOperators[i].id);
                    operator.deleteValueLogs = createValueLogDeletionFunction(compatibleOperators[i].id);
                }

                //Stores the parameters and their values as assigned by the user
                vm.parameterValues = [];

                //Initialize deployment parameters
                initParameters();

                //Interval for updating states on a regular basis
                let interval = $interval(function () {
                    updateDeviceState(true);
                    loadMonitoringOperatorsStates();
                }, 2 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

                //Load device and operators states for the first time
                updateDeviceState();
                loadMonitoringOperatorsStates()
            })();

            /**
             * [Private]
             * Returns a function that checks whether the live chart of an operator is allowed to update its data.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createUpdateCheckFunction(monitoringOperatorId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring operator with this id
                    let operator = getMonitoringOperatorById(monitoringOperatorId);
                    if (operator == null) {
                        return;
                    }

                    return operator.state === 'RUNNING';
                }
            }

            /**
             * [Private]
             * Returns a function that allows the retrieval of monitoring log data.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createDataRetrievalFunction(monitoringOperatorId) {
                //Create function and return it
                return function (numberLogs, descending, unit) {
                    return retrieveMonitoringData(monitoringOperatorId, numberLogs, descending, unit);
                }
            }


            /**
             * [Private]
             * Returns a function that allows the retrieval of value log statistics data.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createStatsRetrievalFunction(monitoringOperatorId) {
                //Create function and return it
                return function (unit) {
                    //Return resulting promise
                    return MonitoringService.getMonitoringValueLogStats(DEVICE_ID, monitoringOperatorId, unit).then(function (response) {
                        //Success, pass statistics data
                        return response;
                    }, function (response) {
                        //Failure
                        NotificationService.notify('Could not load monitoring value log statistics.', 'error');
                        return {};
                    });

                }
            }


            /**
             * [Private]
             * Returns an object of functions that display a waiting screen when the chart wants to load data
             * and hide the waiting screen again after loading has finished.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @param chartSelectorPrefix The selector prefix for the chart container for which the waiting screen
             * is supposed to be displayed
             * @param displayText The text to display on the waiting screen
             * @returns {{start: DeviceDetailsController.start, finish: DeviceDetailsController.finish}}
             */
            function createLoadingFunctions(monitoringOperatorId, chartSelectorPrefix, displayText) {
                //Create object of functions and return it
                return {
                    start: function () {
                        //Show waiting screen
                        $(chartSelectorPrefix + monitoringOperatorId).waitMe({
                            effect: 'bounce',
                            text: displayText,
                            bg: 'rgba(255,255,255,0.85)'
                        });
                    },
                    finish: function () {
                        //Hide waiting screen
                        $(chartSelectorPrefix + monitoringOperatorId).waitMe("hide");
                    }
                }
            }


            /**
             * [Private]
             * Returns a function that handles monitoring toggle events triggered by the user.
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createMonitoringToggleFunction(monitoringOperatorId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring operator with this id
                    let operator = getMonitoringOperatorById(monitoringOperatorId);
                    if (operator == null) {
                        return;
                    }

                    //Get index of operator in operator list
                    let index = compatibleOperators.indexOf(operator)

                    //Check what the user wants
                    if (operator.enable) {
                        enableMonitoring(operator, vm.parameterValues[index]);
                    } else {
                        disableMonitoring(operator);
                    }
                };
            }

            /**
             * [Private]
             * Returns a function that retrieves the monitoring state for an operator with a certain id.
             * @param monitoringOperatorId The id of the monitoring operator
             * @returns {Function}
             */
            function createReloadStateFunction(monitoringOperatorId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring operator with this id
                    let operator = getMonitoringOperatorById(monitoringOperatorId);
                    if (operator == null) {
                        return;
                    }

                    //Enable spinner
                    operator.state = 'LOADING';

                    //Perform server request and set state of the operator object accordingly
                    MonitoringService.getMonitoringState(DEVICE_ID, operator.id).then(function (response) {
                        operator.state = response.content;
                        operator.enable = (operator.state === "RUNNING");
                    }, function (response) {
                        operator.state = 'UNKNOWN';
                        NotificationService.notify("Could not retrieve monitoring state.", "error");
                    }).then(function () {
                        $scope.$apply();
                    });
                };
            }

            /**
             * [Private]
             * Returns a function that handles display unit changes triggered by the user.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createOnDisplayUnitChangeFunction(monitoringOperatorId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring operator with this id
                    let operator = getMonitoringOperatorById(monitoringOperatorId);
                    if (operator == null) {
                        return;
                    }

                    //Check whether the entered unit is compatible with the operator unit
                    UnitService.checkUnitsForCompatibility(operator.unit, operator.displayUnitInput).then(function (response) {
                        //Check compatibility according to server response
                        if (!response) {
                            NotificationService.notify("The entered unit is not compatible to the operator unit.", "error");
                            return;
                        }

                        //Units are compatible, take user input as new unit
                        operator.displayUnit = operator.displayUnitInput;

                    }, function () {
                        NotificationService.notify("The entered unit is invalid.", "error");
                    });
                };
            }

            /**
             * [Private]
             * Returns a function that allows the deletion of all recorded value logs of the corresponding
             * monitoring component after the user confirmed the decision.
             *
             * @param monitoringOperatorId The id of the affected monitoring operator
             * @returns {Function}
             */
            function createValueLogDeletionFunction(monitoringOperatorId) {
                //Create function and return it
                return function () {
                    /**
                     * Executes the deletion of the value logs by performing the server request.
                     */
                    function executeDeletion(operator) {
                        MonitoringService.deleteMonitoringValueLogs(DEVICE_ID, monitoringOperatorId)
                            .then(function (response) {
                                //Update historical chart and stats
                                operator.historicalChartApi.updateChart();
                                operator.valueLogStatsApi.updateStats();

                                NotificationService.notify("Monitoring data was deleted successfully.", "success");
                            }, function (response) {
                                NotificationService.notify("Could not delete monitoring data.", "error");
                            });
                    }

                    //Try to find an monitoring operator with this id
                    let operator = getMonitoringOperatorById(monitoringOperatorId);
                    if (operator == null) {
                        return;
                    }

                    //Ask the user to confirm the deletion
                    return Swal.fire({
                        title: 'Delete value data',
                        type: 'warning',
                        html: "Are you sure you want to delete all monitoring data that has been recorded so far " +
                            "for this monitoring component? This action cannot be undone.",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    }).then(function (result) {
                        //Check if the user confirmed the deletion
                        if (result.value) {
                            executeDeletion(operator);
                        }
                    });


                };
            }


            /**
             * Returns the monitoring operator object that corresponds to a certain operator id, as
             * it is contained in the list of compatible operators.
             *
             * @param monitoringOperatorId
             * @returns {*}
             */
            function getMonitoringOperatorById(monitoringOperatorId) {
                let operator = null;

                //Iterate over all operators and find the matching one
                for (let i = 0; i < compatibleOperators.length; i++) {
                    if (monitoringOperatorId === compatibleOperators[i].id) {
                        operator = compatibleOperators[i];
                        break;
                    }
                }
                return operator;
            }


            /**
             * [Private]
             * Sends a server request in order to retrieve the monitoring states of all compatible monitoring operators.
             * The states are then stored in the corresponding operator objects.
             */
            function loadMonitoringOperatorsStates() {
                //Perform server request
                MonitoringService.getDeviceMonitoringState(DEVICE_ID).then(function (statesMap) {
                    //Iterate over all compatible operators and update all states accordingly
                    for (let i in compatibleOperators) {
                        let componentId = compatibleOperators[i].id + "@" + DEVICE_ID;
                        compatibleOperators[i].state = statesMap[componentId];
                        compatibleOperators[i].enable = (compatibleOperators[i].state === "RUNNING");
                    }
                }, function (response) {
                    for (let i in compatibleOperators) {
                        compatibleOperators[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve monitoring operator states.", "error");
                });
            }

            /**
             * [Public]
             * Enables monitoring of the device with a certain monitoring operator and a parameter list for this operator.
             * @param operator The monitoring operator to enable
             * @param parameterValuesList List of parameter values to use for the operator.
             */
            function enableMonitoring(operator, parameterValuesList) {
                //Show waiting screen
                showMonitoringControlWaitingScreen("Enabling monitoring...");

                //Execute enable request
                MonitoringService.enableMonitoring(DEVICE_ID, operator.id, parameterValuesList).then(
                    function (response) {
                        //Success, notify user
                        operator.state = 'RUNNING';
                        operator.enable = true;
                        NotificationService.notify('Monitoring enabled successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        operator.state = 'UNKNOWN';
                        NotificationService.notify('Enabling of monitoring failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideMonitoringControlWaitingScreen();
                    $scope.$apply();
                });
            }

            /**
             * [Private]
             * Disables monitoring of the device with a certain monitoring operator.
             * @param operator The monitoring operator to disable
             */
            function disableMonitoring(operator) {
                //Show waiting screen
                showMonitoringControlWaitingScreen("Disabling monitoring...");

                //Execute disable request
                MonitoringService.disableMonitoring(DEVICE_ID, operator.id).then(
                    function (response) {
                        //Success, notify user
                        operator.state = 'READY';
                        operator.enable = false;
                        NotificationService.notify('Monitoring disabled successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        operator.state = 'UNKNOWN';
                        NotificationService.notify('Disabling of monitoring failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideMonitoringControlWaitingScreen();
                    $scope.$apply()
                });
            }

            /**
             * [Private]
             * Retrieves a certain number of monitoring value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param monitoringOperatorId The id of the monitoring operator for which data is supposed to be retrieved
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @param unit The unit in which the values are supposed to be retrieved
             * @returns {*}
             */
            function retrieveMonitoringData(monitoringOperatorId, numberLogs, descending, unit) {
                //Set default order
                let order = 'asc';

                //Check for user option
                if (descending) {
                    order = 'desc';
                }

                //Initialize parameters for the server request
                let pageDetails = {
                    sort: 'time,' + order,
                    size: numberLogs
                };

                //Perform the server request in order to retrieve the data
                return MonitoringService.getMonitoringValueLogs(DEVICE_ID, monitoringOperatorId, pageDetails, unit);
            }

            /**
             * [Private]
             * Initializes the data structures that are required for the deployment parameters of the monitoring operators.
             */
            function initParameters() {
                //Extend parameter array for one array per compatible operator
                for (let i = 0; i < vm.compatibleOperators.length; i++) {
                    //Get formal parameters for the current operator
                    let formalParams = vm.compatibleOperators[i].parameters;

                    //Array for the parameters of the current operator
                    let operatorParameterArray = [];

                    //Iterate over all formal parameters
                    for (let j = 0; j < formalParams.length; j++) {
                        //Set empty default values for the current parameter
                        let value = "";
                        if (formalParams[j].type === "Switch") {
                            value = false;
                        }

                        //Add a tuple (name, value) for the current parameter to the operator array
                        operatorParameterArray.push({
                            "name": formalParams[j].name,
                            "value": value
                        });
                    }

                    //Add parameter array for this operator to the global array
                    vm.parameterValues.push(operatorParameterArray);
                }
            }

            /**
             * [Private]
             * Displays a waiting screen with a certain text for the monitoring control DOM container.
             * @param text The text to display
             */
            function showMonitoringControlWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(MONITORING_CONTROL_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen for the monitoring control DOM container.
             */
            function hideMonitoringControlWaitingScreen() {
                $(MONITORING_CONTROL_CARD_SELECTOR).waitMe("hide");
            }

            /**
             * [Private]
             * Displays a waiting screen with a certain text for the details DOM container.
             * @param text The text to display
             */
            function showDetailsWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(DETAILS_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             * Hides the waiting screen for the details DOM container.
             */
            function hideDetailsWaitingScreen() {
                $(DETAILS_CARD_SELECTOR).waitMe("hide");
            }

            /**
             * {Public]
             * Updates the device state. By default, a waiting screen is displayed during the update.
             * However, this can be deactivated.
             *
             * @param noWaitingScreen If set to true, no waiting screen is displayed during the refreshment
             */
            function updateDeviceState(noWaitingScreen) {
                //Check if waiting screen is supposed to be displayed
                if (!noWaitingScreen) {
                    showDetailsWaitingScreen("Retrieving device state...");
                }

                //Retrieve the state of the current device
                DeviceService.getDeviceState(DEVICE_ID).then(function (response) {
                    //Success
                    vm.deviceState = response.content;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve device state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDetailsWaitingScreen();
                    $scope.$apply()
                });
            }


            angular.extend(vm, {
                updateDeviceState: updateDeviceState,
                getData: retrieveMonitoringData
            });
        }]
);