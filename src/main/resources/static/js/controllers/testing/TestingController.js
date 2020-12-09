/* global app */


/**
 * Controller for the sensor list page.
 */
app.controller('TestingController',
    ['$scope', '$controller', '$interval', '$http', 'TestService','testList', '$rootScope', 'addTest', 'deleteTest', 'ruleList', 'sensorList', '$q', 'ComponentService', 'FileReader', 'ENDPOINT_URI', 'NotificationService',
        function ($scope, $controller, $interval, $http, TestService,testList, $rootScope, addTest, deleteTest, ruleList, sensorList, $q, ComponentService, FileReader, ENDPOINT_URI, NotificationService) {

            // Constant list of the sensor simulators, that can be included in the test
            const SIMULATOR_LIST = {
                TEMPERATURE: 'TestingTemperatureSensor',
                TEMPERATURE_PL: 'TestingTemperatureSensorPl',
                HUMIDITY: 'TestingHumiditySensor',
                HUMIDITY_PL: 'TestingHumiditySensorPl',
                ACCELERATION: 'TestingAccelerationSensor',
                ACCELERATION_PL: 'TestingAccelerationSensorPl',
                GPS: 'TestingGPSSensor',
                GPS_PL: 'TestingGPSSensorPl'
            };

            const TESTING_DEVICE = "TestingDevice";

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


            /**
             * Initializing function, sets up basic things.
             */
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
                    getTestDevice();
                    //   checkActuatorReg();
                    //  checkSensorReg();
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
             * Performs a server request in order to start a test given by its id.
             *
             * @param testId
             */
            function executeTest(testId) {
                TestService.executeTest(testId)
            }

            /**
             * [Public]
             * Performs a server request in order to stop a test given by its id.
             *
             * @param testId
             */
            function stopTest(testId) {
               TestService.stopTest(testId);
            }


            /**
             * [Public]
             * Sends a server request to find out if a test report is available for the specific test.
             *
             * @param testId
             * @param testName
             */
            function refreshTestEntry(testId, testName) {

                $http.get(ENDPOINT_URI + '/test-details/pdfExists/' + testId).then(function (response) {

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
             * Check if Test-Device is already registered or not.
             */
            function getTestDevice() {
                $http.get(ENDPOINT_URI + '/devices/search/findAll').success(function (response) {
                    $scope.device = 'LOADING';
                    $scope.device = "NOT_REGISTERED";

                    // go through every registered device and search for the test device
                    angular.forEach(response._embedded.devices, function (value) {
                        if (value.name === TESTING_DEVICE) {
                            $scope.device = "REGISTERED";
                        }
                    });
                });
            }

            /**
             * [Public]
             * Check if the rerun operator for a test repetition is already registered.
             */
            function getRerunOperator() {
                $http.get(ENDPOINT_URI + '/adapters/search/findAll').success(function (response) {

                    $scope.rerunOperator = 'LOADING';
                    $scope.rerunOperator = "NOT_REGISTERED";

                    // Go through the list of every registered adapters
                    angular.forEach(response._embedded.adapters, function (value) {
                        if (value.name === "RERUN_OPERATOR") {
                            $scope.rerunOperator = "REGISTERED";
                        }
                    });
                });
            }

            /**
             * [Public]
             * Server request for the registration of the Rerun Operator.
             */
            function registerRerunOperator() {
                $http.post(ENDPOINT_URI + '/test-details/addRerunOperator').success(function success() {
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
             * Register Test Device and update the registered status.
             */
            function registerTestDevice() {
                $http.post(ENDPOINT_URI + '/test-details/registerTestDevice').success(function success() {
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
             * Check if Actuator simulator is already registered or not.
             */
            function checkActuatorReg() {
                $http.get(ENDPOINT_URI + '/actuators/search/findAll').success(function (response) {
                    let registered = "NOT_REGISTERED";
                    angular.forEach(response._embedded.actuators, function (value) {
                        if (value.name === "TestingActuator") {
                            registered = "REGISTERED";
                        }
                    });
                    $scope.testingActuator = registered;
                });
            }


            /**
             * [Public]
             * Register the Actuator-Simulator for the Test of IoT-Applications.
             */
            function registerTestingActuator() {
                $http.post(ENDPOINT_URI + '/test-details/registerTestDevice').then(function (response) {
                    getTestDevice();
                    //Notify the user
                    NotificationService.notify(response.getBody(), 'success')
                }).catch(function onError() {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Testing Actuator.', 'error')
                });

            }


            /**
             * [Public]
             * Check if the Sensor-Simulator for the Test is registered.
             *
             * @param sensor to be checked
             */
            function checkSensorReg(sensor) {
                let registered = "NOT_REGISTERED";

                angular.forEach(sensorList, function (value) {
                    if (value.name == sensor) {
                        registered = "REGISTERED";
                        vm.availableSensors.push(sensor);
                    }
                });

                // define the scope variable for the sensor simulators for the view
                switch (sensor) {
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


            /**
             * [Private]
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

                    // Add to list if real sensor
                    if (realSensor) {
                        vm.realSensorList.push(sensor);
                    }
                });
            }


            /**
             * [Public]
             * Register the one dimensional Sensor-Simulator for the Test of IoT-Applications.
             */
            function registerOneDimSensor(sensor) {
                $http.post(ENDPOINT_URI + '/test-details/registerSensorSimulator', sensor).success(function (response) {
                    checkSensorReg(sensor);
                    //Notify the user
                    NotificationService.notify(response.getBody(), 'success')
                }).catch(function onError() {
                    //Notify the user
                    NotificationService.notify('Error during creation of the Sensor Simulator.', 'error')
                });

            }

            /**
             * Register the three dimensional Sensor-Simulators for the Test of IoT-Applications.
             */
            function registerThreeDimSensor(sensor) {
                // TODO
            }

            /**
             * [Public]
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
             * Manage the addition or removal of real sensors for creating a test.
             *
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
             * @param endtimeUnix endtime of the test to identify the specific report for the specific test
             */
            function downloadPDF(testID, endtimeUnix) {
                window.open('api/test-details/downloadPDF/' + testID + "_" + endtimeUnix, '_blank');
            }


            /**
             * [Public]
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
             * Retrieve authorization code for the device from the OAuth Authorization server.
             */
            function getDeviceCode() {
                fetch(location.origin + '/MBP/oauth/authorize?client_id=device-client&response_type=code&scope=write', {
                    headers: {
                        // Basic http authentication with username "device-client" and the according password from MBP
                        'Authorization': 'Basic ZGV2aWNlLWNsaWVudDpkZXZpY2U='
                    }
                }).then(function (response) {
                    let chars = response.url.split('?');
                    let code = chars[1].split('=');
                    vm.parameterValues.push({
                        "name": "device_code",
                        "value": code[1]
                    });
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
                if (useNewData === true) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "false").then(function success(response) {
                        $scope.erfolgreich = response.success;
                    });
                } else if (useNewData === false) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "true").then(function success(response) {
                        $scope.erfolgreich = response.success;
                    });
                }
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
                        addItem: function (data) {
                            let checkSimSensor;
                            let checkRealSensor;
                            const newTestObject = {};

                            newTestObject.config = [];
                            newTestObject.type = [];


                            try {

                                checkRealSensor = false;
                                checkSimSensor = false;


                                if (!angular.isUndefined(vm.selectedRealSensor)) {
                                    if (!angular.isUndefined(vm.parameterVal)) {
                                        for (let x = 0; x < vm.selectedRealSensor.length; x++) {
                                            newTestObject.type.push(vm.selectedRealSensor[x].name);
                                        }

                                        checkRealSensor = true;
                                        angular.forEach(vm.parameterVal, function (parameters, key) {
                                            for (let i = 0; i < vm.selectedRealSensor.length; i++) {
                                                if (vm.selectedRealSensor[i].name === key) {
                                                    vm.parameterValues = [];
                                                    vm.parameterValues.push({
                                                        "name": "ConfigName",
                                                        "value": vm.selectedRealSensor[i].name
                                                    });

                                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                                    const requiredParams = vm.selectedRealSensor[i]._embedded.adapter.parameters;


                                                    //Iterate over all parameters
                                                    for (let i = 0; i < requiredParams.length; i++) {
                                                        //Set empty default values for these parameters
                                                        let value = "";

                                                        if (requiredParams[i].type === "Switch") {
                                                            value = true;
                                                        }
                                                        if (requiredParams[i].name === "device_code") {
                                                            console.log("Requesting code for required parameter device_code.");
                                                            value = getDeviceCode();
                                                            continue;
                                                        }

                                                        //For each parameter, add a tuple (name, value) to the globally accessible parameter array
                                                        vm.parameterValues.push({
                                                            "name": requiredParams[i].name,
                                                            "value": parameters[i]
                                                        });
                                                    }
                                                    newTestObject.config.push(vm.parameterValues);
                                                }
                                            }
                                        });
                                        newTestObject.config.push([{
                                            "name": "ConfigRealSensors",
                                            "value": vm.parameterVal
                                        }])
                                    }
                                }


                                //Extend request parameters for routines and parameters
                                let parameters;
                                // random values Angle and Axis for the GPS-Sensor
                                const randomAngle = Math.floor((Math.random() * 361));
                                const randomAxis = Math.floor((Math.random() * 3));


                                // random values for the direction of the outlier and movement for the acceleration Sensor
                                const directionOutlier = Math.floor(Math.random() * 6);
                                const directionMovement = Math.floor(Math.random() * 6);

                                if (!angular.isUndefined(vm.selectedSensors)) {
                                    checkSimSensor = true;
                                    for (let z = 0; z < vm.selectedSensors.length; z++) {
                                        newTestObject.type.push(vm.selectedSensors[z]);
                                    }
                                    if (vm.selectedSensors.includes('TestingTemperatureSensor')) {

                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": 'TestingTemperatureSensor'
                                        });
                                        if (vm.config.eventTemp === '3' || vm.config.eventTemp === '4' || vm.config.eventTemp === '5' || vm.config.eventTemp === '6') {

                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventTemp)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});

                                        } else {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventTemp)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "room", "value": vm.config.roomTemp});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyTemp)
                                            });
                                        }
                                        newTestObject.config.push(vm.parameterValues);


                                    }

                                    if (vm.selectedSensors.includes('TestingHumiditySensor')) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": 'TestingHumiditySensor'
                                        });
                                        if (vm.config.eventHum === '3' || vm.config.eventHum === '4' || vm.config.eventHum === '5' || vm.config.eventHum === '6') {

                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventHum)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});

                                        } else {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventHum)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "room", "value": vm.config.roomHum});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyHum)
                                            });
                                        }

                                        newTestObject.config.push(vm.parameterValues);
                                    }

                                    if (vm.selectedSensors.includes('TestingTemperatureSensorPl')) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": 'TestingTemperatureSensorPl'
                                        });
                                        if (vm.config.eventTempPl === '3' || vm.config.eventTempPl === '4' || vm.config.eventTempPl === '5' || vm.config.eventTempPl === '6') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventTempPl)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        } else {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.event)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                            vm.parameterValues.push({"name": "room", "value": vm.config.roomTempPl});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyTempPl)
                                            });

                                        }
                                        newTestObject.config.push(vm.parameterValues);
                                    }

                                    if (vm.selectedSensors.includes(SIMULATOR_LIST.HUMIDITY_PL)) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": SIMULATOR_LIST.HUMIDITY_PL
                                        });
                                        if (vm.config.eventHumPl === '3' || vm.config.eventHumPl === '4' || vm.config.eventHumPl === '5' || vm.config.eventHumPl === '6') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventHumPl)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        } else {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventHumPl)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                            vm.parameterValues.push({"name": "room", "value": vm.config.roomHumPl});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyHumPl)
                                            });

                                        }
                                        newTestObject.config.push(vm.parameterValues);
                                    }

                                    if (vm.selectedSensors.includes(SIMULATOR_LIST.GPS)) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": SIMULATOR_LIST.GPS
                                        });
                                        if (vm.config.eventGPS === '3' || vm.config.eventGPS === '4' || vm.config.eventGPS === '5') {
                                            vm.parameterValues.push({"name": "who", "value": vm.config.whoGPS});
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventGPS)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "latitude",
                                                "value": vm.config.latitudeGPS
                                            });
                                            vm.parameterValues.push({
                                                "name": "longitude",
                                                "value": vm.config.longitudeGPS
                                            });
                                            vm.parameterValues.push({"name": "hight", "value": vm.config.hightGPS});
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": vm.config.reactionMetersGPS
                                            });
                                            vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                            vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                        } else {
                                            vm.parameterValues.push({"name": "who", "value": vm.config.whoGPS});
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventGPS)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "latitude",
                                                "value": vm.config.latitudeGPS
                                            });
                                            vm.parameterValues.push({
                                                "name": "longitude",
                                                "value": vm.config.longitudeGPS
                                            });
                                            vm.parameterValues.push({"name": "hight", "value": vm.config.hightGPS});
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": vm.config.reactionMetersGPS
                                            });
                                            vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                            vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyGPS)
                                            });
                                        }

                                        newTestObject.config.push(vm.parameterValues);
                                    }
                                    if (vm.selectedSensors.includes(SIMULATOR_LIST.GPS_PL)) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": SIMULATOR_LIST.GPS_PL
                                        });
                                        if (vm.config.eventGPSPl === '3' || vm.config.eventGPSPl === '4' || vm.config.eventGPSPl === '5') {

                                            vm.parameterValues.push({"name": "who", "value": vm.config.whoGPSPl});
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventGPSPl)
                                            });
                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "latitude",
                                                "value": vm.config.latitudeGPSPl
                                            });
                                            vm.parameterValues.push({
                                                "name": "longitude",
                                                "value": vm.config.longitudeGPSPl
                                            });
                                            vm.parameterValues.push({"name": "hight", "value": vm.config.hightGPSPl});
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": vm.config.reactionMetersGPSPl
                                            });
                                            vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                            vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        } else {
                                            vm.parameterValues.push({"name": "who", "value": vm.config.whoGPSPl});
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventGPSPl)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "latitude",
                                                "value": vm.config.latitudeGPSPl
                                            });
                                            vm.parameterValues.push({
                                                "name": "longitude",
                                                "value": vm.config.longitudeGPSPl
                                            });
                                            vm.parameterValues.push({"name": "hight", "value": vm.config.hightGPSPl});
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": vm.config.reactionMetersGPSPl
                                            });
                                            vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                            vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyGPSPl)
                                            });
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        }

                                        newTestObject.config.push(vm.parameterValues);
                                    }
                                    if (vm.selectedSensors.includes(SIMULATOR_LIST.ACCELERATION)) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": SIMULATOR_LIST.ACCELERATION
                                        });
                                        if (vm.config.eventAcc === '3' || vm.config.eventAcc === '4' || vm.config.eventAcc === '5') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAcc)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });

                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "weightObject", "value": 0});
                                            vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                            vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                        } else if (vm.config.event === '2') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAcc)
                                            });
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyAcc)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "weightObject",
                                                "value": parseInt(vm.config.weightObjectAcc)
                                            });
                                            vm.parameterValues.push({
                                                "name": "sensitivityClass",
                                                "value": parseInt(vm.config.sensitivityAcc)
                                            });
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": parseInt(vm.config.reactionMetersAcc)
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });
                                        } else if (vm.config.event === '1') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAcc)
                                            });
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyAcc)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });

                                            vm.parameterValues.push({"name": "weightObject", "value": 0});
                                            vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                            vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                        }
                                        newTestObject.config.push(vm.parameterValues);
                                    }

                                    if (vm.selectedSensors.includes(SIMULATOR_LIST.ACCELERATION_PL)) {
                                        vm.parameterValues = [];
                                        vm.parameterValues.push({
                                            "name": "ConfigName",
                                            "value": SIMULATOR_LIST.ACCELERATION
                                        });
                                        if (vm.config.eventAccPl === '3' || vm.config.eventAccPl === '4' || vm.config.eventAccPl === '5') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAccPl)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });

                                            vm.parameterValues.push({"name": "anomaly", "value": 0});
                                            vm.parameterValues.push({"name": "weightObject", "value": 0});
                                            vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                            vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        } else if (vm.config.event === '2') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAccPl)
                                            });
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyAccPl)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "weightObject",
                                                "value": parseInt(vm.config.weightObjectAccPl)
                                            });
                                            vm.parameterValues.push({
                                                "name": "sensitivityClass",
                                                "value": parseInt(vm.config.sensitivityAccPl)
                                            });
                                            vm.parameterValues.push({
                                                "name": "reactionMeters",
                                                "value": parseInt(vm.config.reactionMetersAccPl)
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        } else if (vm.config.event === '1') {
                                            vm.parameterValues.push({
                                                "name": "event",
                                                "value": parseInt(vm.config.eventAccPl)
                                            });
                                            vm.parameterValues.push({
                                                "name": "anomaly",
                                                "value": parseInt(vm.config.anomalyAccPl)
                                            });
                                            vm.parameterValues.push({"name": "useNewData", "value": true});
                                            vm.parameterValues.push({
                                                "name": "directionAnomaly",
                                                "value": directionOutlier
                                            });
                                            vm.parameterValues.push({
                                                "name": "directionMovement",
                                                "value": directionMovement
                                            });

                                            vm.parameterValues.push({"name": "weightObject", "value": 0});
                                            vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                            vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                            vm.parameterValues.push({"name": "simTime", "value": vm.config.simTime});
                                            vm.parameterValues.push({
                                                "name": "amountEvents",
                                                "value": vm.config.amountEvents
                                            });
                                            vm.parameterValues.push({
                                                "name": "amountAnomalies",
                                                "value": vm.config.amountAnomalies
                                            });
                                        }
                                        newTestObject.config.push(vm.parameterValues);
                                    }


                                }


                                for (let property in data) {
                                    if (data.hasOwnProperty(property)) {
                                        newTestObject[property] = data[property];
                                    }
                                }


                            } catch (e) {
                                vm.parameterValues = [];
                                newTestObject.type = [];
                                vm.parameterValues.push({
                                    "name": "ConfigName",
                                    "value": 'ERROR'
                                });
                                vm.parameterValues.push({
                                    "name": "event",
                                    "value": 0
                                });
                                vm.parameterValues.push({"name": "anomaly", "value": 0});
                                vm.parameterValues.push({"name": "useNewData", "value": true});
                                newTestObject.config.push(vm.parameterValues);

                                console.log("catched error")
                            }

                            newTestObject.useNewData = true;

                            newTestObject.rules = vm.rules;
                            const radios = document.getElementsByName('executeRules');
                            let i = 0;
                            const length = radios.length;
                            for (; i < length; i++) {
                                if (radios[i].checked) {
                                    var executeRulesTemp = radios[i].value;
                                    break;
                                }
                            }

                            if (checkSimSensor == false && checkRealSensor == false) {
                                NotificationService.notify('Choose at least one sensor', 'error')
                            }


                            if (executeRulesTemp === 'undefined') {
                                NotificationService.notify('A decision must be made.', 'error')
                            }

                            vm.executeRules = executeRulesTemp === 'true';
                            newTestObject.triggerRules = vm.executeRules;
                            return addTest(newTestObject);

                        }
                    }),
                deleteTestCtrl: $controller('DeleteTestController as deleteTestCtrl', {
                    $scope: $scope,
                    deleteItem: deleteTest,
                    confirmDeletion: confirmDelete
                }),
                executeTest: executeTest,
                editConfig: editConfig,
                stopTest: stopTest,
                downloadPDF: downloadPDF,
                refreshTestEntry: refreshTestEntry,
                getDevice: getTestDevice,
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
