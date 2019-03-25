/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('ComponentDetailsController',
    ['$scope', '$rootScope', '$routeParams', '$interval', '$timeout', 'componentDetails', 'liveChartContainer', 'historicalChartContainer', 'historicalChartSlider', 'ComponentService', 'DeviceService', 'UnitService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, $interval, $timeout, componentDetails, liveChartContainer, historicalChartContainer, historicalChartSlider, ComponentService, DeviceService, UnitService, NotificationService) {
            //Interval with that the live value display is refreshed (seconds)
            const LIVE_REFRESH_DELAY_SECONDS = 15;

            //Maximum number of elements that may be displayed in the historical chart
            const LIVE_CHART_MAX_ELEMENTS = 20;

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

            //Contains data for the live progress
            vm.liveProgress = {
                progress: 0,
                delayTime: '0s'
            };

            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];

            //Settings for the historical chart
            vm.historicalChartSettings = {
                numberOfValues: HISTORICAL_CHART_INITIAL_ELEMENTS_NUMBER,
                mostRecent: true
            };

            //Hold the chart objects after the charts have been initialized
            var liveChart = null;
            var historicalChart = null;

            //Update interval for the live chart
            var liveChartInterval = null;

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

                //Initialize data retriever and dispatcher
                initLiveChartUpdate();

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
             *
             */
            function onDisplayUnitChange() {
                vm.displayUnit = vm.displayUnitInput;

                updateValueLogStats();
                updateHistoricalChart();

                cancelLiveChartUpdate();
                initLiveChart();
                initLiveChartUpdate();

                //var startUnit = COMPONENT_ADAPTER_UNIT;
                //var targetUnit = vm.displayUnitInput;

                //TODO
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

                console.log("Chart:");
                console.log(historicalChart);

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
             *  Displays a fake progress bar in the live chart in order to indicate when the live data is refreshed.
             *  The bar does not show a real progress, but just the time that is left until a certain time is passed.
             *
             * @param time The time (in seconds) during which the progress bar is supposed to be active.
             */
            function runLiveFakeProgress(time) {
                //Reset progress bar instantly
                vm.liveProgress.delayTime = '0s';
                vm.liveProgress.progress = 0;

                //Wait until the ui has updated
                $timeout(function () {
                    //Start the progress bar, the animation is achieved through a css transition time
                    vm.liveProgress.delayTime = time + 's';
                    vm.liveProgress.progress = 100;
                }, 10);
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
             * Initializes the update mechanics that are required in order to retrieve the most recent value logs
             * from the server and to update the live chart accordingly.
             */
            function initLiveChartUpdate() {
                //Counts the number of retrieved logs for ensuring the log limit
                var count = 0;

                //Get series from the live chart that is supposed to be updated
                var series = liveChart.series[0];

                var lastDate = null;

                //Define the update function that can be called on a regular basis
                var intervalFunction = function () {
                    //Do not update in case the sensor is not deployed
                    if (vm.deploymentState != 'DEPLOYED') {
                        return;
                    }

                    //Retrieve the most recent component data
                    retrieveComponentData(LIVE_CHART_MAX_ELEMENTS, true).then(function (values) {
                        //Abort of no data is available
                        if (values.length < 1) {
                            return;
                        }

                        /*
                         * The server requests returns a number of most recent logs; however, it is possible
                         * that some of the value logs are already displayed in the chart and do not need
                         * to be added again. Thus, filtering is needed, for which the variable lastDate
                         * is used to remember the date of the most recent log displayed in the chart.
                         */

                        //Check if there is already data in the live chart
                        if (lastDate == null) {
                            //No data in the live chart, thus add all received value logs
                            for (var i = values.length - 1; i >= 0; i--) {
                                series.addPoint(values[i], true, (++count >= LIVE_CHART_MAX_ELEMENTS));
                            }
                        } else {
                            /* There is already data in the live chart, so iterate over all value logs but
                             only take the ones from the array that occur before the log with lastDate */
                            var insert = false;
                            for (var i = values.length - 1; i >= 0; i--) {
                                //Try to find the log with lastdate in the array
                                if (values[i][0] == lastDate) {
                                    insert = true;
                                } else if (insert) {
                                    //This is a log before the log with lastedate
                                    series.addPoint(values[i], true, (++count >= LIVE_CHART_MAX_ELEMENTS));
                                }
                            }

                            /* In case the log with lastDate could not be found, this means that all data is relevant
                             and needs to be added to the chart */
                            if (!insert) {
                                for (var i = values.length - 1; i >= 0; i--) {
                                    series.addPoint(values[i], true, (++count >= LIVE_CHART_MAX_ELEMENTS));
                                }
                            }
                        }
                        //Update lastDate with the most recent log that was added to the chart
                        lastDate = values[0][0];
                    }).then(function () {
                        //Hide the waiting screen for the case it was displayed before
                        $(LIVE_CHART_CARD_SELECTOR).waitMe("hide");

                        //Visualize the time until the next refreshment
                        runLiveFakeProgress(LIVE_REFRESH_DELAY_SECONDS);
                    });
                };

                //Create an interval that calls the update function on a regular basis
                liveChartInterval = $interval(intervalFunction, 1000 * LIVE_REFRESH_DELAY_SECONDS);

                //Ensure that the interval is cancelled in case the user switches the page
                $scope.$on('$destroy', function () {
                    cancelLiveChartUpdate();
                });
            }

            /**
             * [Private]
             * Cancels the live chart update.
             */
            function cancelLiveChartUpdate() {
                if (liveChartInterval) {
                    $interval.cancel(liveChartInterval);
                }
            }

            /**
             * [Private]
             * Initializes the live chart in order to always display the most recent sensor values.
             */
            function initLiveChart() {
                //Set required global library options
                Highcharts.setOptions({
                    global: {
                        useUTC: false
                    }
                });

                //Destroy chart if already existing
                if (liveChart) {
                    liveChart.destroy();
                }

                //Create new chart with certain options
                liveChart = Highcharts.stockChart(liveChartContainer, {
                    title: {
                        text: ''
                    },
                    rangeSelector: {
                        enabled: false
                    },
                    xAxis: {
                        type: 'datetime',
                        labels: {
                            format: '{value}'
                        }
                    },
                    yAxis: {
                        opposite: false
                    },
                    navigator: {
                        xAxis: {
                            type: 'datetime',
                            labels: {
                                format: '{value}'
                            }
                        }
                    },
                    series: [{
                        name: 'Value',
                        data: []
                    }],
                    tooltip: {
                        valueDecimals: 2,
                        valuePrefix: '',
                        valueSuffix: ' ' + vm.displayUnit
                    }
                });

                //Set y-axis unit to currently displayed unit
                liveChart.yAxis[0].labelFormatter = function () {
                    return this.value + ' ' + vm.displayUnit;
                };

                //Show the waiting screen
                $(LIVE_CHART_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: 'Loading chart...',
                    bg: 'rgba(255,255,255,0.85)'
                });
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
