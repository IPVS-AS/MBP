/* global app */


/**
 * Controller for the sensor list page.
 */
app.controller('TestingController',
    ['$scope', '$controller', '$interval', '$http', 'TestService', 'HttpService', 'testList', '$rootScope', 'addTest', 'deleteTest', 'ruleList', 'sensorList', '$q', 'ComponentService', 'FileReader', 'ENDPOINT_URI', 'NotificationService', 'accessControlPolicyList',
        function ($scope, $controller, $interval, $http, TestService, HttpService, testList, $rootScope, addTest, deleteTest, ruleList, sensorList, $q, ComponentService, FileReader, ENDPOINT_URI, NotificationService, accessControlPolicyList) {

            // Constant list of the sensor simulators, that can be included in the test
            const SIMULATOR_LIST = {
                TEMPERATURE: 'TESTING_TemperatureSensor',
                TEMPERATURE_PL: 'TESTING_TemperatureSensorPl',
                HUMIDITY: 'TESTING_HumiditySensor',
                HUMIDITY_PL: 'TESTING_HumiditySensorPl',
                ACCELERATION: 'TESTING_AccelerationSensor',
                ACCELERATION_PL: 'TESTING_AccelerationSensorPl',
                GPS: 'TESTING_GPSSensor',
                GPS_PL: 'TESTING_GPSSensorPl'
            };

            const TESTING_DEVICE = "TESTING_Device";
            const TESTING_ACTUATOR = "TESTING_Actuator";
            const RERUN_OPERATOR = "RERUN_OPERATOR";


            const vm = this;
            vm.ruleList = ruleList;
            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];
            //Settings objects that contains application settings for this page
            vm.useNewData = true;
            vm.testName = "";
            vm.rulesPDF = [];
            vm.availableSensors = [];
            vm.realSensorList = [];
            vm.test = "";
            vm.addSimulator = false;
            vm.addRealSensor = false;
            vm.executeRules = "true";


            (function initController() {
                vm.availableSensors = [];
                //Check if the test list was retrieved successfully
                if (testList == null) {
                    NotificationService.notify("Could not retrieve test list.", "error");
                }


                // Check the registration of the different testing components
                getTestDevice();
                checkActuatorReg();
                angular.forEach(SIMULATOR_LIST, function (value) {
                    checkSensorReg(String(value));
                });
                getRealSensors();
                // Check the registration of the rerun operator for test repetition
                getRerunOperator();


                $scope.availableSensors = vm.availableSensors;
                $scope.realSensorList = vm.realSensorList;

                //Interval for updating sensor states on a regular basis
                const interval = $interval(function () {
                    //    getTestDevice();
                    checkActuatorReg();
                    checkSensorReg();
                }, 5 * 60 * 1000);

                //Refresh test select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });

                //Cancel interval on route change
                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });
            })();


            /**
             * [Public]
             *
             * Performs a server request in order to start a test given by its id.
             *
             * @param testId
             */
            function executeTest(testId) {
                TestService.executeTest(testId)
            }

            /**
             * [Public]
             *
             * Performs a server request in order to stop a test given by its id.
             *
             * @param testId
             */
            function stopTest(testId) {
                TestService.stopTest(testId);
            }


            /**
             * [Public]
             *
             * Sends a server request to find out if a test report is available for the specific test.
             *
             * @param testId
             * @param testName
             */

            function checkReportExists(testId, testName) {
                TestService.pdfExists(testId).then(function (response) {
                    switch (response.data) {
                        case "true":
                            document.getElementById(testName).disabled = false;
                            break;
                        case "false":
                            document.getElementById(testName).disabled = true;
                            break;
                    }
                });
            }


            /**
             * [Public]
             *
             * Check if Test-Device is already registered or not.
             */
            function getTestDevice() {
                // go through every registered device and search for the test device
                HttpService.getAll('devices').then(function (deviceList) {
                    $scope.device = "NOT_REGISTERED";

                    angular.forEach(deviceList, function (device) {

                        if (device.name === TESTING_DEVICE) {
                            $scope.device = "REGISTERED";

                        } else {
                            $scope.device = "NOT_REGISTERED";

                        }
                    })
                });
            }

            /**
             * [Public]
             *
             * Check if the rerun operator for a test repetition is already registered.
             */
            function getRerunOperator() {
                // go through every registered adapter and search for the test rerun adapter
                HttpService.getAll('operators').then(function (adaptersList) {
                    $scope.rerunOperator = "NOT_REGISTERED";

                    angular.forEach(adaptersList, function (adapters) {

                        if (adapters.name === RERUN_OPERATOR) {
                            $scope.rerunOperator = "REGISTERED";

                        } else {

                            $scope.rerunOperator = "NOT_REGISTERED";
                        }
                    })

                });
            }

            /**
             * [Public]
             *
             * Server request for the registration of the Rerun Operator.
             */
            function registerRerunOperator() {
                TestService.registerRerunOperator().success(function success() {
                    getRerunOperator();
                    //Notify the user
                    NotificationService.notify('Entity successfully created.', 'success')
                }).catch(function onError() {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Rerun Operator.', 'error')
                });
            }

            /**
             * [Public]
             *
             * Register Test Device and update the registered status.
             */
            function registerTestDevice() {
                TestService.registerTestDevice().then(function success() {
                    getTestDevice();
                    //Notify the user
                    NotificationService.notify('Entity successfully created.', 'success')
                }).catch(function onError() {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Rerun Operator.', 'error')
                });

            }

            /**
             * [Public]
             *
             * Check if Actuator simulator is already registered or not.
             */
            function checkActuatorReg() {
                // go through every registered actuator and search for the testing actuator
                HttpService.getAll('actuators').then(function (actuatorList) {
                    $scope.testingActuator = "NOT_REGISTERED";
                    angular.forEach(actuatorList, function (actuator) {
                        if (actuator.name === TESTING_ACTUATOR) {
                            $scope.testingActuator = "REGISTERED";
                        } else {
                            $scope.testingActuator = "NOT_REGISTERED";
                        }
                    });
                });
            }


            /**
             * [Public]
             *
             * Register the Actuator-Simulator for the Test of IoT-Applications.
             */
            function registerTestingActuator() {
                TestService.registerTestActuator().then(function () {
                    checkActuatorReg();
                    //Notify the user
                    NotificationService.notify('Entity successfully created.', 'success')
                }).catch(function onError() {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Testing Actuator.', 'error')
                    checkActuatorReg();


                });

            }


            /**
             * [Public]
             *
             * Check if the Sensor-Simulator for the Test is registered.
             *
             * @param sensorSimulator to be checked
             */

            function checkSensorReg(sensorSimulator) {
                let registered = "NOT_REGISTERED";
                // go through every registered sensor and search for the sensor simulator
                HttpService.getAll('sensors').then(function (sensorList) {
                        if (sensorList.length > 0) {
                            angular.forEach(sensorList, function (sensor) {
                                if (sensor.name == sensorSimulator) {
                                    registered = "REGISTERED";
                                    vm.availableSensors.push(sensorSimulator);
                                }
                            })
                        }
                        // define the scope variable for the sensor simulators for the view
                        switch (sensorSimulator) {
                            case SIMULATOR_LIST.TEMPERATURE:
                                $scope.temp = registered;
                                break;
                            case SIMULATOR_LIST.TEMPERATURE_PL:
                                $scope.tempPl = registered;
                                break;
                            case SIMULATOR_LIST.HUMIDITY:
                                $scope.hum = registered;
                                break;
                            case SIMULATOR_LIST.HUMIDITY_PL:
                                $scope.humPl = registered;
                                break;
                            case SIMULATOR_LIST.GPS:
                                $scope.gps = registered;
                                break;
                            case SIMULATOR_LIST.GPS_PL:
                                $scope.gpsPl = registered;
                                break;
                            case SIMULATOR_LIST.ACCELERATION:
                                $scope.acc = registered;
                                break;
                            case SIMULATOR_LIST.ACCELERATION_PL:
                                $scope.accPl = registered;
                                break;
                        }
                    }
                );
            }

            /**
             * [Private]
             *
             * Creates a list of all real sensors that can be included in a test.
             */
            function getRealSensors() {
                angular.forEach(sensorList, function (sensor) {
                    let realSensor = true;
                    // Check if sensor is a sensor simulator
                    angular.forEach(SIMULATOR_LIST, function (simulator) {
                        if (simulator == sensor.name) {
                            realSensor = false;
                        }
                    });

                    if (sensor.name.includes("RERUN_")) {
                        realSensor = false;
                    }

                    // Add to list if real sensor
                    if (realSensor) {
                        vm.realSensorList.push(sensor);
                    }
                });
            }


            /**
             * [Public]
             *
             * Register the one dimensional Sensor-Simulator for the Test of IoT-Applications.
             */
            function registerOneDimSensor(sensor) {
                TestService.registerOneDimSensor(sensor).then(function () {
                    //Notify the user
                    NotificationService.notify('Entity successfully created.', 'success')
                    checkSensorReg(sensor);
                }, function () {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Sensor Simulator.', 'error')
                    checkSensorReg(sensor);
                });
                checkSensorReg(sensor);


            }

            /**
             * Register the three dimensional Sensor-Simulators for the Test of IoT-Applications.
             */
            function registerThreeDimSensor(sensor) {
                // TODO
            }

            /**
             * [Public]
             *
             * Manage the addition or removal of simulated sensors for creating a test.
             */
            function addSimulators() {
                const elem = document.getElementById("addSimulator");
                if (elem.value === "+") {
                    elem.value = "-";
                    vm.addSimulator = true;
                    document.getElementById("addSimulator").innerHTML = '';
                    document.getElementById("addSimulator").innerHTML = '<i class="material-icons">remove</i>';

                } else {
                    elem.value = "+";
                    vm.addSimulator = false;
                    document.getElementById("addSimulator").innerHTML = '';
                    document.getElementById("addSimulator").innerHTML = '<i class="material-icons">add</i>';
                }
            }


            /**
             * [Public]
             *
             * Manage the addition or removal of real sensors for creating a test.
             */
            function addRealSensor() {
                let elemReal = document.getElementById("addRealSensor");
                vm.selectedRealSensor = [];


                if (elemReal.value === '+') {
                    vm.addRealSensors = true;
                    elemReal.value = '-';
                    document.getElementById("addRealSensor").innerHTML = '';
                    document.getElementById("addRealSensor").innerHTML = '<i class="material-icons">remove</i>';


                } else {
                    elemReal.value = '+';
                    vm.addRealSensors = false;
                    document.getElementById("addRealSensor").innerHTML = '';
                    document.getElementById("addRealSensor").innerHTML = '<i class="material-icons">add</i>';
                }
            }


            /**
             * [Public]
             *
             * Sends a server request in order to open a window for downloading/open the specific test report
             *
             * @param testID ID of the test for which the test report should be opened.
             * @param endtimeUnix end time of the test to identify the specific report for the specific test
             */
            function downloadPDF(testID, endtimeUnix) {
                TestService.downloadReport(testID, endtimeUnix);
            }


            /**
             * [Public]
             *
             * Reference to the detailed page of a specific test.
             *
             * @param test for which the detailed page should be opened
             * @returns {*}
             */
            $scope.detailsLink = function (test) {
                if (test.id) {
                    return "view/testing-tool/" + test.id;
                }
                return "#";
            };


            /**
             * [Public]
             *
             * Shows an alert that asks the user if he is sure that he wants to delete a certain test.
             *
             * @param data A data object that contains the id of the test that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {

                const testId = data.id;
                let testName = "";

                //Determines the tests's name by checking the list
                for (let i = 0; i < testList.length; i++) {
                    if (testId === testList[i].id) {
                        testName = testList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete test',
                    type: 'warning',
                    html: "Are you sure you want to delete this test?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }


            /**
             * [Public]
             *
             * Sends a server request in order to edit the configurations of the test "useNewData",
             * so that the latest values of a specific test are reused in the new execution or not
             *
             * @param testId
             * @param useNewData
             */
            function editConfig(testId, useNewData) {
                let useNewDataConfig;
                if (useNewData === true) {
                    useNewDataConfig = "false";
                } else if (useNewData === false) {
                    useNewDataConfig = "true";
                }
                TestService.editConfig(testId, useNewDataConfig);

            }

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                testListCtrl: $controller('ItemListController as testListCtrl',
                    {
                        $scope: $scope,
                        list: testList
                    }),
                addTestCtrl: $controller('AddItemController as addTestCtrl',
                    {
                        $scope: $scope,
                        entity: 'test',
                        addItem: function (data) {
                            let selectedSensorsReal = vm.selectedRealSensor;
                            let parameterValuesReal = vm.parameterVal;


                            const newTestObject = TestService.getTestData(vm.selectedSensors, selectedSensorsReal, parameterValuesReal, vm.config, vm.rules, ruleList, vm.executeRules, data);


                            return addTest(newTestObject);

                        }
                    }),
                deleteTestCtrl: $controller('DeleteTestController as deleteTestCtrl', {
                    $scope: $scope,
                    deleteItem: deleteTest,
                    confirmDeletion: confirmDelete
                }),

                accessControlPolicyList: accessControlPolicyList,

                executeTest: executeTest,
                editConfig: editConfig,
                stopTest: stopTest,
                downloadPDF: downloadPDF,
                checkReportExists: checkReportExists,
                getTestDevice: getTestDevice,
                getRerunOperator: getRerunOperator,
                registerRerunOperator: registerRerunOperator,
                registerTestDevice: registerTestDevice,
                checkSensorReg: checkSensorReg,
                registerOneDimSensor: registerOneDimSensor,
                registerThreeDimSensor: registerThreeDimSensor,
                checkActuatorReg: checkActuatorReg,
                registerTestingActuator: registerTestingActuator,
                addSimulators: addSimulators,
                addRealSensor: addRealSensor

            });
            // $watch 'addTest' result and add to 'testList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addTestCtrl.result;
                },
                function () {
                    //Callback
                    const test = vm.addTestCtrl.result;

                    if (test) {
                        //Close modal on success
                        $("#addTestingModal").modal('toggle');
                        //Add sensor to sensor list
                        vm.testListCtrl.pushItem(test);

                    }
                }
            );


            //Watch deletion of tests and remove them from the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.deleteTestCtrl.result;
                },
                function () {
                    //Callback
                    const id = vm.deleteTestCtrl.result;
                    vm.testListCtrl.removeItem(id);
                }
            );

        }

    ]);

