app.controller('ComponentDetailsController',
    ['$scope', '$rootScope', '$routeParams', '$interval', 'componentDetails', 'liveChartContainer', 'historicalChartContainer', 'ComponentService', 'CrudService', 'DeviceService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, $interval, componentDetails, liveChartContainer, historicalChartContainer, ComponentService, CrudService, DeviceService, NotificationService) {
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const STATS_CARD_SELECTOR = ".stats-card";

            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = componentDetails.componentTypeName;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';

            var vm = this;
            vm.component = componentDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';
            vm.valueLogStats = null;

            var liveChart = null;
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

                //Initialize data retriever and dispatcher
                retrieveAndDispatchData();

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
             * [Public]
             */
            function updateDeploymentState(noThrobber) {
                if (!noThrobber) {
                    showDeploymentThrobber("Retrieving component state...");
                }

                ComponentService.getComponentState(COMPONENT_ID, COMPONENT_TYPE_URL).then(function (response) {
                    vm.deploymentState = response.data;
                }, function (response) {
                    vm.deploymentState = 'UNKNOWN';
                    NotificationService.notify('Could not retrieve deployment state.', 'error');
                }).then(function () {
                    hideDeploymentThrobber();
                });
            }

            /**
             * [Public]
             */
            function updateDeviceState() {
                vm.deviceState = 'LOADING';

                DeviceService.getDeviceState(componentDetails._embedded.device.id).then(function (response) {
                    vm.deviceState = response.data;
                }, function (response) {
                    vm.deviceState = 'UNKNOWN';
                    NotificationService.notify('Could not load device state.', 'error');
                });
            }

            /**
             * [Public]
             */
            function updateValueLogStats(noThrobber) {
                if (!noThrobber) {
                    $(STATS_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: "Loading overview...",
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                ComponentService.getValueLogStats(COMPONENT_ID, COMPONENT_TYPE_URL).then(function (response) {
                    vm.valueLogStats = response.data;
                }, function (response) {
                    NotificationService.notify('Could not load value log statistics.', 'error');
                }).then(function () {
                    $(STATS_CARD_SELECTOR).waitMe("hide");
                });
            }

            /**
             * [Public]
             */
            function deploy() {
                showDeploymentThrobber("Deploying...");

                ComponentService.deploy(vm.parameterValues, componentDetails._links.deploy.href)
                    .then(
                        function (response) {
                            if (!response.data.success) {
                                vm.deploymentState = 'UNKNOWN';
                                NotificationService.notify('Error during deployment: ' + response.data.globalMessage, 'error');
                                return;
                            }
                            vm.deploymentState = 'DEPLOYED';
                            NotificationService.notify('Component deployed successfully.', 'success');
                        },
                        function (response) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Deployment failed.', 'error');
                        }).then(function () {
                    hideDeploymentThrobber();
                });
            }

            /**
             * [Public]
             */
            function undeploy() {
                showDeploymentThrobber("Undeploying...");

                ComponentService.undeploy(componentDetails._links.deploy.href)
                    .then(
                        function (response) {
                            if (!response.data.success) {
                                vm.deploymentState = 'UNKNOWN';
                                NotificationService.notify('Error during undeployment: ' + response.data.globalMessage, 'error');
                                return;
                            }
                            vm.deploymentState = 'READY';
                            NotificationService.notify('Component undeployed successfully.', 'success');
                        },
                        function (response) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Undeployment failed.', 'error');
                        }).then(function () {
                    hideDeploymentThrobber();
                });
            }

            /**
             * [Public]
             */
            function updateHistoricalChart() {
                const MAX_ELEMENTS = 1000;

                if (historicalChart == null) {
                    console.error("The historical chart has not been initialized yet.");
                    return;
                }

                var chartDiv = $('#' + historicalChartContainer);

                chartDiv.waitMe({
                    effect: 'bounce',
                    text: 'Updating chart...',
                    bg: 'rgba(255,255,255,0.85)'
                });

                retrieveComponentData(MAX_ELEMENTS).then(function (values) {
                    historicalChart.series[0].update({
                        data: values
                    }, true); //True: Redraw
                    chartDiv.waitMe("hide");
                });
            }

            /**
             * [Private]
             *
             * @param numberElements
             * @param order (asc/desc)
             * @returns {*}
             */
            function retrieveComponentData(numberElements, order) {
                if (!order) {
                    order = 'asc';
                }

                var params = {
                    idref: COMPONENT_ID,
                    sort: 'date,' + order,
                    size: numberElements
                };

                return CrudService.searchPage('valueLogs', 'findAllByIdref', params).then(function (data) {
                        var finalValues = [];

                        for (var i = 0; i < data._embedded.valueLogs.length; i++) {
                            var value = data._embedded.valueLogs[i].value * 1;
                            var date = data._embedded.valueLogs[i].date;
                            date = date.replace(/\s/g, "T");
                            date = new Date(date).toString();

                            var tuple = [date, value];
                            finalValues.push(tuple);
                        }

                        return finalValues;
                    }
                );
            }

            /**
             * [Private]
             */
            function retrieveAndDispatchData() {
                var lastDate = null;

                var intervalFunction = function () {
                    if (vm.deploymentState != 'DEPLOYED') {
                        return;
                    }

                    retrieveComponentData(20, 'desc').then(function (values) {
                        if (values.length < 1) {
                            return;
                        }

                        if (lastDate == null) {
                            liveChart.series[0].update({data: values.reverse()}, true);
                            lastDate = values[0][0];
                        } else {
                            var insert = false;
                            for (var i = values.length - 1; i >= 0; i--) {
                                if (values[i][0] == lastDate) {
                                    insert = true;
                                } else if (insert) {
                                    liveChart.series[0].addPoint(values[i], true, true);
                                }
                            }

                            if (!insert) {
                                for (var i = values.length - 1; i >= 0; i--) {
                                    liveChart.series[0].addPoint(values[i], true, true);
                                }
                            }

                            lastDate = values[0][0];
                        }
                    }).then(function () {
                        $('#' + liveChartContainer).waitMe("hide");
                    });
                };

                var interval = $interval(intervalFunction, 1000 * 15);

                $scope.$on('$destroy', function () {
                    if (interval) {
                        $interval.cancel(interval);
                    }
                });
            }

            /**
             * [Private]
             */
            function initLiveChart() {
                liveChart = Highcharts.stockChart(liveChartContainer, {
                    title: {
                        text: ''
                    },
                    global: {
                        useUTC: false
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
                    }]
                });

                $('#' + liveChartContainer).waitMe({
                    effect: 'bounce',
                    text: 'Loading chart...',
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             */
            function initHistoricalChart() {
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
                    }]
                });
                updateHistoricalChart();
            }

            /**
             * [Private]
             */
            function initParameters() {
                vm.parameterValues = [];
                var requiredParams = componentDetails._embedded.adapter.parameters;
                for (var i = 0; i < requiredParams.length; i++) {
                    var value = "";

                    if (requiredParams[i].type == "Switch") {
                        value = false;
                    }

                    vm.parameterValues.push({
                        "name": requiredParams.name,
                        "value": value
                    });
                }
            }

            /**
             * [Private]
             */
            function showDeploymentThrobber(text) {
                if (!text) {
                    text = 'Please wait...';
                }

                $(DEPLOYMENT_CARD_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.85)'
                });
            }

            /**
             * [Private]
             */
            function hideDeploymentThrobber() {
                $(DEPLOYMENT_CARD_SELECTOR).waitMe("hide");
            }

            angular.extend(vm, {
                updateDeploymentState: updateDeploymentState,
                updateDeviceState: updateDeviceState,
                updateValueLogStats: updateValueLogStats,
                deploy: deploy,
                undeploy: undeploy,
                updateHistoricalChart: updateHistoricalChart
            });

        }]
);
