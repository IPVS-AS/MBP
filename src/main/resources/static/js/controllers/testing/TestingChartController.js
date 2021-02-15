/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingChartController',
    ['$scope', '$rootScope', '$routeParams', 'TestService', 'testingDetails', 'sensorList', '$interval', 'ComponentService', 'DeviceService', 'UnitService', 'NotificationService', '$http', 'ENDPOINT_URI', 'HttpService',
        function ($scope, $rootScope, $routeParams, TestService, testingDetails, sensorList, $interval, ComponentService, DeviceService, UnitService, NotificationService, $http, ENDPOINT_URI, HttpService) {

            const vm = this;

            //Selectors that allow the selection of different ui cards
            const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
            const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const COMPONENT_ID = testingDetails.id;
            const COMPONENT_TYPE_URL = vm.component_type_url;
            const COMPONENT_OPERATOR_UNIT = vm.component_operator_unit;

            vm.sensorList = sensorList;

            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];
            vm.deploymentState = '';

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

                //Disable the loading bar
                $rootScope.showLoading = false;

                //Initialize parameters and retrieve states and stats
                updateDeploymentState();
                updateDeviceState();

                //Initialize charts
                initLiveChart();
                initHistoricalChart();


                //Interval for updating states on a regular basis
                const interval = $interval(function () {
                    updateDeploymentState(true);
                    updateDeviceState();
                }, 5000);
                //Cancel interval on route change and enable the loading bar again
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                    $rootScope.showLoading = true;
                });
            })();


            /**
             * [Public]
             *
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
                vm.deploymentStateTemp = [];
                try {
                    for (let i = 0; i < sensorList.length; i++) {
                        //Retrieve the state of the current component

                        ComponentService.getComponentState(vm.sensorList[i].id, vm.sensorList[i].componentTypeName + 's').then(function (response) {
                            //Success
                            vm.deploymentStateTemp.push(response.content);

                            if (vm.deploymentStateTemp.includes('NOT_READY')) {
                                vm.deploymentState = 'NOT_READY';
                            } else if (vm.deploymentStateTemp.includes('UNKNOWN')) {
                                vm.deploymentState = 'UNKNOWN';
                            } else if (vm.deploymentStateTemp.includes('RUNNING')) {
                                vm.deploymentState = 'RUNNING';
                            } else if (vm.deploymentStateTemp.includes('READY') || vm.deploymentStateTemp.includes('DEPLOYED')) {
                                vm.deploymentState = 'READY';
                            }

                        }, function () {
                            //Failure
                            vm.deploymentStateTemp.push('UNKNOWN');
                            NotificationService.notify('Could not retrieve deployment state.', 'error');
                        })
                    }
                } finally {
                    //Finally hide the waiting screen again
                    hideDeploymentWaitingScreen();
                }

            }

            /**
             * [Public]
             *
             * Updates the state of the device that is dedicated to the component.
             */
            function updateDeviceState() {
                vm.deviceState = 'LOADING';
                //Retrieve device state
                DeviceService.getDeviceState(vm.sensorList[0].device.id).then(function (response) {
                    //Success
                    vm.deviceState = response.content;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not load device state.', 'error');
                });
            }


            /**
             * [Public]
             *
             * Creates a server request to get a list of all generated Test Reports regarding to the Test of the IoT-Application.
             */
            function getPDFList() {
                vm.pdfDetails = [];
                TestService.getPDFList(COMPONENT_ID).then(function (response) {
                    $scope.pdfTable = response;
                });
            }

            /**
             * Called, when the user updates the unit in which the values should be displayed
             * by clicking on the update button.
             */
            function onDisplayUnitChange() {
                //Retrieve entered unit
                const inputUnit = vm.displayUnitInput;

                //Check whether the entered unit is compatible with the operator unit
                UnitService.checkUnitsForCompatibility(COMPONENT_OPERATOR_UNIT, inputUnit).then(function (response) {
                    //Check compatibility according to server response
                    if (!response) {
                        NotificationService.notify("The entered unit is not compatible to the operator unit.", "error");
                        return;
                    }

                    //Units are compatible, take user input as new unit
                    vm.displayUnit = vm.displayUnitInput;

                }, function () {
                    NotificationService.notify("The entered unit is invalid.", "error");
                });
            }


            /**
             * [Public]
             *
             * Starts the current test (in case it has been stopped before) and shows a waiting screen during
             * the start progress.
             */
            function startComponent() {
                //Show waiting screen
                vm.startTest = 'STARTING_TEST';

                //Perform request
                TestService.startTest(COMPONENT_ID).then(function (response) {
                    //Success
                    if(response === true){
                        getPDFList();
                        vm.startTest = "END_TEST";
                        NotificationService.notify('Test completed successfully.', 'success');
                    }
                }, function (response) {
                    //Handle failure
                    vm.startTest = "ERROR_TEST";
                    NotificationService.notify('Error during the test.', 'error');
                });
            }

            /**
             * [Public]
             *
             * Stops the current test and shows a waiting screen during the stop progress.
             */
            function stopComponent() {
                //Show waiting screen
                showDeploymentWaitingScreen("Stopping...");

                TestService.stopTest(COMPONENT_ID).then(function () {
                    //Finally hide the waiting screen
                    vm.deploymentState = updateDeploymentState();
                    hideDeploymentWaitingScreen();
                });

            }

            /**
             * [Public]
             *
             * Retrieves a certain number of value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @param unit The unit in which the values are supposed to be retrieved
             * @param sensor The sensor of which the values are supposed to be retrieved
             * @returns A promise that passes the logs as a parameter
             */
            function retrieveComponentData(numberLogs, descending, unit, sensor) {
                //Set default order
                let order = 'asc';

                //Check for user option
                if (descending) {
                    order = 'desc';
                }

                //Initialize parameters for the server request
                const pageDetails = {
                    sort: 'time,' + order,
                    size: numberLogs
                };

                //Perform the server request in order to retrieve the data
                return ComponentService.getValueLogs(sensor.id, 'sensor', pageDetails, unit);
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
                        return response;
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

            /**
             * [Private]
             * Initializes the live chart for displaying the most recent sensor values.
             */
            function initLiveChart() {
                /**
                 * Function that is called when the chart loads something
                 */
                function loadingStart() {
                    //Show the waiting screen
                    $(LIVE_CHART_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(LIVE_CHART_CARD_SELECTOR).waitMe("hide");
                }

                /**
                 * Function that checks whether the chart is allowed to update its data.
                 * @returns {boolean} True, if the chart may update; false otherwise
                 */
                function isUpdateable() {
                    return vm.deploymentState === 'RUNNING';
                }

                //Expose
                vm.liveChart = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    isUpdateable: isUpdateable,
                    getData: retrieveComponentData
                };
            }

            /**
             * [Private]
             *
             * Initializes the historical chart for displaying all sensor values (up to a certain limit).
             */
            function initHistoricalChart() {
                /**
                 * Function that is called when the chart loads something
                 */
                function loadingStart() {
                    //Show the waiting screen
                    $(HISTORICAL_CHART_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: 'Loading chart...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the chart finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(HISTORICAL_CHART_CARD_SELECTOR).waitMe("hide");
                }

                //Expose
                vm.historicalChart = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    getData: retrieveComponentData
                };
            }


            /**
             * [Private]
             *
             * Displays a waiting screen with a certain text for the deployment DOM container.
             * @param text The text to display
             */
            function showDeploymentWaitingScreen(text) {
                //Set a default text
                if (!text) {
                    text = 'Please wait...';
                }

                //Show waiting screen
                $(DEPLOYMENT_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             *
             * Hides the waiting screen for the deployment DOM container.
             */
            function hideDeploymentWaitingScreen() {
                $(DEPLOYMENT_CARD_SELECTOR).waitMe("hide");
            }


            function specifyChart(sensor) {

                vm.sensor = sensor;
                vm.component_id = sensor.id;
                vm.component_type = sensor.componentTypeName;
                vm.component_type_url = vm.component_type + 's';
                vm.component_operator_unit = sensor._embedded.operator.unit;


                vm.component = vm.sensor;
                vm.isLoading = false;
                vm.deploymentState = 'UNKNOWN';
                vm.deviceState = 'UNKNOWN';
                vm.displayUnit = COMPONENT_OPERATOR_UNIT;
                vm.displayUnitInput = COMPONENT_OPERATOR_UNIT;


            }

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                startComponent: startComponent,
                stopComponent: stopComponent,
            });
        }]);