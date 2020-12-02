/* global app */

/**
 * Controller for the test details pages that can be used to extend more specific controllers with a default behaviour.
 */
app.controller('TestingDetailsController',
    ['$scope', '$controller', 'testingDetails', 'sensorList', '$rootScope', '$routeParams', '$interval', 'UnitService', 'NotificationService', '$http', 'ENDPOINT_URI', 'ruleList',
        function ($scope, $controller, testingDetails, sensorList, $rootScope, $routeParams, $interval, UnitService, NotificationService, $http, ENDPOINT_URI, ruleList) {
            //Initialization of variables that are used in the frontend by angular
            const vm = this;
            //Test ID
            const COMPONENT_ID = $routeParams.id;

            vm.sensorListTest = [];
            vm.sensorListTestNames = [];
            //Extend each sensor in sensorList for a state and a reload function
            for (const i in sensorList) {
                if (testingDetails.type.indexOf(sensorList[i].name) !== -1) {
                    vm.sensorListTestNames.push(sensorList[i].name);
                }
            }

            if (testingDetails.useNewData === false) {
                for (const sensorName in vm.sensorListTestNames) {
                    for (const y in sensorList) {
                        const test = "RERUN_" + vm.sensorListTestNames[sensorName];
                        if (sensorList[y].name === "RERUN_" + vm.sensorListTestNames[sensorName]) {
                            vm.sensorListTest.push(sensorList[y]);
                        }
                    }
                }
            } else {
                for (const i in sensorList) {
                    if (testingDetails.type.indexOf(sensorList[i].name) !== -1) {
                        vm.sensorListTest.push(sensorList[i]);
                        vm.sensorListTestNames.push(sensorList[i].name);
                    }
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
            vm.rulesUpdate = [];
            vm.sensorType = testingDetails.type;
            vm.newTestObject = {};
            vm.newTestObject.config = [];


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

                getPDFList();
                getTestSensors();
                getTestRules();
                getConfig();
                disableReuse();


                //Refresh test select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });

            })();

            function checkAvailability(arr, val) {
                return arr.some(function (arrVal) {
                    return val === arrVal;
                });
            }

            function disableReuse(){
                if(testingDetails.simulationList === null){
                    console.log("in if")
                    document.getElementById("ReuseSwitch").disabled = true;
                } else{
                    console.log("else")
                    document.getElementById("ReuseSwitch").removeAttribute('disabled');
                }
                console.log(document.getElementById("ReuseSwitch"))
            }


            /**
             * Get the Configuration of the test to display them on the edit Test Modal
             */
            function getConfig() {
                let eventTemp;
                let anomalyTemp;
                let roomTemp;
                let eventHum;
                let anomalyHum;
                let roomHum;
                let eventTempPl;
                let anomalyTempPl;
                let roomTempPl;
                let eventHumPl;
                let anomalyHumPl;
                let useNewData;
                let roomHumPl;

                let simTime;
                let amountEvents;
                let amountAnomalies;
                let eventGPS;
                let anomalyGPS;
                let whoGPS;
                let latitudeGPS;
                let longitudeGPS;
                let hightGPS;
                let reactionMetersGPS;

                let eventGPSPl;
                let anomalyGPSPl;
                let whoGPSPl;
                let latitudeGPSPl;
                let longitudeGPSPl;
                let hightGPSPl;
                let reactionMetersGPSPl;

                let eventAcc;
                let anomalyAcc;
                let weightObjectAcc;
                let sensitivityClassAcc;
                let reactionMetersAcc;

                let eventAccPl;
                let anomalyAccPl;
                let weightObjectAccPl;
                let sensitivityClassAccPl;
                let reactionMetersAccPl;

                vm.rules = [];
                vm.selectedRealSensor = [];
                $rootScope.config = {};

                var arr = ['TestingTemperaturSensor', 'TestingTemperaturSensorPl', 'TestingFeuchtigkeitsSensor', 'TestingFeuchtigkeitsSensorPl', 'TestingGPSSensorPl', 'TestingGPSSensor', 'TestingBeschleunigungsSensor', 'TestingBeschleunigungsSensorPl']

                for (let y = 0; y < testingDetails.type.length; y++) {

                    if (!checkAvailability(arr, testingDetails.type[y])) {
                        for (let z = 0; z < sensorList.length; z++) {
                            if (sensorList[z].name === testingDetails.type[y]) {
                                vm.selectedRealSensor.push(sensorList[z]);
                            }

                        }
                    }

                }

                testingDetails.config.forEach(function (config) {
                    for (let n = 0; n < config.length; n++) {
                        if (config[n].name === "ConfigRealSensors") {
                            vm.parameterVal = angular.fromJson(config[n].value);
                        }
                    }

                });


                $scope.config.useNewData = !testingDetails.useNewData;

                if (testingDetails.type.includes('TestingTemperaturSensor')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event" && parameterInstance.value === 3 || parameterInstance.name === "event" && parameterInstance.value === 4 || parameterInstance.name === "event" && parameterInstance.value === 5 || parameterInstance.name === "event" && parameterInstance.value === 6) {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventTemp = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyTemp = parameterInstance.value;
                                        } else if (parameterInstance.name === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        }
                                    })
                                })

                                $rootScope.config.eventTemp = eventTemp;
                                $rootScope.config.anomalyTemp = anomalyTemp;
                            } else {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventTemp = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyTemp = parameterInstance.value;
                                        } else if (parameterInstance.name === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "room") {
                                            roomTemp = parameterInstance.value;
                                        }
                                    })
                                })


                                $rootScope.config.eventTemp = eventTemp;
                                $rootScope.config.anomalyTemp = anomalyTemp;
                                $rootScope.config.roomTemp = roomTemp;


                            }
                        });
                    });


                }
                if (testingDetails.type.includes('TestingFeuchtigkeitsSensor')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event" && parameterInstance.value === '3' || parameterInstance.name === "event" && parameterInstance.value === '4' || parameterInstance.name === "event" && parameterInstance.value === '5' || parameterInstance.name === "event" && parameterInstance.value === '6') {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventHum = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyHum = parameterInstance.value;
                                        } else if (parameterInstance.name === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        }
                                    })
                                })

                                $rootScope.config.eventHum = eventHum;
                                $rootScope.config.anomalyHum = anomalyHum;
                            } else {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventHum = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyHum = parameterInstance.value;
                                        } else if (parameterInstance.name === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "room") {
                                            roomHum = parameterInstance.value;
                                        }
                                    })
                                })

                                $rootScope.config.eventHum = eventHum;
                                $rootScope.config.anomalyHum = anomalyHum;
                                $rootScope.config.roomHum = roomHum;

                            }
                        });
                    });


                }


                if (testingDetails.type.includes('TestingTemperaturSensorPl')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event" && parameterInstance.value === '3' || parameterInstance.name === "event" && parameterInstance.value === '4' || parameterInstance.name === "event" && parameterInstance.value === '5' || parameterInstance.name === "event" && parameterInstance.value === '6') {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventTempPl = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyTempPl = parameterInstance.value;
                                        } else if (parameterInstance.name === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "simTime") {
                                            simTime = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountEvents") {
                                            amountEvents = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountAnomalies") {
                                            amountAnomalies = parameterInstance.value;
                                        }
                                    })
                                })


                                $rootScope.config.eventTempPl = eventTempPl;
                                $rootScope.config.anomalyTempPl = anomalyTempPl;
                                $rootScope.config.simTime = simTime;
                                $rootScope.config.amountEvents = amountEvents;
                                $rootScope.config.amountAnomalies = amountAnomalies;

                            } else {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventTempPl = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyTempPl = parameterInstance.value;
                                        } else if (parameterInstance.value === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "simTime") {
                                            simTime = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountEvents") {
                                            amountEvents = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountAnomalies") {
                                            amountAnomalies = parameterInstance.value;
                                        } else if (parameterInstance.name === "room") {
                                            roomTempPl = parameterInstance.value;
                                        }
                                    })
                                })

                                $rootScope.config.eventTempPl = eventTempPl;
                                $rootScope.config.anomalyTempPl = anomalyTempPl;
                                $rootScope.config.roomTempPl = roomTempPl;
                                $rootScope.config.simTime = simTime;
                                $rootScope.config.amountEvents = amountEvents;
                                $rootScope.config.amountAnomalies = amountAnomalies;


                            }
                        })
                    })
                }
                if (testingDetails.type.includes('TestingFeuchtigkeitsSensorPl')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event" && parameterInstance.value === '3' || parameterInstance.name === "event" && parameterInstance.value === '4' || parameterInstance.name === "event" && parameterInstance.value === '5' || parameterInstance.name === "event" && parameterInstance.value === '6') {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventHumPl = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyHumPl = parameterInstance.value;
                                        } else if (parameterInstance.value === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "simTime") {
                                            simTime = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountEvents") {
                                            amountEvents = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountAnomalies") {
                                            amountAnomalies = parameterInstance.value;
                                        }
                                    })
                                })
                                $rootScope.config.eventHumPl = eventHumPl;
                                $rootScope.config.anomalyHumPl = anomalyHumPl;
                                $rootScope.config.simTime = simTime;
                                $rootScope.config.amountEvents = amountEvents;
                                $rootScope.config.amountAnomalies = amountAnomalies;

                            } else {
                                testingDetails.config.forEach(function (config) {
                                    config.forEach(function (parameterInstance) {
                                        if (parameterInstance.name === "event") {
                                            eventHumPl = parameterInstance.value;
                                        } else if (parameterInstance.name === "anomaly") {
                                            anomalyHumPl = parameterInstance.value;
                                        } else if (parameterInstance.value === "useNewData") {
                                            useNewData = !parameterInstance.value;
                                        } else if (parameterInstance.name === "simTime") {
                                            simTime = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountEvents") {
                                            amountEvents = parameterInstance.value;
                                        } else if (parameterInstance.name === "amountAnomalies") {
                                            amountAnomalies = parameterInstance.value;
                                        } else if (parameterInstance.name === "room") {
                                            roomHumPl = parameterInstance.value;
                                        }
                                    })
                                })
                                $rootScope.config.eventHumPl = eventHumPl;
                                $rootScope.config.anomalyHumPl = anomalyHumPl;
                                $rootScope.config.roomHumPl = roomHumPl;
                                $rootScope.config.simTime = simTime;
                                $rootScope.config.amountEvents = amountEvents;
                                $rootScope.config.amountAnomalies = amountAnomalies;

                            }
                        })
                    })
                }

                if (testingDetails.type.includes('TestingGPSSensor')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event") {
                                eventGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "anomaly") {
                                anomalyGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "useNewData") {
                                useNewData = !parameterInstance.value;
                            } else if (parameterInstance.name === "latitude") {
                                latitudeGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "longitude") {
                                longitudeGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "hight") {
                                hightGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "who") {
                                whoGPS = parameterInstance.value;
                            } else if (parameterInstance.name === "reactionMeters") {
                                reactionMetersGPS = parameterInstance.value;
                            }
                        })
                    })

                    $rootScope.config.eventGPS = eventGPS;
                    $rootScope.config.anomalyGPS = anomalyGPS;

                    $rootScope.config.latitudeGPS = latitudeGPS;
                    $rootScope.config.longitudeGPS = longitudeGPS;
                    $rootScope.config.hightGPS = hightGPS;
                    $rootScope.config.whoGPS = whoGPS
                    $rootScope.config.reactionMetersGPS = reactionMetersGPS;

                }


                if (testingDetails.type.includes('TestingGPSSensorPl')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event") {
                                eventGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "anomaly") {
                                anomalyGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "useNewData") {
                                useNewData = !parameterInstance.value;
                            } else if (parameterInstance.name === "latitude") {
                                latitudeGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "longitude") {
                                longitudeGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "hight") {
                                hightGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "who") {
                                whoGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "reactionMeters") {
                                reactionMetersGPSPl = parameterInstance.value;
                            } else if (parameterInstance.name === "simTime") {
                                simTime = parameterInstance.value;
                            } else if (parameterInstance.name === "amountEvents") {
                                amountEvents = parameterInstance.value;
                            } else if (parameterInstance.name === "amountAnomalies") {
                                amountAnomalies = parameterInstance.value;

                            }
                        })
                    })

                    $rootScope.config.eventGPS = eventGPS;
                    $rootScope.config.anomalyGPS = anomalyGPS;
                    $rootScope.config.latitudeGPS = latitudeGPS;
                    $rootScope.config.longitudeGPS = longitudeGPS;
                    $rootScope.config.hightGPS = hightGPS;
                    $rootScope.config.whoGPS = whoGPS
                    $rootScope.config.reactionMetersGPS = reactionMetersGPS;
                    $rootScope.config.simTime = simTime;
                    $rootScope.config.amountEvents = amountEvents;
                    $rootScope.config.amountAnomalies = amountAnomalies;


                }

                if (testingDetails.type.includes('TestingBeschleunigungsSensor')) {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event") {
                                eventAcc = parameterInstance.value;
                            } else if (parameterInstance.name === "anomaly") {
                                anomalyAcc = parameterInstance.value;
                            } else if (parameterInstance.name === "useNewData") {
                                useNewData = !parameterInstance.value;
                            } else if (parameterInstance.name === "weightObject") {
                                weightObjectAcc = parameterInstance.value;
                            } else if (parameterInstance.name === "sensitivityClass") {
                                sensitivityClassAcc = parameterInstance.value;
                            } else if (parameterInstance.name === "reactionMeters") {
                                reactionMetersAcc = parameterInstance.value;
                            }
                        })
                    })
                    $rootScope.config.eventAcc = eventAcc;
                    $rootScope.config.anomalyAcc = anomalyAcc;
                    $rootScope.config.reactionMetersAcc = reactionMetersAcc;
                    $rootScope.config.weightObjectAcc = weightObjectAcc;
                    $rootScope.config.sensitivityClassAcc = sensitivityClassAcc;


                }

                if (testingDetails.type === 'TestingBeschleunigungsSensorPl') {
                    testingDetails.config.forEach(function (config) {
                        config.forEach(function (parameterInstance) {
                            if (parameterInstance.name === "event") {
                                eventAccPl = parameterInstance.value;
                            } else if (parameterInstance.name === "anomaly") {
                                anomalyAccPl = parameterInstance.value;
                            } else if (parameterInstance.name === "useNewData") {
                                useNewData = !parameterInstance.value;
                            } else if (parameterInstance.name === "weightObject") {
                                weightObjectAccPl = parameterInstance.value;
                            } else if (parameterInstance.name === "sensitivityClass") {
                                sensitivityClassAccPl = parameterInstance.value;
                            } else if (parameterInstance.name === "reactionMeters") {
                                reactionMetersAccPl = parameterInstance.value;
                            } else if (parameterInstance.name === "simTime") {
                                simTime = parameterInstance.value;
                            } else if (parameterInstance.name === "amountEvents") {
                                amountEvents = parameterInstance.value;
                            } else if (parameterInstance.name === "amountAnomalies") {
                                amountAnomalies = parameterInstance.value;

                            }

                        })
                    })

                    $rootScope.config.eventAccPl = eventAccPl;
                    $rootScope.config.anomalyAccPl = anomalyAccPl;

                    $rootScope.config.reactionMetersAccPl = reactionMetersAccPl;
                    $rootScope.config.weightObjectAccPl = weightObjectAccPl;
                    $rootScope.config.sensitivityClassAccPl = sensitivityClassAccPl;
                    $rootScope.config.simTime = simTime;
                    $rootScope.config.amountEvents = amountEvents;
                    $rootScope.config.amountAnomalies = amountAnomalies;


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
             * Performs a server request in order to start a test given by its id.
             */
            async function executeTest() {
                $http.post(ENDPOINT_URI + '/test-details/test/' + COMPONENT_ID, COMPONENT_ID.toString()).success(function successCallback() {
                    // If the test was completed successfully, enable the download Test Report Button
                    getPDFList();
                });


            }

            /**
             * [Public]
             * Creates a server request to get a list of all generated Test Reports regarding to the Test of the IoT-Application.
             */
            function getPDFList() {
                $http.get(ENDPOINT_URI + '/test-details/pdfList/' + COMPONENT_ID).then(function (response) {
                    const pdfList = {};
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
             * @param useNewData
             */
            function editConfig(useNewData) {
                if (useNewData === true) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + COMPONENT_ID, "false").success(function success(response) {
                        $scope.erfolgreich = response.success;
                        window.location.reload();
                });
                }else if (useNewData === false) {
                    $http.post(ENDPOINT_URI + '/test-details/editConfig/' + COMPONENT_ID, "true").success(function success(response) {
                        $scope.erfolgreich = response.success;
                        window.location.reload();
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
                testingDetails.config = vm.newTestObject.config;
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
                    vm.rulesUpdate = [];
                    //Extend request parameters for routines and parameters

                    // random values Angle and Axis for the GPS-Sensor
                    const randomAngle = Math.floor((Math.random() * 361));
                    const randomAxis = Math.floor((Math.random() * 3));


                    // random values for the direction of the outlier and movement for the acceleration Sensor
                    const directionAnomaly = Math.floor(Math.random() * 6);
                    const directionMovement = Math.floor(Math.random() * 6);
                    vm.newTestObject = {};
                    vm.newTestObject.config = [];


                    if (!angular.isUndefined(vm.parameterVal)) {
                        angular.forEach(vm.parameterVal, function (parameters, key) {
                            for (let i = 0; i < vm.selectedRealSensor.length; i++) {
                                if (vm.selectedRealSensor[i].name === key) {
                                    vm.configUpdate = [];
                                    vm.configUpdate.push({
                                        "name": "ConfigName",
                                        "value": vm.selectedRealSensor[i].name
                                    });
                                    var requiredParams = vm.selectedRealSensor[i]._embedded.adapter.parameters;

                                    //Iterate over all parameters
                                    for (let i = 0; i < requiredParams.length; i++) {
                                        //Set empty default values for these parameters
                                        var value = "";

                                        if (requiredParams[i].type === "Switch") {
                                            value = true;
                                        }
                                        if (requiredParams[i].name === "device_code") {
                                            console.log("Requesting code for required parameter device_code.");
                                            value = getDeviceCode();
                                            continue;
                                        }

                                        //For each parameter, add a tuple (name, value) to the globally accessible parameter array
                                        vm.configUpdate.push({
                                            "name": requiredParams[i].name,
                                            "value": parameters[i]
                                        });
                                    }
                                    vm.newTestObject.config.push(vm.configUpdate);
                                }
                            }
                        });
                        vm.newTestObject.config.push([{"name": "ConfigRealSensors", "value": vm.parameterVal}])
                    }

                    if (testingDetails.type.includes('TestingTemperaturSensor')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingTemperaturSensor'
                        });
                        if ($rootScope.config.eventTemp === '3' || $rootScope.config.eventTemp === '4' || $rootScope.config.eventTemp === '5' || $rootScope.config.eventTemp === '6') {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventTemp)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                        } else {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventTemp)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({"name": "room", "value": $rootScope.config.roomTemp});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyTemp)
                            });
                        }
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingFeuchtigkeitsSensor')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingFeuchtigkeitsSensor'
                        });
                        if ($rootScope.config.eventHum === 3 || $rootScope.config.eventHum === 4 || $rootScope.config.eventHum === 5 || $rootScope.config.eventHum === 6) {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventHum)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                        } else {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventHum)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({"name": "room", "value": $rootScope.config.roomHum});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyHum)
                            });
                        }
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingTemperaturSensorPl')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingTemperaturSensorPl'
                        });
                        if ($rootScope.config.eventTempPl === 3 || $rootScope.config.eventTempPl === 4 || $rootScope.config.eventTempPl === 5 || $rootScope.config.eventTempPl === 6) {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventTempPl)
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
                                "value": parseInt($rootScope.config.eventTempPl)
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
                                "value": parseInt($rootScope.config.anomalyTempPl)
                            });

                        }
                        vm.newTestObject.config.push(vm.configUpdate);

                    }
                    if (testingDetails.type.includes('TestingFeuchtigkeitsSensorPl')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingFeuchtigkeitsSensorPl'
                        });
                        if ($rootScope.config.eventHumPl === 3 || $rootScope.config.eventHumPl === 4 || $rootScope.config.eventHumPl === 5 || $rootScope.config.eventHumPl === 6) {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventHumPl)
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
                                "value": parseInt($rootScope.config.eventHumPl)
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
                            vm.configUpdate.push({"name": "room", "value": $rootScope.config.roomHumPl});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyHumPl)
                            });

                        }
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingGPSSensor')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingGPSSensor'
                        });
                        if ($rootScope.config.eventGPS === 3 || $rootScope.config.eventGPS === 4 || $rootScope.config.eventGPS === 5) {
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.whoGPS});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventGPS)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitudeGPS
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitudeGPS
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hightGPS});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMetersGPS
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                        } else {
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.whoGPS});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventGPS)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitudeGPS
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitudeGPS
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hightGPS});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMetersGPS
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyGPS)
                            });
                        }
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingGPSSensorPl')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingGPSSensorPl'
                        });
                        if ($rootScope.config.eventGPS === 3 || $rootScope.config.eventGPS === 4 || $rootScope.config.eventGPS === 5) {

                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.whoGPS});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventGPS)
                            });
                            vm.configUpdate.push({"name": "anomaly", "value": 0});
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitudeGPS
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitudeGPS
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hightGPS});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMetersGPS
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
                            vm.configUpdate.push({"name": "who", "value": $rootScope.config.whoGPS});
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventGPS)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "latitude",
                                "value": $rootScope.config.latitudeGPS
                            });
                            vm.configUpdate.push({
                                "name": "longitude",
                                "value": $rootScope.config.longitudeGPS
                            });
                            vm.configUpdate.push({"name": "hight", "value": $rootScope.config.hightGPS});
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": $rootScope.config.reactionMetersGPS
                            });
                            vm.configUpdate.push({"name": "randomAngle", "value": randomAngle});
                            vm.configUpdate.push({"name": "axis", "value": randomAxis});
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyGPS)
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
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingBeschleunigungsSensor')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingBeschleunigungsSensor'
                        });
                        if ($rootScope.config.eventAcc === 3 || $rootScope.config.eventAcc === 4 || $rootScope.config.eventAcc === 5) {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventAcc)
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
                                "value": parseInt($rootScope.config.eventAcc)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyAcc)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "weightObject",
                                "value": parseInt($rootScope.config.weightObjectAcc)
                            });
                            vm.configUpdate.push({
                                "name": "sensitivityClass",
                                "value": parseInt($rootScope.config.sensitivityAcc)
                            });
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": parseInt($rootScope.config.reactionMetersAcc)
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
                                "value": parseInt($rootScope.config.eventAcc)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyAcc)
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
                        vm.newTestObject.config.push(vm.configUpdate);
                    }
                    if (testingDetails.type.includes('TestingBeschleunigungsSensorPl')) {
                        vm.configUpdate = [];
                        vm.configUpdate.push({
                            "name": "ConfigName",
                            "value": 'TestingBeschleunigungsSensorPl'
                        });
                        if ($rootScope.config.eventAccPl === 3 || $rootScope.config.eventAccPl === 4 || $rootScope.config.eventAccPl === 5) {
                            vm.configUpdate.push({
                                "name": "event",
                                "value": parseInt($rootScope.config.eventAccPl)
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
                                "value": parseInt($rootScope.config.eventAccPl)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyAccPl)
                            });
                            vm.configUpdate.push({"name": "useNewData", "value": true});
                            vm.configUpdate.push({
                                "name": "weightObject",
                                "value": parseInt($rootScope.config.weightObjectAccPl)
                            });
                            vm.configUpdate.push({
                                "name": "sensitivityClass",
                                "value": parseInt($rootScope.config.sensitivityAccPl)
                            });
                            vm.configUpdate.push({
                                "name": "reactionMeters",
                                "value": parseInt($rootScope.config.reactionMetersAccPl)
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
                                "value": parseInt($rootScope.config.eventAccPl)
                            });
                            vm.configUpdate.push({
                                "name": "anomaly",
                                "value": parseInt($rootScope.config.anomalyAccPl)
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
                        vm.newTestObject.config.push(vm.configUpdate);
                    }


                } catch (e) {
                    vm.newTestObject.type = "";
                    vm.configUpdate.push({
                        "name": "event",
                        "value": parseInt(0)
                    });
                    vm.configUpdate.push({"name": "anomaly", "value": 0});
                    vm.configUpdate.push({"name": "useNewData", "value": true});
                    vm.newTestObject.config = vm.configUpdate;


                }

                // Get the new list of selected rules for the test
                vm.rulesUpdate = $rootScope.selectedRules.rules;

                if (vm.executeRules === 'undefined') {
                    NotificationService.notify('A decision must be made.', 'error')
                }

                // Get information whether the selected rules should be triggered through the Sensor simulation or not
                vm.executeRulesNew = vm.executeRules === true;


            }

            /**
             * [Public]
             * Performs a server request in order to add default operators.
             * @returns {*}
             */
            function addRerunOperators() {
                return $.post(URL_DEFAULT_OPERATORS);
            }


            //Extend the controller object for the public functions to make them available from outside
            angular.extend(vm, $controller('TestingChartController as testingChartCtrl',
                {
                    $scope: $scope,
                    testingDetails: testingDetails,
                    sensorList: vm.sensorListTest,
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
)
;
