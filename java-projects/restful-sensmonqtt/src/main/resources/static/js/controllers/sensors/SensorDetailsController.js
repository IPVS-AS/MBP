/* global app */

app.controller('SensorDetailsController',
        ['$scope', '$timeout', '$routeParams', '$controller', 'ComponentService', 'sensorDetails',
            function ($scope, $timeout, $routeParams, $controller, ComponentService, sensorDetails) {
                var vm = this;

                vm.loader = {};


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
                    ComponentService.deploy(vm.deployer.deploy, vm.sensorDetailsCtrl.item._links.deploy.href)
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
                var loadSensorValues = function () {

                    vm.loader.sensorValues = true;
                    $timeout(
                            function () {
                                ComponentService.getValues(undefined, $routeParams.id).then(
                                        function (data) {
                                            vm.loader.sensorValues = false;
                                            vm.sensorValues = {
                                                data: data
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

                vm.reloadValues = function () {
                    loadSensorValues();

                    $timeout(function () {
                        vm.reloadValues();
                    }, 10000);
                };
                vm.reloadValues();

                angular.extend(vm, {
                    deployer: {
                        deploy: {
                            component: ComponentService.COMPONENT.SENSOR
                        },
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

                // VERY IMPORTANT LINE HERE
                update();
            }]);