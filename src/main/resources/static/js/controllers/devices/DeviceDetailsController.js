/* global app */

/**
 * Controller for the device details page.
 */
app.controller('DeviceDetailsController',
    ['$scope', '$controller', '$routeParams', '$interval', 'deviceDetails', 'compatibleAdapters', 'DeviceService', 'MonitoringService', 'NotificationService',
        function ($scope, $controller, $routeParams, $interval, deviceDetails, compatibleAdapters, DeviceService, MonitoringService, NotificationService) {

            //Selectors that allow the selection of different ui cards
            const DETAILS_CARD_SELECTOR = ".details-card";
            const MONITORING_CONTROL_CARD_SELECTOR = ".control-card";

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
                    adapter.reloadState = createReloadStateFunction(compatibleAdapters[i].id);
                    adapter.onMonitoringToggle = createMonitoringToggleFunction(compatibleAdapters[i].id);
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
             * Returns a function that handles monitoring toggle events triggered by the user.
             * @param monitoringAdapterId The id of the affected monitoring adapter
             * @returns {Function}
             */
            function createMonitoringToggleFunction(monitoringAdapterId) {
                //Create function and return it
                return function () {
                    onMonitoringToggle(monitoringAdapterId);
                };
            }

            /**
             * [Private]
             * Handles monitoring toggle events triggered by the user by enabling or disabling the monitoring for
             * a certain monitoring adapter.
             *
             * @param monitoringAdapterId The id of the affected monitoring adapter
             */
            function onMonitoringToggle(monitoringAdapterId) {
                //Find adapter with this id
                var adapter = null;
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    if (monitoringAdapterId == compatibleAdapters[i].id) {
                        adapter = compatibleAdapters[i];
                        break;
                    }
                }

                //Ensure adapter could be found
                if (adapter == null) {
                    return;
                }

                //Check what the user wants
                if (adapter.enable) {
                    enableMonitoring(adapter, vm.parameterValues[i]);
                } else {
                    disableMonitoring(adapter);
                }
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
                    getMonitoringAdapterState(monitoringAdapterId);
                };
            }

            /**
             * [Private]
             * Sends a server request in order to retrieve the monitoring state of a monitoring adapter
             * with a certain id. The state is then stored in the corresponding adapter object.
             *
             * @param monitoringAdapterId The id of the monitoring adapter whose state is supposed to be retrieved
             */
            function getMonitoringAdapterState(monitoringAdapterId) {
                //Get adapter object
                var adapter = null;
                for (var i = 0; i < compatibleAdapters.length; i++) {
                    if (compatibleAdapters[i].id == monitoringAdapterId) {
                        adapter = compatibleAdapters[i];
                    }
                }

                //Check if adapter could be found
                if (adapter == null) {
                    return;
                }

                //Enable spinner
                adapter.state = 'LOADING';

                //Perform server request and set state of the adapter object accordingly
                MonitoringService.getMonitoringState(DEVICE_ID, adapter.id).then(function (response) {
                    adapter.state = response.data;
                    adapter.enable = (adapter.state == "DEPLOYED");
                }, function (response) {
                    adapter.state = 'UNKNOWN';
                    NotificationService.notify("Could not retrieve monitoring state.", "error");
                });
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
                        compatibleAdapters[i].enable = (compatibleAdapters[i].state == "DEPLOYED");
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
                        adapter.state = 'DEPLOYED';
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
                    vm.deviceState = response.data;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve device state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDetailsWaitingScreen();
                });
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

            angular.extend(vm, {
                updateDeviceState: updateDeviceState
            });
        }]
);