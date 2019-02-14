/* global app */

app.controller('SensorDetailsController',
    ['$scope', '$rootScope', '$timeout', '$routeParams', '$controller', '$interval', 'CrudService', 'ComponentService', 'cfpLoadingBar',
        'NotificationService', 'sensorDetails',
        function ($scope, $rootScope, $timeout, $routeParams, $controller, $interval, CrudService, ComponentService, cfpLoadingBar,
                  NotificationService, sensorDetails) {

            var vm = this;

            vm.loader = {};
            vm.parameterValues = [];

            //Disable the loading bar for this page
            $rootScope.showLoading = false;
            $scope.$on('$locationChangeStart', function() {
                $rootScope.showLoading = true;
            });

            //private
            function initParameters() {
                var params = vm.sensorDetailsCtrl.item._embedded.adapter.parameters;
                for (var i = 0; i < params.length; i++) {
                    var value = "";

                    if (params[i].type == "Switch") {
                        value = false;
                    }

                    vm.parameterValues.push({
                        "name": params[i].name,
                        "value": value
                    });
                }
            }

            // public
            function update() { // update deployment status
                vm.deployer.processing = true;
                ComponentService.isDeployed(vm.sensorDetailsCtrl.item._links.deploy.href)
                    .then(
                        function (deployed) {
                            console.log('update: available, ' + deployed);
                            vm.deployer.processing = false;
                            vm.deployer.available = true;
                            vm.deployer.deployed = deployed;
                        },
                        function (response) {
                            console.log('update: unavailable');
                            vm.deployer.processing = false;
                            vm.deployer.available = false;
                        });
            }

            $scope.isCollapsedLog = false;

            function deploy() {
                vm.deployer.processing = true;
                ComponentService.deploy(vm.parameterValues, vm.sensorDetailsCtrl.item._links.deploy.href)
                    .then(
                        function (response) {
                            vm.deployer.processing = false;
                            vm.deployer.deployed = true;
                            vm.deployer.status = response.data;
                            vm.deployer.update();

                            //Notify user
                            NotificationService.notify('Successfully deployed.', 'success');
                        },
                        function (response) {
                            vm.deployer.processing = false;
                            vm.deployer.status = response.data;
                            vm.deployer.update();

                            //Notify user
                            NotificationService.notify('Unable to undeploy: ' + response.data.globalMessage, 'error');
                        });
            }

            function undeploy() {
                vm.deployer.processing = true;
                ComponentService.undeploy(vm.sensorDetailsCtrl.item._links.deploy.href)
                    .then(
                        function (response) {
                            vm.deployer.processing = false;
                            vm.deployer.deployed = false;
                            vm.deployer.status = response.data;
                            vm.deployer.update();

                            //Notify user
                            NotificationService.notify('Successfully undeployed.', 'success');
                        },
                        function (response) {
                            vm.deployer.processing = false;
                            vm.deployer.status = response.data;
                            vm.deployer.update();

                            //Notify user
                            NotificationService.notify('Unable to undeploy: ' + response.data.globalMessage, 'error');
                        });
            }

            // sensor values
            var loadSensorValues = function (tableState) {
                vm.loader.sensorValues = true;

                var pagination = tableState.pagination || {};

                var start = pagination.start || 0; // This is NOT the page number, but the index of item in the list that you want to use to display the table.
                var size = pagination.number || 10; // Number of entries showed per page.

                $timeout(
                    function () {
                        var query = 'findAllByIdref';
                        var params = {
                            idref: $routeParams.id,
                            sort: 'date,desc',
                            size: size,
                            page: Math.floor(start / size)
                        };

                        CrudService.searchPage('valueLogs', query, params).then(
                            function (data) {
                                console.log(data);
                                vm.loader.sensorValues = false;

                                tableState.pagination.numberOfPages = data.page.totalPages; //set the number of pages so the pagination can update

                                vm.sensorValues = {
                                    data: data._embedded.valueLogs
                                };
                            },
                            function (response) {
                                vm.loader.sensorValues = false;
                                vm.sensorValues = {
                                    error: 'Could not load values',
                                    response: response
                                };

                                //Notify user
                                NotificationService.notify('Unable to retrieve sensor values.', 'error');
                            }
                        );
                    }, 500);
            };

            vm.loadSensorValues = loadSensorValues;

            // sensor values
            var showCharts = function () {

                $timeout(
                    function () {
                        var query = 'findAllByIdref';
                        var params = {
                            idref: $routeParams.id,
                            sort: 'date,asc',
                            size: 10000
                        };

                        CrudService.searchPage('valueLogs', query, params).then(
                            function (data) {

                                var finalValues = new Array();
                                for (var i = 0; i < data._embedded.valueLogs.length; i++) {
                                    var values = new Array();
                                    var value = data._embedded.valueLogs[i].value * 1;

                                    var date = data._embedded.valueLogs[i].date;
                                    date = date.replace(/\s/g, "T");

                                    var parsedDate = new Date(date);
                                    values.push(parsedDate.toString(), value);

                                    finalValues.push(values);
                                }

                                vm.chartValues = finalValues;

                                Highcharts.chart('historicalValues', {
                                    title: {
                                        text: ''
                                    },

                                    chart: {
                                        zoomType: 'xy'
                                    },
                                    series: [{
                                        data: finalValues,
                                        showInLegend: false
                                    }]
                                });
                            }
                        );
                    }, 500);
            };

            vm.showCharts = showCharts;


            // sensor values
            var showLiveCharts = function () {

                $timeout(
                    function () {
                        var query = 'findAllByIdref';
                        var params = {
                            idref: $routeParams.id,
                            sort: 'date,desc',
                            size: 20
                        };

                        CrudService.searchPage('valueLogs', query, params).then(
                            function (data) {

                                var finalValues = new Array();
                                var i = data._embedded.valueLogs.length - 1;
                                for (i; i > 0; i--) {
                                    var values = new Array();
                                    var value = data._embedded.valueLogs[i].value * 1;

                                    var date = data._embedded.valueLogs[i].date;
                                    date = date.replace(/\s/g, "T");

                                    var parsedDate = new Date(date);
                                    values.push(parsedDate.toString(), value);

                                    finalValues.push(values);
                                }

                                vm.chartValues = finalValues;

                                Highcharts.stockChart('liveValues', {
                                    title: {
                                        text: ''
                                    },
                                    global: {
                                        useUTC: false
                                    },
                                    chart: {
                                        events: {
                                            load: function () {
                                                // set up the updating of the chart each second
                                                var series = this.series[0];
                                                var x = series.data.length;
                                                var timer = setInterval(function () {
                                                    //var x = (new Date()).getTime(), // current time

                                                    $timeout(
                                                        function () {
                                                            var query = 'findAllByIdref';
                                                            var params = {
                                                                idref: $routeParams.id,
                                                                sort: 'date,desc',
                                                                size: 1
                                                            };

                                                            CrudService.searchPage('valueLogs', query, params).then(
                                                                function (data) {
                                                                    //Sanity check
                                                                    if(data._embedded.valueLogs.length < 1){
                                                                        return;
                                                                    }

                                                                    var value = data._embedded.valueLogs[0].value * 1.0;
                                                                    var date = data._embedded.valueLogs[0].date;
                                                                    date = date.replace(/\s/g, "T");

                                                                    var parsedDate = new Date(date);

                                                                    series.addPoint([parsedDate.toString(), value], true, true);
                                                                }
                                                            );
                                                        }, 500);

                                                    //y = Math.round(Math.random() * 100);
                                                    //series.addPoint([x, y], true, true);
                                                    //x++;
                                                }, 2000);

                                                //Delete interval timer if angular route has changed
                                                $scope.$on('$locationChangeStart', function() {
                                                    window.clearInterval(timer);
                                                });
                                            }
                                        }
                                    },
                                    rangeSelector: {
                                        enabled: false,
                                    },
                                    xAxis: {
                                        type: 'datetime',
                                        labels: {
                                            format: '{value}',
                                        }
                                    },
                                    navigator: {

                                        xAxis: {
                                            type: 'datetime',
                                            labels: {
                                                format: '{value}',
                                            }
                                        },
                                    },
                                    series: [{
                                        data: finalValues
                                    }]
                                });
                            }
                        );
                    }, 500);
            };

            vm.showLiveCharts = showLiveCharts;

            vm.reloadValues = function () {
                $scope.$broadcast('refreshSensorValues');

                $timeout(function () {
                    vm.reloadValues();
                }, 10000);
            };
            vm.reloadValues();

            angular.extend(vm, {
                deployer: {
                    deploy: {},
                    update: update,
                    doDeploy: deploy,
                    doUndeploy: undeploy
                }
            });

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                sensorDetailsCtrl: $controller('ItemDetailsController as sensorDetailsCtrl',
                    {
                        $scope: $scope,
                        item: sensorDetails
                    })
            });

            initParameters();

            showCharts();
            showLiveCharts();

            // VERY IMPORTANT LINE HERE
            update();
        }]);