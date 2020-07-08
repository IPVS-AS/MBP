/* global app */

/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails', '$rootScope', '$routeParams', '$interval', 'UnitService', 'NotificationService', '$http',
        function ($scope, controller, testingDetails, $rootScope, $routeParams, $interval, UnitService, NotificationService, $http) {
            //Selectors that allow the selection of different ui cards
            const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
            const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const STATS_CARD_SELECTOR = ".stats-card";

            //Important properties of the currently considered component
            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = testingDetails;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';
            const COMPONENT_ADAPTER_UNIT = testingDetails._embedded;


            //  CrudService.fetchSpecificItem()

            //Initialization of variables that are used in the frontend by angular
            var vm = this;
            vm.test = testingDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';
            vm.displayUnit = COMPONENT_ADAPTER_UNIT;
            vm.displayUnitInput = COMPONENT_ADAPTER_UNIT;
            vm.ruleNames = "";
            vm.actionNames = "";
            vm.deviceNames = "";



            $http.get(testingDetails._links.rules.href).success(function successCallback(responseRules) {

                console.log(responseRules);
                for (let i = 0; i < responseRules._embedded.rules.length; i++) {
                    if (i === 0) {
                        vm.ruleNames = vm.ruleNames + responseRules._embedded.rules[i].name;
                        vm.actionNames = vm.actionNames + responseRules._embedded.rules[i].actionNames;
                    } else {
                        vm.ruleNames = vm.ruleNames + ", " + responseRules._embedded.rules[i].name;
                        for (let x = 0; x < responseRules._embedded.rules[i].actionNames.length; x++) {
                            if (vm.actionNames.includes(responseRules._embedded.rules[i].actionNames[x])) {

                            } else {
                                vm.actionNames = vm.actionNames + ", " + responseRules._embedded.rules[i].actionNames[x];
                            }
                        }
                    }
                }

            });


            $http.get(testingDetails._links.sensor.href).success(function successCallback(responseSensors) {
                for (let i = 0; i < responseSensors._embedded.sensors.length; i++) {
                    if (i === 0) {
                        vm.deviceNames = vm.deviceNames + responseSensors._embedded.sensors[i]._embedded.device.name;

                    } else {
                        vm.deviceNames = vm.deviceNames + ", " + responseSensors._embedded.sensors[i]._embedded.device.name;
                    }
                }


            });

            console.log(testingDetails, '\n', COMPONENT_ADAPTER_UNIT, '\n', COMPONENT_ID);

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Disable the loading bar
                $rootScope.showLoading = false;

                //Initialize parameters and retrieve states and stats
                initParameters();
                showPDF();
                /**
                 updateDeploymentState();
                 updateDeviceState();

                 //Initialize value log stats
                 initValueLogStats();

                 //Initialize charts
                 initLiveChart();
                 initHistoricalChart();

                 //Interval for updating states on a regular basis
                 var interval = $interval(function () {
                    updateDeploymentState(true);
                    updateDeviceState();
                }, 2 * 60 * 1000);

                 //Cancel interval on route change and enable the loading bar again
                 $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                    $rootScope.showLoading = true;
                });
                 **/
            })();

            /**
             * {Public]
             * Updates the deployment state of the currently considered component. By default, a waiting screen
             * is displayed during the update. However, this can be deactivated.
             *
             * @param noWaitingScreen If set to true, no waiting screen is displayed during the refreshment
             */
            function updateDeploymentState(noWaitingScreen) {
                //Check if waiting screen is supposed to be displayed
                if (!noWaitingScreen) {
                    showDeploymentWaitingScreen("Retrieving component state...");
                }

                //Retrieve the state of the current component
                ComponentService.getComponentState(COMPONENT_ID, COMPONENT_TYPE_URL).then(function (response) {
                    //Success
                    vm.deploymentState = response.data.content;
                }, function (response) {
                    //Failure
                    vm.deploymentState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve deployment state.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen again
                    hideDeploymentWaitingScreen();
                });
            }

            /**
             * [Public]
             *
             * Updates the state of the device that is dedicated to the component.
             */
            function updateDeviceState() {
                vm.deviceState = 'LOADING';

                //Retrieve device state
                DeviceService.getDeviceState(componentDetails._embedded.device.id).then(function (response) {
                    //Success
                    vm.deviceState = response.data.content;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not load device state.', 'error');
                });
            }


            /**
             * [Public]
             * Called, when the user updates the unit in which the values should be displayed
             * by clicking on the update button.
             */

            /**
             function onDisplayUnitChange() {
                //Retrieve entered unit
                var inputUnit = vm.displayUnitInput;

                //Check whether the entered unit is compatible with the adapter unit
                UnitService.checkUnitsForCompatibility(COMPONENT_ADAPTER_UNIT, inputUnit).then(function (response) {
                    //Check compatibility according to server response
                    if (!response.data) {
                        NotificationService.notify("The entered unit is not compatible to the adapter unit.", "error");
                        return;
                    }

                    //Units are compatible, take user input as new unit
                    vm.displayUnit = vm.displayUnitInput;

                }, function () {
                    NotificationService.notify("The entered unit is invalid.", "error");
                });
            }
             **/


            /**
             * [Public]
             * Starts the current component (in case it has been stopped before) and shows a waiting screen during
             * the start progress.
             */
            function startComponent() {
                //Show waiting screen
                showDeploymentWaitingScreen("Starting...");

                //Execute start request
                ComponentService.startComponent(COMPONENT_ID, COMPONENT_TYPE, vm.parameterValues)
                    .then(function (response) {
                            //Success, check if everything worked well
                            if (!response.data.success) {
                                vm.deploymentState = 'UNKNOWN';
                                NotificationService.notify('Error during starting: ' + response.data.globalMessage, 'error');
                                return;
                            }
                            //Notify user
                            vm.deploymentState = 'RUNNING';
                            NotificationService.notify('Component started successfully.', 'success');
                        },
                        function (response) {
                            //Failure
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Starting failed.', 'error');
                        }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                });
            }

            /**
             * [Public]
             * Stops the current component and shows a waiting screen during the stop progress.
             */
            function stopComponent() {
                //Show waiting screen
                showDeploymentWaitingScreen("Stopping...");

                //Execute stop request
                ComponentService.stopComponent(COMPONENT_ID, COMPONENT_TYPE).then(function (response) {
                        //Success, check if everything worked well
                        if (!response.data.success) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Error during stopping: ' + response.data.globalMessage, 'error');
                            return;
                        }
                        //Notify user
                        vm.deploymentState = 'DEPLOYED';
                        NotificationService.notify('Component stopped successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Stopping failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                });
            }

            /**
             * [Public]
             * Retrieves a certain number of value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @param unit The unit in which the values are supposed to be retrieved
             * @returns A promise that passes the logs as a parameter
             */
            function retrieveComponentData(numberLogs, descending, unit) {
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
                return ComponentService.getValueLogs(COMPONENT_ID, COMPONENT_TYPE, pageDetails, unit);
            }

            /**
             * [Public]
             * Asks the user if he really wants to delete all value logs for the current component. If this is the case,
             * the deletion is executed by creating the corresponding server request.
             */
            function deleteValueLogs() {
                /**
                 * Executes the deletion of the value logs by performing the server request.
                 */
                function executeDeletion() {
                    ComponentService.deleteValueLogs(COMPONENT_ID, COMPONENT_TYPE).then(function (response) {
                        //Update historical chart and stats
                        $scope.historicalChartApi.updateChart();
                        $scope.valueLogStatsApi.updateStats();

                        NotificationService.notify("Value logs were deleted successfully.", "success");
                    }, function (response) {
                        NotificationService.notify("Could not delete value logs.", "error");
                    });
                }

                //Ask the user to confirm the deletion
                return Swal.fire({
                    title: 'Delete value data',
                    type: 'warning',
                    html: "Are you sure you want to delete all value data that has been recorded so far for this " +
                        "component? This action cannot be undone.",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then(function (result) {
                    //Check if the user confirmed the deletion
                    if (result.value) {
                        executeDeletion();
                    }
                });
            }


            /**
             * [Private]
             * Initializes the value log stats display.
             */
            function initValueLogStats() {
                /**
                 * Function that is called when the value log stats display loads something
                 */
                function loadingStart() {
                    //Show waiting screen
                    $(STATS_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: 'Loading value statistics...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the value log stats display finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(STATS_CARD_SELECTOR).waitMe("hide");
                }

                /**
                 * Function that is used by the value log stats display to retrieve the statistics in a specific unit
                 * from the server.
                 */
                function getStats(unit) {
                    return ComponentService.getValueLogStats(COMPONENT_ID, COMPONENT_TYPE_URL, unit).then(function (response) {
                        //Success, pass statistics data
                        return response.data;
                    }, function (response) {
                        //Failure
                        NotificationService.notify('Could not load value log statistics.', 'error');
                        return {};
                    });
                }

                vm.valueLogStats = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    getStats: getStats
                };
            }


            function showPDF() {

                console.log("showPDF Methode")
                if(testingDetails.pdfExists == true){
                    document.getElementById("pdfExists").innerHTML = testingDetails.endTestTime;
                    document.getElementById("downloadReport").disabled = false;
                }
            }

            /**
             * Sends a server request to open the test report of a specific test fiven by its id.
             *
             * @param testID
             */
            function downloadPDF() {
                window.open('api/test-details/downloadPDF/' + testingDetails.id, '_blank');
            }



            /**
             * [Private]
             * Initializes the data structures that are required for the deployment parameters.
             */
            function initParameters() {
                //Retrieve all formal parameters for this component
                var requiredParams = testingDetails._embedded;



                console.log(testingDetails._embedded);

            }




            /**
             * [Private]
             * Hides the waiting screen for the deployment DOM container.
             */
            function hideDeploymentWaitingScreen() {
                $(DEPLOYMENT_CARD_SELECTOR).waitMe("hide");
            }

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                updateDeploymentState: updateDeploymentState,
                updateDeviceState: updateDeviceState,
                startComponent: startComponent,
                stopComponent: stopComponent,
                deleteValueLogs: deleteValueLogs,
                showPDF: showPDF,
                downloadPDF: downloadPDF
            });
        }]
);
