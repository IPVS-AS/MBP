/* global app */

/**
 * Controller for the test details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails', 'sensorList','$rootScope', '$routeParams', '$interval', 'UnitService', 'NotificationService', '$http', 'ENDPOINT_URI', 'ruleList', 'updateTest',
        function ($scope, $controller, testingDetails, sensorList ,$rootScope, $routeParams, $interval, UnitService, NotificationService, $http, ENDPOINT_URI, ruleList, updateTest) {
            //Initialization of variables that are used in the frontend by angular
            var vm = this;
            //Test ID
            const COMPONENT_ID = $routeParams.id;

            //Extend each sensor in sensorList for a state and a reload function
            for (var i in sensorList) {
                if(sensorList[i].name === testingDetails.type){
                    vm.sensorID = sensorList[i].id;
                }

            }

            vm.ruleList = ruleList;
            vm.test = testingDetails;
            vm.ruleNames = "";
            vm.actionNames = "";
            vm.deviceNames = "";
            vm.rules = [];
            vm.executeRules = true;
            vm.updateValues = [];
            vm.configUpdate = [];
            vm.executeRulesNew;
            vm.rulesUpdate = [];
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
                var room;
                var simTime;
                var amountEvents;
                var amountAnomalies;
                var who;
                var latitude;
                var longitude;
                var hight;
                var reactionMeters;
                var weightObject;
                var sensitivity;

                vm.rules = [];


                let sensitivityClass;
                if (testingDetails.type === 'TestingTemperaturSensor' || testingDetails.type === 'TestingFeuchtigkeitsSensor') {
                    for (let i = 0; i < testingDetails.config.length; i++) {
                        if ((testingDetails.config[i].name === "event" && testingDetails.config[i].value === '3') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '4') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '5') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '6')) {
                            for (let i = 0; i < testingDetails.config.length; i++) {
                                if (testingDetails.config[i].name === "event") {
                                    event = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "anomaly") {
                                    anomaly = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "useNewData") {
                                    useNewData = !testingDetails.config[i].value;
                                }
                            }
                            $rootScope.config = {
                                event: event,
                                anomaly: anomaly,
                                useNewData: useNewData
                            };
                        } else {
                            for (let i = 0; i < testingDetails.config.length; i++) {
                                if (testingDetails.config[i].name === "event") {
                                    event = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "anomaly") {
                                    anomaly = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "useNewData") {
                                    useNewData = !testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "room") {
                                    room = testingDetails.config[i].value;
                                }
                            }
                            $rootScope.config = {
                                event: event,
                                anomaly: anomaly,
                                useNewData: useNewData,
                                room: room
                            };
                        }
                    }
                } else if (testingDetails.type === 'TestingTemperaturSensorPl' || testingDetails.type === 'TestingFeuchtigkeitsSensorPl') {
                    for (let i = 0; i < testingDetails.config.length; i++) {
                        if ((testingDetails.config[i].name === "event" && testingDetails.config[i].value === '3') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '4') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '5') || (testingDetails.config[i].name === "event" && testingDetails.config[i].value === '6')) {
                            for (let i = 0; i < testingDetails.config.length; i++) {
                                if (testingDetails.config[i].name === "event") {
                                    event = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "anomaly") {
                                    anomaly = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "useNewData") {
                                    useNewData = !testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "simTime") {
                                    simTime = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "amountEvents") {
                                    amountEvents = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "amountAnomalies") {
                                    amountAnomalies = testingDetails.config[i].value;
                                }
                            }

                            $rootScope.config = {
                                event: event,
                                anomaly: anomaly,
                                useNewData: useNewData,
                                simTime: simTime,
                                amountEvents: amountEvents,
                                amountAnomalies: amountAnomalies
                            };

                        } else {
                            for (let i = 0; i < testingDetails.config.length; i++) {
                                if (testingDetails.config[i].name === "event") {
                                    event = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "anomaly") {
                                    anomaly = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "useNewData") {
                                    useNewData = !testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "room") {
                                    room = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "simTime") {
                                    simTime = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "amountEvents") {
                                    amountEvents = testingDetails.config[i].value;
                                } else if (testingDetails.config[i].name === "amountAnomalies") {
                                    amountAnomalies = testingDetails.config[i].value;
                                }
                            }
                            $rootScope.config = {
                                event: event,
                                anomaly: anomaly,
                                useNewData: useNewData,
                                room: room,
                                simTime: simTime,
                                amountEvents: amountEvents,
                                amountAnomalies: amountAnomalies
                            };


                        }
                    }

                } else if (testingDetails.type === 'TestingGPSSensor') {
                    for (let i = 0; i < testingDetails.config.length; i++) {

                        if (testingDetails.config[i].name === "event") {
                            event = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "anomaly") {
                            anomaly = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "useNewData") {
                            useNewData = !testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "latitude") {
                            latitude = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "longitude") {
                            longitude = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "hight") {
                            hight = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "who") {
                            who = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "reactionMeters") {
                            reactionMeters = testingDetails.config[i].value;
                        }

                    }
                    $rootScope.config = {
                        event: event,
                        anomaly: anomaly,
                        useNewData: useNewData,
                        latitude: latitude,
                        longitude: longitude,
                        hight: hight,
                        who: who,
                        reactionMeters: reactionMeters
                    };

                } else if (testingDetails.type === 'TestingGPSSensorPl') {
                    for (let i = 0; i < testingDetails.config.length; i++) {
                        if (testingDetails.config[i].name === "event") {
                            event = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "anomaly") {
                            anomaly = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "useNewData") {
                            useNewData = !testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "latitude") {
                            latitude = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "longitude") {
                            longitude = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "hight") {
                            hight = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "who") {
                            who = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "reactionMeters") {
                            reactionMeters = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "simTime") {
                            simTime = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "amountEvents") {
                            amountEvents = testingDetails.config[i].value;
                        } else if (testingDetails.config[i].name === "amountAnomalies") {
                            amountAnomalies = testingDetails.config[i].value;

                        }
                    }
                    $rootScope.config = {
                        event: event,
                        anomaly: anomaly,
                        useNewData: useNewData,
                        latitude: latitude,
                        longitude: longitude,
                        hight: hight,
                        who: who,
                        reactionMeters: reactionMeters,
                        simTime: simTime,
                        amountEvents: amountEvents,
                        amountAnomalies: amountAnomalies
                    };


                } else if (testingDetails.type === 'TestingBeschleunigungsSensor') {
                    for (let i = 0; i < testingDetails.config.length; i++) {
                        for (let i = 0; i < testingDetails.config.length; i++) {

                            if (testingDetails.config[i].name === "event") {
                                event = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "anomaly") {
                                anomaly = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "useNewData") {
                                useNewData = !testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "weightObject") {
                                weightObject = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "sensitivityClass") {
                                sensitivityClass = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "reactionMeters") {
                                reactionMeters = testingDetails.config[i].value;
                            }


                        }
                    }
                    $rootScope.config = {
                        event: event,
                        anomaly: anomaly,
                        useNewData: useNewData,
                        reactionMeters: reactionMeters,
                        weightObject: weightObject,
                        sensitivityClass: sensitivityClass,
                    };
                } else if (testingDetails.type === 'TestingBeschleunigungsSensorPl') {
                    for (let i = 0; i < testingDetails.config.length; i++) {
                        for (let i = 0; i < testingDetails.config.length; i++) {

                            if (testingDetails.config[i].name === "event") {
                                event = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "anomaly") {
                                anomaly = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "useNewData") {
                                useNewData = !testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "weightObject") {
                                weightObject = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "sensitivityClass") {
                                sensitivityClass = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "reactionMeters") {
                                reactionMeters = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "simTime") {
                                simTime = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "amountEvents") {
                                amountEvents = testingDetails.config[i].value;
                            } else if (testingDetails.config[i].name === "amountAnomalies") {
                                amountAnomalies = testingDetails.config[i].value;

                            }


                        }
                    }
                    $rootScope.config = {
                        event: event,
                        anomaly: anomaly,
                        useNewData: useNewData,
                        reactionMeters: reactionMeters,
                        weightObject: weightObject,
                        sensitivityClass: sensitivityClass,
                        simTime: simTime,
                        amountAnomalies: amountAnomalies,
                        amountEvents: amountEvents
                    };
                }

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
                    var pdfList = {};
                    vm.pdfDetails = [];


                    if (Object.keys(response.data).length > 0) {
                        angular.forEach(response.data, function (value, key) {
                            vm.pdfDetails.push({
                                "date": key,
                                "path": value
                            });
                        });
                        pdfList.pdfTable = vm.pdfDetails;
                        $scope.pdfTable = pdfList.pdfTable;
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


            /**
             * Sends a server request in order to update/edit the whole configurations of the test, if the user modifies the configuration via the edit Test Modal.
             *
             *
             */
            function editTestConfiguration() {
                vm.configUpdate = [];
                vm.rulesUpdate = [];

                getUpdateValues();
                testingDetails.config = [];
                testingDetails.config = vm.configUpdate;
                testingDetails.rules = vm.rulesUpdate;
                if (vm.executeRules === "true") {
                    testingDetails.triggerRules = true;
                } else if (vm.executeRules === "false") {
                    testingDetails.triggerRules = false;
                }

                $http.post(ENDPOINT_URI + '/test-details/updateTest/' + COMPONENT_ID, JSON.stringify(testingDetails)).success(function successCallback() {
                    //Close modal on success
                    $("#editTestModal").modal('toggle');
                });


            }


            /**
             * Gets all new configuration settings of the user from the edit test modal and puts them into the correct format.
             *
             */
            function getUpdateValues() {
                try {
                    vm.configUpdate = [];
                    vm.rulesUpdate = [];
                    //Extend request parameters for routines and parameters

                    // random values Angle and Axis for the GPS-Sensor
                    var randomAngle = Math.floor((Math.random() * 361));
                    var randomAxis = Math.floor((Math.random() * 3));


                    // random values for the direction of the outlier and movement for the acceleration Sensor
                    var directionAnomaly = Math.floor(Math.random() * 6);
                    var directionMovement = Math.floor(Math.random() * 6);
                    var newTestObject = {};


                    if (testingDetails.type === 'TestingTemperaturSensor' || testingDetails.type === 'TestingFeuchtigkeitsSensor') {
                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5' || $rootScope.config.event === '6') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                        } else {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({"name": "room", "value": $rootScope.config.room});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                        }
                    } else if (testingDetails.type === 'TestingTemperaturSensorPl' || testingDetails.type === 'TestingFeuchtigkeitsSensorPl') {

                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5' || $rootScope.config.event === '6') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        } else {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                            vm.configUpdate.push({"name": "room", "value": $rootScope.config.room});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });

                        }
                    } else if (testingDetails.type === 'TestingGPSSensor') {
                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5') {
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.who});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitude
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitude
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hight});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMeters
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                        } else {
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.who});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitude
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitude
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hight});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMeters
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                        }
                    } else if (testingDetails.type === 'TestingGPSSensorPl') {
                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5') {

                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.who});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitude
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitude
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hight});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMeters
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        } else {
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.who});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitude
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitude
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hight});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMeters
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        }

                    } else if (testingDetails.type === 'TestingBeschleunigungsSensor') {
                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "weightObject", "value": 0});
                            vm.configUpdate.push({"name": "sensitivityClass", "value": 0});
                            vm.configUpdate.push({"name": "reactionMeters", "value": 3});
                        } else if ($rootScope.config.event === '2') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "weightObject",
                                "value": parseInt($rootScope.config.weightObject)
                            });
                            vm.configUpdate.push({
                                "name": "sensitivityClass",
                                "value": parseInt($rootScope.config.sensitivity)
                            });
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": parseInt($rootScope.config.reactionMeters)
                            });
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });
                        } else if ($rootScope.config.event === '1') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            vm.configUpdate.push({"name": "weightObject", "value": 0});
                            vm.configUpdate.push({"name": "sensitivityClass", "value": 0});
                            vm.configUpdate.push({"name": "reactionMeters", "value": 3});
                        }
                    } else if (testingDetails.type === 'TestingBeschleunigungsSensorPl') {
                        if ($rootScope.config.event === '3' || $rootScope.config.event === '4' || $rootScope.config.event === '5') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "weightObject", "value": 0});
                            vm.configUpdate.push({"name": "sensitivityClass", "value": 0});
                            vm.configUpdate.push({"name": "reactionMeters", "value": 3});
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        } else if ($rootScope.config.event === '2') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "weightObject",
                                "value": parseInt($rootScope.config.weightObject)
                            });
                            vm.configUpdate.push({
                                "name": "sensitivityClass",
                                "value": parseInt($rootScope.config.sensitivity)
                            });
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": parseInt($rootScope.config.reactionMeters)
                            });
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        } else if ($rootScope.config.event === '1') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.event)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomaly)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "directionAnomaly",
                                "value": directionAnomaly
                            });
                            vm.configUpdate.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            vm.configUpdate.push({"name": "weightObject", "value": 0});
                            vm.configUpdate.push({"name": "sensitivityClass", "value": 0});
                            vm.configUpdate.push({"name": "reactionMeters", "value": 3});
                            vm.configUpdate.push({"name": "simTime", "value": $rootScope.config.simTime});
                            vm.configUpdate.push({
                                "name": "amountEvents",
                                "value": $rootScope.config.amountEvents
                            });
                            vm.configUpdate.push({
                                "name": "amountAnomalies",
                                "value": $rootScope.config.amountAnomalies
                            });
                        }
                    }


                } catch (e) {
                    newTestObject.type = "";
                    vm.configUpdate.push({
                        "name": "event",
                        "value": parseInt(0)
                    });
                    vm.configUpdate.push({"name": "anomaly", "value": 0});
                    vm.configUpdate.push({"name": "useNewData", "value": true});
                    newTestObject.config = vm.configUpdate;


                }

                // Get the new list of selected rules for the test
                vm.rulesUpdate = $rootScope.selectedRules.rules;

                if (vm.executeRules === 'undefined') {
                    NotificationService.notify('A decision must be made.', 'error')
                }

                // Get information whether the selected rules should be triggered through the Sensor simulation or not
                vm.executeRulesNew = vm.executeRules === true;


            }

            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, $controller('TestingChartController as testingChartCtrl',
                {
                    $scope: $scope,
                    testingDetails: testingDetails,
                    sensorList: sensorList,
                    liveChartContainer: 'liveValues',
                    historicalChartContainer: 'historicalValues',
                    historicalChartSlider: 'historicalChartSlider'
                }), {
                updateTestCtrl: $controller('UpdateItemController as updateTestCtrl', {
                    $scope: $scope,
                    updateItem: editTestConfiguration
                }),
                downloadPDF: downloadPDF,
                executeTest: executeTest,
                stopTest: stopTest,
                getPDFList: getPDFList,
                editConfig: editConfig,
                editTestConfiguration: editTestConfiguration
            });


        }
    ]
);
