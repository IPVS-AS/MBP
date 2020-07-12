/* global app */

/**
 * Controller for the component details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails', '$rootScope', '$routeParams', '$interval', 'UnitService', 'NotificationService', '$http', 'ENDPOINT_URI',
        function ($scope, controller, testingDetails, $rootScope, $routeParams, $interval, UnitService, NotificationService, $http, ENDPOINT_URI) {
            //Selectors that allow the selection of different ui cards
            const STATS_CARD_SELECTOR = ".stats-card";

            //Important properties of the currently considered component
            const COMPONENT_ID = $routeParams.id;
            const COMPONENT_TYPE = testingDetails;
            const COMPONENT_TYPE_URL = COMPONENT_TYPE + 's';
            const COMPONENT_ADAPTER_UNIT = testingDetails._embedded;


            //Initialization of variables that are used in the frontend by angular
            var vm = this;
            vm.test = testingDetails;
            vm.isLoading = false;
            vm.deploymentState = 'UNKNOWN';
            vm.deviceState = 'UNKNOWN';
            vm.displayUnit = COMPONENT_ADAPTER_UNIT;
            vm.displayUnitInput = COMPONENT_ADAPTER_UNIT;
            vm.ruleNames = "";
            vm.actionNames = "";
            vm.deviceNames = "";
            vm.parameterValues = [];



            $http.get(testingDetails._links.rules.href).success(function successCallback(responseRules) {
                for (let i = 0; i < responseRules._embedded.rules.length; i++) {
                    if (i === 0) {
                        vm.ruleNames = vm.ruleNames + responseRules._embedded.rules[i].name;
                        vm.actionNames = vm.actionNames + responseRules._embedded.rules[i].actionNames;
                    } else {
                        vm.ruleNames = vm.ruleNames + ", " + responseRules._embedded.rules[i].name;
                        for (let x = 0; x < responseRules._embedded.rules[i].actionNames.length; x++) {
                            if (vm.actionNames.includes(responseRules._embedded.rules[i].actionNames[x])) {

                            } else {
                                vm.actionNames = vm.actionNames + ", " + responseRules._embedded.rules[i].actionNames[x];
                            }
                        }
                    }
                }
            });


            $http.get(testingDetails._links.sensor.href).success(function successCallback(responseSensors) {

                for (let i = 0; i < responseSensors._embedded.sensors.length; i++) {
                    if (i === 0) {

                        vm.deviceNames = vm.deviceNames + responseSensors._embedded.sensors[i]._embedded.device.name;
                    } else {
                        vm.deviceNames = vm.deviceNames + ", " + responseSensors._embedded.sensors[i]._embedded.device.name;
                    }
                }

            });

            $http.get(ENDPOINT_URI + '/test-details/pdfExists/' + COMPONENT_ID).then(function (response) {
                if (response.data === "true") {
                    document.getElementById("pdfExists").innerHTML = testingDetails.endTestTime;
                    document.getElementById("downloadReport").style.display = "block";
                } else if (response.data === "false") {
                    document.getElementById("downloadReport").style.display = "none";
                    // document.getElementById("downloadReport").disabled = true;
                }
            });


            $http.get(ENDPOINT_URI + '/test-details/pdfList/' + COMPONENT_ID).then(function (response) {
                var newTestObject = {};
                angular.forEach(response.data, function(value, key) {
                    vm.parameterValues.push({
                        "link": value,
                        "date": key
                    });
                });

                newTestObject.pdfTable = vm.parameterValues;
                $scope.pdfTable = newTestObject.pdfTable;
                console.log(newTestObject);
            });

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Disable the loading bar
                $rootScope.showLoading = false;

                //Initialize parameters and retrieve states and stats
                initParameters();
                showPDF();
            })();


            /**
             * [Public]
             * Asks the user if he really wants to delete all value logs for the current component. If this is the case,
             * the deletion is executed by creating the corresponding server request.
             */
            function deleteValueLogs() {
                /**
                 * Executes the deletion of the value logs by performing the server request.
                 */
                function executeDeletion() {
                    ComponentService.deleteValueLogs(COMPONENT_ID, COMPONENT_TYPE).then(function (response) {
                        //Update historical chart and stats
                        $scope.historicalChartApi.updateChart();
                        $scope.valueLogStatsApi.updateStats();

                        NotificationService.notify("Value logs were deleted successfully.", "success");
                    }, function (response) {
                        NotificationService.notify("Could not delete value logs.", "error");
                    });
                }

                //Ask the user to confirm the deletion
                return Swal.fire({
                    title: 'Delete value data',
                    type: 'warning',
                    html: "Are you sure you want to delete all value data that has been recorded so far for this " +
                        "component? This action cannot be undone.",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                }).then(function (result) {
                    //Check if the user confirmed the deletion
                    if (result.value) {
                        executeDeletion();
                    }
                });
            }


            /**
             * [Private]
             * Initializes the value log stats display.
             */
            function initValueLogStats() {
                /**
                 * Function that is called when the value log stats display loads something
                 */
                function loadingStart() {
                    //Show waiting screen
                    $(STATS_CARD_SELECTOR).waitMe({
                        effect: 'bounce',
                        text: 'Loading value statistics...',
                        bg: 'rgba(255,255,255,0.85)'
                    });
                }

                /**
                 * Function that is called when the value log stats display finished loading
                 */
                function loadingFinish() {
                    //Hide the waiting screen for the case it was displayed before
                    $(STATS_CARD_SELECTOR).waitMe("hide");
                }

                /**
                 * Function that is used by the value log stats display to retrieve the statistics in a specific unit
                 * from the server.
                 */
                function getStats(unit) {
                    return ComponentService.getValueLogStats(COMPONENT_ID, COMPONENT_TYPE_URL, unit).then(function (response) {
                        //Success, pass statistics data
                        return response.data;
                    }, function (response) {
                        //Failure
                        NotificationService.notify('Could not load value log statistics.', 'error');
                        return {};
                    });
                }

                vm.valueLogStats = {
                    loadingStart: loadingStart,
                    loadingFinish: loadingFinish,
                    getStats: getStats
                };
            }


            /**
             * Performs a server request in order to start a test given by its id.
             */
            function executeTest() {
                $http.post(ENDPOINT_URI + '/test-details/test/' + COMPONENT_ID, COMPONENT_ID.toString()).success(function successCallback(responseTest) {
                    // If the test was completed successfully, enable the download Test Report Button
                    showPDF();
                });
            }

            /**
             * Performs a server request in order to stop a test given by its id.
             *
             * @param testId
             */
            function stopTest() {
                vm.http = $http.post(ENDPOINT_URI + '/test-details/test/stop/' + COMPONENT_ID, COMPONENT_ID.toString());
            }


            /**
             * Checks if a PDF exists. If yes, the download button is activated enabled.
             */
            function showPDF() {
                $http.get(ENDPOINT_URI + '/test-details/pdfExists/' + COMPONENT_ID).then(function (response) {
                    if (response.data === "true") {
                        document.getElementById("pdfExists").innerHTML = testingDetails.endTestTime;
                        document.getElementById("downloadReport").style.display = "block";
                    } else if (response.data === "false") {
                        document.getElementById("downloadReport").style.display = "none";

                    }
                });

            }


            /**
             * Sends a server request to open the test report of a specific test fiven by its id.
             *
             */
            function downloadPDF(path) {
                console.log(path);
                window.open('api/test-details/downloadPDF/' + path, '_blank');
            }


            /**
             * [Private]
             * Initializes the data structures that are required for the deployment parameters.
             */
            function initParameters() {
                //Retrieve all formal parameters for this component
                var requiredParams = testingDetails._embedded;


            }


            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                deleteValueLogs: deleteValueLogs,
                showPDF: showPDF,
                downloadPDF: downloadPDF,
                executeTest: executeTest,
                stopTest: stopTest
            });
        }]
);
