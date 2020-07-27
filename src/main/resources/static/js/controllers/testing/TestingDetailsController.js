/* global app */

/**
 * Controller for the test details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails', '$rootScope', '$routeParams', '$interval', 'UnitService', 'NotificationService', '$http', 'ENDPOINT_URI', 'ruleList',
        function ($scope, controller, testingDetails, $rootScope, $routeParams, $interval, UnitService, NotificationService, $http, ENDPOINT_URI, ruleList) {

            //Test ID
            const COMPONENT_ID = $routeParams.id;


            //Initialization of variables that are used in the frontend by angular
            var vm = this;
            vm.ruleList = ruleList;
            vm.test = testingDetails;
            vm.ruleNames = "";
            vm.actionNames = "";
            vm.deviceNames = "";
            vm.rules = [];
            vm.executeRules = true;

            vm.sensorType = testingDetails.type;


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

                getPDFList();
                getTestSensors();
                getTestRules();
                getConfig();


                //Refresh test select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });

            })();


            /**
             * Get the Configuration of the test to display them on the edit Test Modal
             */
            function getConfig() {
                var event;
                var anomaly;
                var useNewData;
                vm.rules = [];

                //TODO: Get further informations like room, coordinates, etc. 
                for (let i = 0; i < testingDetails.config.length; i++) {
                    if (testingDetails.config[i].name === "event") {
                        event = testingDetails.config[i].value;
                    } else if (testingDetails.config[i].name === "anomaly") {
                        anomaly = testingDetails.config[i].value;
                    }else if (testingDetails.config[i].name === "useNewData") {
                        useNewData = !testingDetails.config[i].value;
                        console.log(testingDetails.config[i].value + " "+  !testingDetails.config[i].value);

                    }
                }

                $rootScope.config = {
                    event: event,
                    anomaly: anomaly,
                    useNewData: useNewData
                };

                $http.get(testingDetails._links.rules.href).success(function successCallback(responseRules) {
                    for (let i = 0; i < responseRules._embedded.rules.length; i++) {
                        vm.rules.push(responseRules._embedded.rules[i]._links.self.href);
                    }
                    $rootScope.selectedRules = {rules: vm.rules};
                });


                if (testingDetails.triggerRules === true) {
                    vm.executeRules = "true";
                } else {
                    vm.executeRules = "false";
                }






            }


            /**
             * [Private]
             * Creates a server request to get all rules to be observed during the Test of the IoT-Application.
             */
            function getTestRules() {
                $http.get(ENDPOINT_URI + "/test-details/ruleList/" + COMPONENT_ID).success(function (response) {
                    $scope.ruleList = response;

                });

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
            }

            /**
             * [Private]
             * Creates a server request to get all sensors regarding to the Test of the IoT-Application.
             */
            function getTestSensors() {
                $http.get(testingDetails._links.sensor.href).success(function successCallback(responseSensors) {
                    for (let i = 0; i < responseSensors._embedded.sensors.length; i++) {
                        if (i === 0) {

                            vm.deviceNames = vm.deviceNames + responseSensors._embedded.sensors[i]._embedded.device.name;
                        } else {
                            vm.deviceNames = vm.deviceNames + ", " + responseSensors._embedded.sensors[i]._embedded.device.name;
                        }
                    }
                });
            }

            /**
             * [Public]
             * Creates a server request to get a list of all generated Test Reports regarding to the Test of the IoT-Application.
             */
            function getPDFList() {
                $http.get(ENDPOINT_URI + '/test-details/pdfList/' + COMPONENT_ID).then(function (response) {
                    var newTestObject = {};
                    vm.parameterValues = [];

                    if (Object.keys(response.data).length > 0) {
                        angular.forEach(response.data, function (value, key) {
                            vm.parameterValues.push({
                                "date": key,
                                "path": value
                            });
                        });
                        newTestObject.pdfTable = vm.parameterValues;
                        $scope.pdfTable = newTestObject.pdfTable;
                    } else {
                        document.getElementById("pdfTable").innerHTML = "There is no Test Report for this Test yet.";
                    }
                });
            }

            /**
             * [Public]
             * Performs a server request in order to start a test given by its id.
             */
            function executeTest() {
                $http.post(ENDPOINT_URI + '/test-details/test/' + COMPONENT_ID, COMPONENT_ID.toString()).success(function successCallback(responseTest) {
                    // If the test was completed successfully, enable the download Test Report Button
                    getPDFList();
                });
            }

            /**
             * [Public]
             * Performs a server request in order to stop a test given by its id.
             */
            function stopTest() {
                vm.http = $http.post(ENDPOINT_URI + '/test-details/test/stop/' + COMPONENT_ID, COMPONENT_ID.toString());
            }


            /**
             * [Public]
             * Sends a server request to open the test report of a specific test fiven by its id.
             */
            function downloadPDF(path) {
                window.open('api/test-details/downloadPDF/' + path, '_blank');
            }


            /**
             * Sends a server request in order to edit the configurations of the test "useNewData",
             * so that the latest values of a specific test are reused in the new execution or not
             *
             * @param testId
             * @param useNewData
             */
            function editConfig(useNewData) {
                if (useNewData === true) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + COMPONENT_ID, "false").then(function success(response) {
                        $scope.erfolgreich = response.success;
                    });
                } else if (useNewData === false) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + COMPONENT_ID, "true").then(function success(response) {
                        $scope.erfolgreich = response.success;
                    });
                }
            }


            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, {
                downloadPDF: downloadPDF,
                executeTest: executeTest,
                stopTest: stopTest,
                getPDFList: getPDFList,
                editConfig: editConfig
            });
        }]
);
