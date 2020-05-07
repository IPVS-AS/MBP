/* global app */

/**
 * Controller for the device details page.
 */
app.controller('DeviceDetailsController',
    ['$scope', '$controller', '$routeParams', '$interval', 'deviceDetails', 'compatibleAdapters', 'DeviceService', 'MonitoringService', 'UnitService', 'NotificationService',
        function ($scope, $controller, $routeParams, $interval, deviceDetails, compatibleAdapters, DeviceService, MonitoringService, UnitService, NotificationService) {

            //Selectors that allow the selection of different ui cards
            const DETAILS_CARD_SELECTOR = ".details-card";
            const MONITORING_CONTROL_CARD_SELECTOR = ".control-card";
            const LIVE_CHART_SELECTOR_PREFIX = "#live-chart-";
            const HISTORICAL_CHART_SELECTOR_PREFIX = "#historical-chart-";
            const STATS_SELECTOR_PREFIX = "#value-stats-";

            //Important properties of the currently considered device
            const DEVICE_ID = $routeParams.id;

            var vm = this;


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if the compatible adapters were retrieved successfully
                if (compatibleAdapters == null) {
                    NotificationService.notify("Could not retrieve compatible adapters.", "error");
                }

                //Initialize device state
                vm.deviceState = 'UNKNOWN';

                //Make device details and list of compatible adapters available
                vm.device = deviceDetails;
                vm.compatibleAdapters = compatibleAdapters;

                //Prepare monitoring adapter objects
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    //Retrieve adapter
                    var adapter = vm.compatibleAdapters[i];

                    //Add required properties
                    adapter.enable = false;
                    adapter.state = 'LOADING';
                    adapter.displayUnit = adapter.unit;
                    adapter.reloadState = createReloadStateFunction(compatibleAdapters[i].id);
                    adapter.onMonitoringToggle = createMonitoringToggleFunction(compatibleAdapters[i].id);
                    adapter.getData = createDataRetrievalFunction(compatibleAdapters[i].id);
                    adapter.isUpdateable = createUpdateCheckFunction(compatibleAdapters[i].id);
                    adapter.getStats = createStatsRetrievalFunction(compatibleAdapters[i].id);
                    adapter.loadingLive = createLoadingFunctions(compatibleAdapters[i].id, LIVE_CHART_SELECTOR_PREFIX,
                        "Loading live chart...");
                    adapter.loadingHistorical = createLoadingFunctions(compatibleAdapters[i].id,
                        HISTORICAL_CHART_SELECTOR_PREFIX, "Loading historical chart...");
                    adapter.loadingStats = createLoadingFunctions(compatibleAdapters[i].id, STATS_SELECTOR_PREFIX,
                        "Loading value statistics...");
                    adapter.onDisplayUnitChange = createOnDisplayUnitChangeFunction(compatibleAdapters[i].id);
                    adapter.deleteValueLogs = createValueLogDeletionFunction(compatibleAdapters[i].id);
                }

                //Stores the parameters and their values as assigned by the user
                vm.parameterValues = [];

                //Initialize deployment parameters
                initParameters();

                //Interval for updating states on a regular basis
                var interval = $interval(function () {
                    updateDeviceState(true);
                    loadMonitoringAdaptersStates();
                }, 2 * 60 * 1000);

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

                //Load device and adapters states for the first time
                updateDeviceState();
                loadMonitoringAdaptersStates()
            })();

            /**
             * [Private]
             * Returns a function that checks whether the live chart of an adapter is allowed to update its data.
             *
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createUpdateCheckFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring adapter with this id
                    var adapter = getMonitoringAdapterById(monitoringAdapterId);
                    if (adapter == null) {
                        return;
                    }

                    return adapter.state == 'RUNNING';
                }
            }

            /**
             * [Private]
             * Returns a function that allows the retrieval of monitoring log data.
             *
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createDataRetrievalFunction(monitoringAdapterId) {
                //Create function and return it
                return function (numberLogs, descending, unit) {
                    return retrieveMonitoringData(monitoringAdapterId, numberLogs, descending, unit);
                }
            }


            /**
             * [Private]
             * Returns a function that allows the retrieval of value log statistics data.
             *
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createStatsRetrievalFunction(monitoringAdapterId) {
                //Create function and return it
                return function (unit) {
                    //Return resulting promise
                    return MonitoringService.getMonitoringValueLogStats(DEVICE_ID, monitoringAdapterId, unit).then(function (response) {
                        //Success, pass statistics data
                        return response.data;
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
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @param chartSelectorPrefix The selector prefix for the chart container for which the waiting screen
             * is supposed to be displayed
             * @param displayText The text to display on the waiting screen
             * @returns {{start: DeviceDetailsController.start, finish: DeviceDetailsController.finish}}
             */
            function createLoadingFunctions(monitoringAdapterId, chartSelectorPrefix, displayText) {
                //Create object of functions and return it
                return {
                    start: function () {
                        //Show waiting screen
                        $(chartSelectorPrefix + monitoringAdapterId).waitMe({
                            effect: 'bounce',
                            text: displayText,
                            bg: 'rgba(255,255,255,0.85)'
                        });
                    },
                    finish: function () {
                        //Hide waiting screen
                        $(chartSelectorPrefix + monitoringAdapterId).waitMe("hide");
                    }
                }
            }


            /**
             * [Private]
             * Returns a function that handles monitoring toggle events triggered by the user.
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createMonitoringToggleFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring adapter with this id
                    var adapter = getMonitoringAdapterById(monitoringAdapterId);
                    if (adapter == null) {
                        return;
                    }

                    //Get index of adapter in adapter list
                    var index = compatibleAdapters.indexOf(adapter)

                    //Check what the user wants
                    if (adapter.enable) {
                        enableMonitoring(adapter, vm.parameterValues[index]);
                    } else {
                        disableMonitoring(adapter);
                    }
                };
            }

            /**
             * [Private]
             * Returns a function that retrieves the monitoring state for an adapter with a certain id.
             * @param monitoringAdapterId The id of the monitoring adapter
             * @returns {Function}
             */
            function createReloadStateFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring adapter with this id
                    var adapter = getMonitoringAdapterById(monitoringAdapterId);
                    if (adapter == null) {
                        return;
                    }

                    //Enable spinner
                    adapter.state = 'LOADING';

                    //Perform server request and set state of the adapter object accordingly
                    MonitoringService.getMonitoringState(DEVICE_ID, adapter.id).then(function (response) {
                        adapter.state = response.data.content;
                        adapter.enable = (adapter.state === "RUNNING");
                    }, function (response) {
                        adapter.state = 'UNKNOWN';
                        NotificationService.notify("Could not retrieve monitoring state.", "error");
                    });
                };
            }

            /**
             * [Private]
             * Returns a function that handles display unit changes triggered by the user.
             *
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createOnDisplayUnitChangeFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    //Try to find an monitoring adapter with this id
                    var adapter = getMonitoringAdapterById(monitoringAdapterId);
                    if (adapter == null) {
                        return;
                    }

                    //Check whether the entered unit is compatible with the adapter unit
                    UnitService.checkUnitsForCompatibility(adapter.unit, adapter.displayUnitInput).then(function (response) {
                        //Check compatibility according to server response
                        if (!response.data) {
                            NotificationService.notify("The entered unit is not compatible to the adapter unit.", "error");
                            return;
                        }

                        //Units are compatible, take user input as new unit
                        adapter.displayUnit = adapter.displayUnitInput;

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
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createValueLogDeletionFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    /**
                     * Executes the deletion of the value logs by performing the server request.
                     */
                    function executeDeletion(adapter) {
                        MonitoringService.deleteMonitoringValueLogs(DEVICE_ID, monitoringAdapterId)
                            .then(function (response) {
                                //Update historical chart and stats
                                adapter.historicalChartApi.updateChart();
                                adapter.valueLogStatsApi.updateStats();

                                NotificationService.notify("Monitoring data was deleted successfully.", "success");
                            }, function (response) {
                                NotificationService.notify("Could not delete monitoring data.", "error");
                            });
                    }

                    //Try to find an monitoring adapter with this id
                    var adapter = getMonitoringAdapterById(monitoringAdapterId);
                    if (adapter == null) {
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
                            executeDeletion(adapter);
                        }
                    });


                };
            }


            /**
             * Returns the monitoring adapter object that corresponds to a certain adapter id, as
             * it is contained in the list of compatible adapters.
             *
             * @param monitoringAdapterId
             * @returns {*}
             */
            function getMonitoringAdapterById(monitoringAdapterId) {
                var adapter = null;

                //Iterate over all adapters and find the matching one
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    if (monitoringAdapterId === compatibleAdapters[i].id) {
                        adapter = compatibleAdapters[i];
                        break;
                    }
                }
                return adapter;
            }


            /**
             * [Private]
             * Sends a server request in order to retrieve the monitoring states of all compatible monitoring adapters.
             * The states are then stored in the corresponding adapter objects.
             */
            function loadMonitoringAdaptersStates() {
                //Perform server request
                MonitoringService.getDeviceMonitoringState(DEVICE_ID).then(function (response) {
                    var statesMap = response.data;

                    //Iterate over all compatible adapters and update all states accordingly
                    for (var i in compatibleAdapters) {
                        var componentId = compatibleAdapters[i].id + "@" + DEVICE_ID;
                        compatibleAdapters[i].state = statesMap[componentId];
                        compatibleAdapters[i].enable = (compatibleAdapters[i].state == "RUNNING");
                    }
                }, function (response) {
                    for (var i in compatibleAdapters) {
                        compatibleAdapters[i].state = 'UNKNOWN';
                    }
                    NotificationService.notify("Could not retrieve monitoring adapter states.", "error");
                });
            }

            /**
             * [Public]
             * Enables monitoring of the device with a certain monitoring adapter and a parameter list for this adapter.
             * @param adapter The monitoring adapter to enable
             * @param parameterValuesList List of parameter values to use for the adapter.
             */
            function enableMonitoring(adapter, parameterValuesList) {
                //Show waiting screen
                showMonitoringControlWaitingScreen("Enabling monitoring...");

                //Execute enable request
                MonitoringService.enableMonitoring(DEVICE_ID, adapter.id, parameterValuesList).then(
                    function (response) {
                        //Success, check if every thing worked well
                        if (!response.data.success) {
                            adapter.state = 'UNKNOWN';
                            NotificationService.notify('Error during monitoring enabling: ' + response.data.globalMessage, 'error');
                            return;
                        }
                        //Notify user
                        adapter.state = 'RUNNING';
                        adapter.enable = true;
                        NotificationService.notify('Monitoring enabled successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        adapter.state = 'UNKNOWN';
                        NotificationService.notify('Enabling of monitoring failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideMonitoringControlWaitingScreen();
                });
            }

            /**
             * [Private]
             * Disables monitoring of the device with a certain monitoring adapter.
             * @param adapter The monitoring adapter to disable
             */
            function disableMonitoring(adapter) {
                //Show waiting screen
                showMonitoringControlWaitingScreen("Disabling monitoring...");

                //Execute disable request
                MonitoringService.disableMonitoring(DEVICE_ID, adapter.id).then(
                    function (response) {
                        //Success, check if every thing worked well
                        if (!response.data.success) {
                            adapter.state = 'UNKNOWN';
                            NotificationService.notify('Error during monitoring disabling: ' + response.data.globalMessage, 'error');
                            return;
                        }
                        //Notify user
                        adapter.state = 'READY';
                        adapter.enable = false;
                        NotificationService.notify('Monitoring disabled successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        adapter.state = 'UNKNOWN';
                        NotificationService.notify('Disabling of monitoring failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideMonitoringControlWaitingScreen();
                });
            }

            /**
             * [Private]
             * Retrieves a certain number of monitoring value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param monitoringAdapterId The id of the monitoring adapter for which data is supposed to be retrieved
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @param unit The unit in which the values are supposed to be retrieved
             * @returns {*}
             */
            function retrieveMonitoringData(monitoringAdapterId, numberLogs, descending, unit) {
                //Set default order
                var order = 'asc';

                //Check for user option
                if (descending) {
                    order = 'desc';
                }

                //Initialize parameters for the server request
                var pageDetails = {
                    sort: 'time,' + order,
                    size: numberLogs
                };

                //Perform the server request in order to retrieve the data
                return MonitoringService.getMonitoringValueLogs(DEVICE_ID, monitoringAdapterId, pageDetails, unit);
            }

            /**
             * [Private]
             * Initializes the data structures that are required for the deployment parameters of the monitoring adapters.
             */
            function initParameters() {
                //Extend parameter array for one array per compatible adapter
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    //Get formal parameters for the current adapter
                    var formalParams = compatibleAdapters[i].parameters;

                    //Array for the parameters of the current adapter
                    var adapterParameterArray = [];

                    //Iterate over all formal parameters
                    for (var j = 0; j < formalParams.length; j++) {
                        //Set empty default values for the current parameter
                        var value = "";
                        if (formalParams[j].type == "Switch") {
                            value = false;
                        }

                        //Add a tuple (name, value) for the current parameter to the adapter array
                        adapterParameterArray.push({
                            "name": formalParams[j].name,
                            "value": value
                        });
                    }

                    //Add parameter array for this adapter to the global array
                    vm.parameterValues.push(adapterParameterArray);
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
                    vm.deviceState = response.data.content;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve device state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDetailsWaitingScreen();
                });
            }


            angular.extend(vm, {
                updateDeviceState: updateDeviceState,
                getData: retrieveMonitoringData
            });
        }]
);