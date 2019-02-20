app.controller('ComponentDetailsController',
    ['$scope', '$rootScope', '$routeParams', 'componentDetails', 'ComponentService', 'DeviceService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, componentDetails, ComponentService, DeviceService, NotificationService) {
            const LOADING_BOX_SELECTOR = ".loading-box";

            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = componentDetails.componentTypeName;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';

            var vm = this;
            vm.component = componentDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';

            //Disable the loading bar
            $rootScope.showLoading = false;
            $scope.$on('$locationChangeStart', function () {
                $rootScope.showLoading = true;
            });

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                initParameters();
                updateDeploymentState();
                updateDeviceState();
            })();

            /**
             * [Public]
             */
            function updateDeploymentState() {
                showLoadingThrobber("Determining sensor state...");
                vm.deploymentState = 'LOADING';

                ComponentService.getComponentState(COMPONENT_ID, COMPONENT_TYPE_URL).then(function (response) {
                    vm.deploymentState = response.data;
                }, function (response) {
                    vm.deploymentState = 'UNKNOWN';
                    NotificationService.notify('Could not load deployment state.', 'error');
                }).then(function () {
                    hideLoadingThrobber();
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
            function deploy() {
                showLoadingThrobber("Deploying...");
                vm.deploymentState = 'LOADING';

                ComponentService.deploy(vm.parameterValues, componentDetails._links.deploy.href)
                    .then(
                        function (response) {
                            if (!response.data.success) {
                                vm.deploymentState = 'UNKNOWN';
                                NotificationService.notify('Error during deployment: ' + response.data.globalMessage, 'error');
                                return;
                            }
                            vm.deploymentState = 'DEPLOYED';
                            NotificationService.notify('Sensor successfully deployed.', 'success');
                        },
                        function (response) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Deployment failed.', 'error');
                        }).then(function () {
                    hideLoadingThrobber();
                });
            }

            /**
             * [Public]
             */
            function undeploy() {
                showLoadingThrobber("Undeploying...");
                vm.deploymentState = 'LOADING';

                ComponentService.undeploy(componentDetails._links.deploy.href)
                    .then(
                        function (response) {
                            if (!response.data.success) {
                                vm.deploymentState = 'UNKNOWN';
                                NotificationService.notify('Error during undeployment: ' + response.data.globalMessage, 'error');
                                return;
                            }
                            vm.deploymentState = 'READY';
                            NotificationService.notify('Sensor successfully undeployed.', 'success');
                        },
                        function (response) {
                            vm.deploymentState = 'UNKNOWN';
                            NotificationService.notify('Undeployment failed.', 'error');
                        }).then(function () {
                    hideLoadingThrobber();
                });
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
            function showLoadingThrobber(text) {
                if (!text) {
                    text = 'Please wait...';
                }

                $(LOADING_BOX_SELECTOR).waitMe({
                    effect: 'bounce',
                    text: text,
                    bg: 'rgba(255,255,255,0.9)'
                });
            }

            /**
             * [Private]
             */
            function hideLoadingThrobber() {
                $(LOADING_BOX_SELECTOR).waitMe("hide");
            }

            angular.extend(vm, {
                updateDeploymentState: updateDeploymentState,
                updateDeviceState: updateDeviceState,
                deploy: deploy,
                undeploy: undeploy
            });

        }]
);
