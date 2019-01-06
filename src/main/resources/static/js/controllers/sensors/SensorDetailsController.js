/* global app */

app.controller('SensorDetailsController',
        ['$scope', '$timeout', '$routeParams', '$controller', '$interval', 'CrudService', 'ComponentService', 'sensorDetails',
            function ($scope, $timeout, $routeParams, $controller, $interval, CrudService, ComponentService, sensorDetails) {
                var vm = this;

                vm.loader = {};

                vm.parameterValues = [];

                //private
                function initParameters(){
                    var params = vm.sensorDetailsCtrl.item._embedded.adapter.parameters;
                    for(var i = 0; i < params.length; i++){
                        var value = "";

                        if(params[i].type == "BOOLEAN"){
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
                                        vm.deployer.deploy.deployed = true;
                                        vm.deployer.deploy.success = 'Deployed successfully';
                                        vm.deployer.update();
                                    },
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deploy.errors = response;
                                        vm.deployer.deploy.errors.global = 'Error on deployment, please try again';
                                        vm.deployer.update();
                                    });
                }

                function undeploy() {
                    vm.deployer.processing = true;
                    vm.deployer.deploy.success = undefined;
                    vm.deployer.deploy.error = undefined;
                    ComponentService.undeploy(vm.sensorDetailsCtrl.item._links.deploy.href)
                            .then(
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deployed = false;
                                        vm.deployer.deploy.success = 'Undeployed successfully';
                                        vm.deployer.update();
                                    },
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deploy.errors = response;
                                        vm.deployer.deploy.errors.global = 'Error on undeployment, please try again';
                                        vm.deployer.update();
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
                                        	for (i; i != 0; i--) {
                                        		var values = new Array();
                                        		var value = data._embedded.valueLogs[i].value * 1;
                                        		
                                        		var date = data._embedded.valueLogs[i].date;
                                        		date = date.replace(/\s/g, "T");
                                        		
                                        		var parsedDate = new Date(date);
                                        		values.push(parsedDate.toString(), value);
                                        		
                                        		//values.push(data._embedded.valueLogs.length - i, value);
                                        		
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
                                                            setInterval(function () {
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
                                                                                	var value = 0.0;
                                                                                	value = data._embedded.valueLogs[0].value * 1.0;
                                                                                	
                                                                            		var date = data._embedded.valueLogs[i].date;
                                                                            		date = date.replace(/\s/g, "T");
                                                                            		
                                                                            		var parsedDate = new Date(date);

                                                                                	if (data._embedded.valueLogs.length > 0) {
                                                                                		series.addPoint([parsedDate.toString(), value], true, true);
                                                                                	}
                                                                                }
                                                                            );
                                                                        }, 500);
                                                            	
                                                                //y = Math.round(Math.random() * 100);
                                                                //series.addPoint([x, y], true, true);
                                                                //x++;
                                                            }, 2000);
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
                        deploy: {
                            parameters: vm.parameterValues
                        }, //May be used to pass parameters
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