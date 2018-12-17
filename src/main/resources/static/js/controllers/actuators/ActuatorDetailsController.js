/* global app */

app.controller('ActuatorDetailsController',
        ['$scope', '$timeout', '$routeParams', '$controller', 'CrudService', 'ComponentService', 'actuatorDetails',
            function ($scope, $timeout, $routeParams, $controller, CrudService, ComponentService, actuatorDetails) {
                var vm = this;

                vm.loader = {};

                
                // public
                function update() { // update deployment status
                    vm.deployer.processing = true;
                    ComponentService.isDeployed(vm.actuatorDetailsCtrl.item._links.deploy.href)
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
                    ComponentService.deploy(vm.deployer.deploy, vm.actuatorDetailsCtrl.item._links.deploy.href)
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
                    ComponentService.undeploy(vm.actuatorDetailsCtrl.item._links.deploy.href)
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

                // actuator values
                var loadActuatorValues = function (tableState) {
                	
                    vm.loader.actuatorValues = true;
                    
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
                                            vm.loader.actuatorValues = false;
                                            
                                            tableState.pagination.numberOfPages = data.page.totalPages; //set the number of pages so the pagination can update
                                            
                                            vm.actuatorValues = {
                                                data: data._embedded.valueLogs
                                            };
                                        },
                                        function (response) {
                                            vm.loader.actuatorValues = false;
                                            vm.actuatorValues = {
                                                error: 'Could not load values',
                                                response: response
                                            };
                                        }
                                );
                            }, 500);
                };

                vm.loadActuatorValues = loadActuatorValues;
                
                vm.reloadValues = function () {
                	$scope.$broadcast('refreshActuatorValues');

                    $timeout(function () {
                        vm.reloadValues();
                    }, 10000);
                };
                vm.reloadValues();

                angular.extend(vm, {
                    deployer: {
                        deploy: {}, //May be used to pass parameters
                        update: update,
                        doDeploy: deploy,
                        doUndeploy: undeploy
                    }
                });

                // expose controller ($controller will auto-add to $scope)
                angular.extend(vm, {
                    actuatorDetailsCtrl: $controller('ItemDetailsController as actuatorDetailsCtrl',
                            {
                                $scope: $scope,
                                item: actuatorDetails
                            })
                });

                // VERY IMPORTANT LINE HERE
                update();
            }]);