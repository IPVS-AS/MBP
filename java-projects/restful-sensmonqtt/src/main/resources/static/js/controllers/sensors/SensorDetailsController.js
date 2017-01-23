/* global app */

app.controller('SensorDetailsController',
        ['$scope', '$timeout', '$routeParams', '$controller', 'ComponentService', 'sensorDetails', 'values',
            function ($scope, $timeout, $routeParams, $controller, ComponentService, sensorDetails, values) {
                var vm = this;

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

                function deploy() {
                    vm.deployer.processing = true;
                    ComponentService.deploy(vm.deployer.deploy, vm.sensorDetailsCtrl.item._links.deploy.href)
                            .then(
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deploy.deployed = true;
                                        vm.deployer.deploy.success = 'Deployed successfully';
                                    },
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deploy.errors = response;
                                        vm.deployer.deploy.errors.global = 'Error on deployment, please try again';
                                    });
                }

                function undeploy() {
                    vm.deployer.processing = true;
                    ComponentService.undeploy(vm.sensorDetailsCtrl.item._links.deploy.href)
                            .then(
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deployed = false;
                                        vm.deployer.undeploy.success = 'Undeployed successfully';
                                        
                                    },
                                    function (response) {
                                        vm.deployer.processing = false;
                                        vm.deployer.deploy.errors = response;
                                        vm.deployer.deploy.errors.global = 'Error on undeployment, please try again';
                                    });
                }

                angular.extend(vm, {
                    deployer: {
                        update: update,
                        deploy: deploy,
                        undeploy: undeploy
                    },
                    values: values
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