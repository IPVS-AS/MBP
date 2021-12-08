/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingChartController',
    ['$scope', '$rootScope', '$routeParams', 'TestService', 'testingDetails', 'sensorList', '$interval', 'ComponentService', 'DeviceService', 'UnitService', 'NotificationService',
        function ($scope, $rootScope, $routeParams, TestService, testingDetails, sensorList, $interval, ComponentService, DeviceService, UnitService, NotificationService  ) {

            const vm = this;

            //Selectors that allow the selection of different ui cards
            const DEPLOYMENT_CARD_SELECTOR = ".deployment-card";
            const COMPONENT_ID = testingDetails.id;

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

                getPDFList();

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
                }, function () {
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
                    if(response.length > 0){
                        document.getElementById("ReuseSwitch").removeAttribute('disabled');
                    } else {
                        document.getElementById("ReuseSwitch").disabled = true;
                    }
                    $scope.pdfTable = response;
                });
            }

            /**
             * [Public]
             *  Starts a repetition of a specific test execution with the help of the report id and shows a waiting screen during
             * the start progress.
             *
             * @param reportId
             */
            function rerunTest(reportId) {
                //Show waiting screen
                vm.startTest = 'STARTING_TEST';

                //Perform request
                TestService.rerunTest(COMPONENT_ID, reportId).then(function (response) {
                    //Success
                    if (response === true) {
                        getPDFList();
                        vm.startTest = "END_TEST";
                        NotificationService.notify('Test completed successfully.', 'success');
                    }
                }, function () {
                    //Handle failure
                    vm.startTest = "ERROR_TEST";
                    NotificationService.notify('Error during the test.', 'error');
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
                    if (response === true) {
                        getPDFList();
                        vm.startTest = "END_TEST";
                        NotificationService.notify('Test completed successfully.', 'success');
                    }
                }, function () {
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

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                startComponent: startComponent,
                stopComponent: stopComponent,
                rerunTest: rerunTest
            });
        }]);