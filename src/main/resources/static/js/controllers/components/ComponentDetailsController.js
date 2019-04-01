/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('ComponentDetailsController',
    ['$scope', '$rootScope', '$routeParams', '$interval', '$timeout', 'componentDetails', 'historicalChartContainer', 'historicalChartSlider', 'ComponentService', 'DeviceService', 'UnitService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, $interval, $timeout, componentDetails, historicalChartContainer, historicalChartSlider, ComponentService, DeviceService, UnitService, NotificationService) {
            //Initial number of elements to display in the historical chart
            const HISTORICAL_CHART_INITIAL_ELEMENTS_NUMBER = 200;

            //Minimum/maximum number of elements that can be displayed in the historical chart
            const HISTORICAL_CHART_MIN_ELEMENTS = 0;
            const HISTORICAL_CHART_MAX_ELEMENTS = 5000;

            //Selectors that allow the selection of different ui cards
            const LIVE_CHART_CARD_SELECTOR = ".live-chart-card";
            const HISTORICAL_CHART_CARD_SELECTOR = ".historical-chart-card";
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const STATS_CARD_SELECTOR = ".stats-card";

            //Important properties of the currently considered component
            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = componentDetails.componentTypeName;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';
            const COMPONENT_ADAPTER_UNIT = componentDetails._embedded.adapter.unit;

            //Initialization of variables that are used in the frontend by angular
            var vm = this;
            vm.component = componentDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';
            vm.valueLogStats = null;
            vm.displayUnit = COMPONENT_ADAPTER_UNIT;
            vm.displayUnitInput = COMPONENT_ADAPTER_UNIT;

            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];

            //Settings for the historical chart
            vm.historicalChartSettings = {
                numberOfValues: HISTORICAL_CHART_INITIAL_ELEMENTS_NUMBER,
                mostRecent: true
            };

            //Hold the chart objects after the charts have been initialized
            var historicalChart = null;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Disable the loading bar
                $rootScope.showLoading = false;

                //Initialize parameters and retrieve states and stats
                initParameters();
                updateDeploymentState();
                updateDeviceState();
                updateValueLogStats();

                //Initialize charts
                initLiveChart();
                initHistoricalChart();

                //Interval for updating states on a regular basis
                var interval = $interval(function () {
                    updateDeploymentState(true);
                    updateDeviceState();
                    updateValueLogStats();
                }, 2 * 60 * 1000);

                //Cancel interval on route change and enable the loading bar again
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                    $rootScope.showLoading = true;
                });
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
                    vm.deploymentState = response.data;
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
                    vm.deviceState = response.data;
                }, function (response) {
                    //Failure
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not load device state.', 'error');
                });
            }

            /**
             * [Public]
             * Updates the value log stats of the currently considered component. By default, a waiting screen
             * is displayed during the update. However, this can be deactivated.
             *
             * @param noWaitingScreen If set to true, no waiting screen is displayed during the refreshment
             */
            function updateValueLogStats(noWaitingScreen) {
                //Display waiting screen if desired
                if (!noWaitingScreen) {
                    $(STATS_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: "Loading overview...",
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                //Retrieve value log stats for this component
                ComponentService.getValueLogStats(COMPONENT_ID, COMPONENT_TYPE_URL, vm.displayUnit).then(function (response) {
                    //Success
                    vm.valueLogStats = response.data;
                }, function (response) {
                    //Failure
                    NotificationService.notify('Could not load value log statistics.', 'error');
                }).then(function () {
                    //Finally hide the waiting screen
                    $(STATS_CARD_SELECTOR).waitMe("hide");
                });
            }

            /**
             * [Public]
             * Called, when the user updates the unit in which the values should be displayed
             * by clicking on the update button.
             */
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

                    /*
                    Units are compatible, take user input and update everything accordingly
                    */
                    vm.displayUnit = vm.displayUnitInput;

                    //Value stats
                    updateValueLogStats();

                    //Historical chart
                    updateHistoricalChart();

                }, function () {
                    NotificationService.notify("The entered unit is invalid.", "error");
                });
            }

            /**
             * [Public]
             * Deploys the current component and shows a waiting screen during the deployment.
             */
            function deploy() {
                //Show waiting screen
                showDeploymentWaitingScreen("Deploying...");

                //Execute deployment request
                ComponentService.deploy(vm.parameterValues, componentDetails._links.deploy.href).then(
                    function (response) {
                        //Success, check if every thing worked well
                        if (!response.data.success) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Error during deployment: ' + response.data.globalMessage, 'error');
                            return;
                        }
                        //Notify user
                        vm.deploymentState = 'DEPLOYED';
                        NotificationService.notify('Component deployed successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Deployment failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                });
            }

            /**
             * [Public]
             * Undeploys the current component and shows a waiting screen during the undeployment.
             */
            function undeploy() {
                //Show waiting screen
                showDeploymentWaitingScreen("Undeploying...");

                //Execute undeployment request
                ComponentService.undeploy(componentDetails._links.deploy.href).then(
                    function (response) {
                        //Success, check if every thing worked well
                        if (!response.data.success) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Error during undeployment: ' + response.data.globalMessage, 'error');
                            return;
                        }
                        //Notify user
                        vm.deploymentState = 'READY';
                        NotificationService.notify('Component undeployed successfully.', 'success');
                    },
                    function (response) {
                        //Failure
                        vm.deploymentState = 'UNKNOWN';
                        NotificationService.notify('Undeployment failed.', 'error');
                    }).then(function () {
                    //Finally hide the waiting screen
                    hideDeploymentWaitingScreen();
                });
            }

            /**
             * [Public]
             * Updates the historical chart. While the refreshment, a waiting screen is displayed.
             */
            function updateHistoricalChart() {
                //Check if historical chart has already been initialized
                if (historicalChart == null) {
                    console.error("The historical chart has not been initialized yet.");
                    return;
                }

                //Show waiting screen
                $(HISTORICAL_CHART_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: 'Updating chart...',
                    bg: 'rgba(255,255,255,0.85)'
                });

                //Set y-axis and tooltip unit to currently displayed unit and redraw chart
                historicalChart.yAxis[0].labelFormatter = function () {
                    return this.value + ' ' + vm.displayUnit;
                };
                historicalChart.series[0].tooltipOptions.valueSuffix = ' ' + vm.displayUnit;

                //Retrieve a fixed number of value logs from the server
                retrieveComponentData(vm.historicalChartSettings.numberOfValues,
                    vm.historicalChartSettings.mostRecent).then(function (values) {
                    //Reverse the values array if ordered in descending order
                    if (vm.historicalChartSettings.mostRecent) {
                        values = values.reverse();
                    }

                    //Update historical chart
                    historicalChart.series[0].update({
                        data: values
                    }, true); //True: Redraw chart

                    //Hide waiting screen
                    $(HISTORICAL_CHART_CARD_SELECTOR).waitMe("hide");
                });
            }

            /**
             * [Private]
             * Retrieves a certain number of value log data (in a specific order) for the current component
             * as a promise.
             *
             * @param numberLogs The number of logs to retrieve
             * @param descending The order in which the value logs should be retrieved. True results in descending
             * order, false in ascending order. By default, the logs are retrieved in ascending
             * order ([oldest log] --> ... --> [most recent log])
             * @returns A promise that passes the logs as a parameter
             */
            function retrieveComponentData(numberLogs, descending) {
                //Set default order
                if (descending) {
                    descending = 'desc';
                } else {
                    descending = 'asc'
                }

                //Initialize parameters for the server request
                var pageDetails = {
                    sort: 'date,' + descending,
                    size: numberLogs
                };

                //Perform the server request in order to retrieve the data
                return ComponentService.getValueLogs(COMPONENT_ID, COMPONENT_TYPE, pageDetails, vm.displayUnit).then(function (response) {
                        //Array that stores the finally formatted value logs
                        var finalValues = [];

                        var receivedLogs = response.data.content;

                        //Iterate over all received value logs
                        for (var i = 0; i < receivedLogs.length; i++) {
                            //Extract value and date for the current log and format them
                            var value = receivedLogs[i].value * 1;
                            var date = receivedLogs[i].date;
                            date = date.replace(/\s/g, "T");
                            date = dateToString(new Date(date));

                            //Create a (date, value) tuple and add it to the array
                            var tuple = [date, value];
                            finalValues.push(tuple);
                        }

                        //Return final value log array so that it is accessible in the promise
                        return finalValues;
                    }
                );
            }

            /**
             * [Private]
             * Initializes the live chart for displaying the most recent sensor values.
             */
            function initLiveChart() {
                /**
                 * Function that is called when the diagram loads something
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
                 * Function that is called when the diagram finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(LIVE_CHART_CARD_SELECTOR).waitMe("hide");
                }

                /**
                 * Function that checks whether the diagram is allowed to update its data.
                 * @returns {boolean} True, if the diagram may update; false otherwise
                 */
                function isUpdateable() {
                    return vm.deploymentState == 'DEPLOYED';
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
             * Initializes the historical chart in order to display all sensor values (up to a certain limit).
             */
            function initHistoricalChart() {
                //Create chart
                historicalChart = Highcharts.chart(historicalChartContainer, {
                    title: {
                        text: ''
                    },
                    chart: {
                        zoomType: 'xy'
                    },
                    series: [{
                        name: 'Value',
                        data: [],
                        showInLegend: false
                    }],
                    tooltip: {
                        valueDecimals: 2,
                        valuePrefix: '',
                        valueSuffix: ' ' + vm.displayUnit
                    },
                });

                //Initialize slider
                $("#" + historicalChartSlider).ionRangeSlider({
                    skin: "flat",
                    type: "single",
                    grid: true,
                    grid_num: 5,
                    grid_snap: false,
                    step: 1,
                    min: HISTORICAL_CHART_MIN_ELEMENTS,
                    max: HISTORICAL_CHART_MAX_ELEMENTS,
                    from: vm.historicalChartSettings.numberOfValues,
                    onFinish: function (data) {
                        //Update chart with new values
                        vm.historicalChartSettings.numberOfValues = data.from;
                        updateHistoricalChart();
                    }
                });

                //Watch value type and update chart on change
                $scope.$watch(
                    //Value to watch
                    function () {
                        return vm.historicalChartSettings.mostRecent;
                    },
                    //Callback
                    function () {
                        updateHistoricalChart();
                    }
                );

                //Populate the chart
                updateHistoricalChart();
            }

            /**
             * [Private]
             * Initializes the data structures that are required for the deployment parameters.
             */
            function initParameters() {
                //Retrieve all formal parameters for this component
                var requiredParams = componentDetails._embedded.adapter.parameters;

                //Iterate over all parameters
                for (var i = 0; i < requiredParams.length; i++) {
                    //Set empty default values for these parameters
                    var value = "";

                    if (requiredParams[i].type == "Switch") {
                        value = false;
                    }

                    //For each parameter, add a tuple (name, value) to the globally accessible parameter array
                    vm.parameterValues.push({
                        "name": requiredParams[i].name,
                        "value": value
                    });
                }
            }

            /**
             * [Private]
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
             * Hides the waiting screen for the deployment DOM container.
             */
            function hideDeploymentWaitingScreen() {
                $(DEPLOYMENT_CARD_SELECTOR).waitMe("hide");
            }

            /**
             * [Private]
             * Converts a javascript date object to a human-readable date string in the "dd.mm.yyyy hh:mm:ss" format.
             *
             * @param date The date object to convert
             * @returns The generated date string in the corresponding format
             */
            function dateToString(date) {
                //Retrieve all properties from the date object
                var year = date.getFullYear();
                var month = '' + (date.getMonth() + 1);
                var day = '' + date.getDate();
                var hours = '' + date.getHours();
                var minutes = '' + date.getMinutes();
                var seconds = '' + date.getSeconds();

                //Add a leading zero (if necessary) to all properties except the year
                var values = [day, month, hours, minutes, seconds];
                for (var i = 0; i < values.length; i++) {
                    if (values[i].length < 2) {
                        values[i] = '0' + values[i];
                    }
                }

                //Generate and return the date string
                return ([values[0], values[1], year].join('.')) +
                    ' ' + ([values[2], values[3], values[4]].join(':'));
            }

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                updateDeploymentState: updateDeploymentState,
                updateDeviceState: updateDeviceState,
                updateValueLogStats: updateValueLogStats,
                onDisplayUnitChange: onDisplayUnitChange,
                deploy: deploy,
                undeploy: undeploy,
                updateHistoricalChart: updateHistoricalChart
            });

        }]
);
