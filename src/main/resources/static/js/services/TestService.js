/* global app */

/**
 * Provides services for managing tests.
 */
app.factory('TestService', ['$http', '$resource', '$q', 'ENDPOINT_URI', 'NotificationService',
    function ($http, $resource, $q, ENDPOINT_URI, NotificationService) {
        //URLs for server requests
        const URL_TEST_START = ENDPOINT_URI + '/test-details/test/';
        const URL_TEST_STOP = ENDPOINT_URI + 'test-details/test/stop/';
        const URL_REPORT_LIST = ENDPOINT_URI + '/test-details/pdfList/';
        const URL_REPORT_EXISTS = ENDPOINT_URI + '/test-details/pdfExists/';
        const URL_TESTDEVICE_REGISTER = ENDPOINT_URI + '/test-details/registerTestDevice';
        const URL_TESTACTUATOR_REGISTER = ENDPOINT_URI +'/test-details/registerTestActuator';
        const URL_ONEDIM_SENSOR_REGISTER = ENDPOINT_URI + '/test-details/registerSensorSimulator';
        const vm = this;

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


        /**
         * [Public]
         *
         * Performs a server request in order to execute a test given by its id.
         * @param testId The id of the test to be executed
         * @returns {*}
         */
        function executeTest(testId) {
            return $http.post(URL_TEST_START + testId);
        }

        /**
         * [Public]
         *
         * Performs a server request in order to stop a test and its components given by its id.
         * @param testId The id of the test to be stopped
         * @returns {*}
         */
        function stopTest(testId) {
            return $http.post(URL_TEST_STOP + testId);

        }


        /**
         * [Public]
         *
         * Performs a server request to get a list of all generated Test Reports regarding to a test given by its id.
         */
        function getPDFList(testId) {
            return $http.get(URL_REPORT_LIST + testId).then(function (response) {
                const pdfList = {};
                let pdfDetails = [];
                let responseArray = [];

                if (Object.keys(response.data).length > 0) {
                    angular.forEach(response.data, function (value, key) {
                        pdfDetails.push({
                            "date": key,
                            "path": value
                        });
                    });
                    pdfList.pdfTable = pdfDetails;
                    return pdfList.pdfTable;
                } else {
                    document.getElementById("pdfTable").innerHTML = "There is no Test Report for this Test yet.";
                }
            });
        }

        /**
         * [Public]
         *
         * Performs a server request to get the information if a test report for a specific test exists or not.
         *
         * @param testId Id of the test to check
         * @returns {*}
         */
        function pdfExists(testId) {
            return $http.get(URL_REPORT_EXISTS + testId);
        }


        /**
         * [Public]
         *
         * Sends a server request in order to edit the configurations of the test "useNewData",
         * so that the latest values of a specific test are reused in the new execution or not
         *
         * @param testId The id of the test the config should be edited
         * @param useNewData boolean if the test should generate new data
         * @returns {*}
         */
        function editConfig(testId, useNewData) {
            if (useNewData === true) {
                return $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "false").success(function success(response) {
                    return response.success; // Update the list of sensors included in the test for the Chart view
                });
            } else if (useNewData === false) {
                return $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "true").success(function success(response) {
                    return response.success; // Update the list of sensors included in the test for the Chart view
                });
            }
        }

        /**
         * [Public]
         *
         * Performs a server request to register the testing device.
         * @returns {*|void}
         */
        function registerTestDevice() {
            return $http.post(URL_TESTDEVICE_REGISTER);
        }

        /**
         * [Public]
         *
         * Performs a server request to register the testing actuator.
         * @returns {*|void}
         */
        function registerTestActuator() {
            return $http.post(URL_TESTACTUATOR_REGISTER);

        }

        /**
         * [Public]
         *
         * Performs a server request to register the a one dimensional sensor simulator.
         * @returns {*|void}
         */
        function registerOneDimSensor(sensor) {
            return $http.post(URL_ONEDIM_SENSOR_REGISTER, sensor);

        }


        /**
         * [Private]
         *
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
         * Gets all configurations of a test entered by the user to save it.
         *
         * @param sensors of the test for which the configurations should be saved
         * @param realSensors list of real sensors included in the test
         * @param parameterValues list of parameter values of the real sensors
         * @param config configurations about the sensors included in the test made by the user
         * @param rules selected rules to be observed in the test
         * @param executeRules information if rules should be triggered through the test
         * @param data object
         */
        function getTestData(sensors, realSensors, parameterValues, config, rules, executeRules, data) {

            console.log("Bin im TestService");
            console.log(config);
            console.log(sensors);


            // to check if the user has selected at least one sensor
            let checkRealSensor = false;
            let checkSimSensor = false;

            // Test Object in which the configuration data should be saved
            let newTestObject = {};
            newTestObject.config = [];
            newTestObject.type = [];

            // random values Angle and Axis for the GPS-Sensor
            const randomAngle = Math.floor((Math.random() * 361));
            const randomAxis = Math.floor((Math.random() * 3));


            // random values for the direction of the outlier and movement for the acceleration Sensor
            const directionOutlier = Math.floor(Math.random() * 6);
            const directionMovement = Math.floor(Math.random() * 6);


            try {

                if (!angular.isUndefined(realSensors)) {
                    if (!angular.isUndefined(vm.parameterVal)) {
                        for (let x = 0; x < realSensors.length; x++) {
                            newTestObject.type.push(realSensors[x].name);
                        }

                        checkRealSensor = true;
                        angular.forEach(vm.parameterVal, function (parameters, key) {
                            for (let i = 0; i < realSensors.length; i++) {
                                if (realSensors[i].name === key) {
                                    vm.parameterValues = [];
                                    vm.parameterValues.push({
                                        "name": "ConfigName",
                                        "value": realSensors[i].name
                                    });
                                    const requiredParams = realSensors[i]._embedded.operator.parameters;


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


                if (!angular.isUndefined(sensors)) {
                    checkSimSensor = true;

                    // Define the types of sensors included in the test
                    for (let sensor = 0; sensor < sensors.length; sensor++) {
                        newTestObject.type.push(sensors[sensor]);
                    }

                    if (sensors.includes(SIMULATOR_LIST.TEMPERATURE)) {

                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.TEMPERATURE
                        });
                        if (config.eventTemp === '3' || config.eventTemp === '4' || config.eventTemp === '5' || config.eventTemp === '6') {
                            console.log("in event Temp");
                            console.log(parameterValues);
                            parameterValues.push({
                                "name": "event",
                                "value": config.eventTemp
                            });
                            console.log(parameterValues);
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});
                            console.log(parameterValues);
                        } else {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventTemp)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "room", "value": config.roomTemp});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyTemp)
                            });
                        }
                        newTestObject.config.push(parameterValues);
                        console.log(newTestObject);


                    }

                    if (sensors.includes(SIMULATOR_LIST.HUMIDITY)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.HUMIDITY
                        });
                        if (config.eventHum === '3' || config.eventHum === '4' || config.eventHum === '5' || config.eventHum === '6') {

                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventHum)
                            });
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});

                        } else {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventHum)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "room", "value": config.roomHum});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyHum)
                            });
                        }

                        newTestObject.config.push(parameterValues);
                    }

                    if (sensors.includes(SIMULATOR_LIST.TEMPERATURE_PL)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.TEMPERATURE_PL
                        });
                        if (config.eventTempPl === '3' || config.eventTempPl === '4' || config.eventTempPl === '5' || config.eventTempPl === '6') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventTempPl)
                            });
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        } else {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventTempPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                            parameterValues.push({"name": "room", "value": config.roomTempPl});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyTempPl)
                            });

                        }
                        newTestObject.config.push(parameterValues);
                    }

                    if (sensors.includes(SIMULATOR_LIST.HUMIDITY_PL)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.HUMIDITY_PL
                        });
                        if (config.eventHumPl === '3' || config.eventHumPl === '4' || config.eventHumPl === '5' || config.eventHumPl === '6') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventHumPl)
                            });
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        } else {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventHumPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                            parameterValues.push({"name": "room", "value": config.roomHumPl});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyHumPl)
                            });

                        }
                        newTestObject.config.push(parameterValues);
                    }

                    if (sensors.includes(SIMULATOR_LIST.GPS)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.GPS
                        });
                        if (config.eventGPS === '3' || config.eventGPS === '4' || config.eventGPS === '5') {
                            parameterValues.push({"name": "who", "value": config.whoGPS});
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventGPS)
                            });
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "latitude",
                                "value": config.latitudeGPS
                            });
                            parameterValues.push({
                                "name": "longitude",
                                "value": config.longitudeGPS
                            });
                            parameterValues.push({"name": "hight", "value": config.hightGPS});
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": config.reactionMetersGPS
                            });
                            parameterValues.push({"name": "randomAngle", "value": randomAngle});
                            parameterValues.push({"name": "axis", "value": randomAxis});
                        } else {
                            parameterValues.push({"name": "who", "value": config.whoGPS});
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventGPS)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "latitude",
                                "value": config.latitudeGPS
                            });
                            parameterValues.push({
                                "name": "longitude",
                                "value": config.longitudeGPS
                            });
                            parameterValues.push({"name": "hight", "value": config.heightGPS});
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": config.reactionMetersGPS
                            });
                            parameterValues.push({"name": "randomAngle", "value": randomAngle});
                            parameterValues.push({"name": "axis", "value": randomAxis});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyGPS)
                            });
                        }

                        newTestObject.config.push(parameterValues);
                    }
                    if (sensors.includes(SIMULATOR_LIST.GPS_PL)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.GPS_PL
                        });
                        if (config.eventGPSPl === '3' || config.eventGPSPl === '4' || config.eventGPSPl === '5') {

                            parameterValues.push({"name": "who", "value": config.whoGPSPl});
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventGPSPl)
                            });
                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "latitude",
                                "value": config.latitudeGPSPl
                            });
                            parameterValues.push({
                                "name": "longitude",
                                "value": config.longitudeGPSPl
                            });
                            parameterValues.push({"name": "hight", "value": config.heightGPSPl});
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": config.reactionMetersGPSPl
                            });
                            parameterValues.push({"name": "randomAngle", "value": randomAngle});
                            parameterValues.push({"name": "axis", "value": randomAxis});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        } else {
                            parameterValues.push({"name": "who", "value": config.whoGPSPl});
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventGPSPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "latitude",
                                "value": config.latitudeGPSPl
                            });
                            parameterValues.push({
                                "name": "longitude",
                                "value": config.longitudeGPSPl
                            });
                            parameterValues.push({"name": "hight", "value": config.heightGPSPl});
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": config.reactionMetersGPSPl
                            });
                            parameterValues.push({"name": "randomAngle", "value": randomAngle});
                            parameterValues.push({"name": "axis", "value": randomAxis});
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyGPSPl)
                            });
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        }

                        newTestObject.config.push(parameterValues);
                    }
                    if (sensors.includes(SIMULATOR_LIST.ACCELERATION)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.ACCELERATION
                        });
                        if (config.eventAcc === '3' || config.eventAcc === '4' || config.eventAcc === '5') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAcc)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "weightObject", "value": 0});
                            parameterValues.push({"name": "sensitivityClass", "value": 0});
                            parameterValues.push({"name": "reactionMeters", "value": 3});
                        } else if (config.event === '2') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAcc)
                            });
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyAcc)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "weightObject",
                                "value": parseInt(config.weightObjectAcc)
                            });
                            parameterValues.push({
                                "name": "sensitivityClass",
                                "value": parseInt(config.sensitivityAcc)
                            });
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": parseInt(config.reactionMetersAcc)
                            });
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });
                        } else if (config.event === '1') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAcc)
                            });
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyAcc)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            parameterValues.push({"name": "weightObject", "value": 0});
                            parameterValues.push({"name": "sensitivityClass", "value": 0});
                            parameterValues.push({"name": "reactionMeters", "value": 3});
                        }
                        newTestObject.config.push(parameterValues);
                    }

                    if (sensors.includes(SIMULATOR_LIST.ACCELERATION_PL)) {
                        parameterValues = [];
                        parameterValues.push({
                            "name": "ConfigName",
                            "value": SIMULATOR_LIST.ACCELERATION
                        });
                        if (config.eventAccPl === '3' || config.eventAccPl === '4' || config.eventAccPl === '5') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAccPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            parameterValues.push({"name": "anomaly", "value": 0});
                            parameterValues.push({"name": "weightObject", "value": 0});
                            parameterValues.push({"name": "sensitivityClass", "value": 0});
                            parameterValues.push({"name": "reactionMeters", "value": 3});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        } else if (config.event === '2') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAccPl)
                            });
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyAccPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "weightObject",
                                "value": parseInt(config.weightObjectAccPl)
                            });
                            parameterValues.push({
                                "name": "sensitivityClass",
                                "value": parseInt(config.sensitivityAccPl)
                            });
                            parameterValues.push({
                                "name": "reactionMeters",
                                "value": parseInt(config.reactionMetersAccPl)
                            });
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        } else if (config.event === '1') {
                            parameterValues.push({
                                "name": "event",
                                "value": parseInt(config.eventAccPl)
                            });
                            parameterValues.push({
                                "name": "anomaly",
                                "value": parseInt(config.anomalyAccPl)
                            });
                            parameterValues.push({"name": "useNewData", "value": true});
                            parameterValues.push({
                                "name": "directionAnomaly",
                                "value": directionOutlier
                            });
                            parameterValues.push({
                                "name": "directionMovement",
                                "value": directionMovement
                            });

                            parameterValues.push({"name": "weightObject", "value": 0});
                            parameterValues.push({"name": "sensitivityClass", "value": 0});
                            parameterValues.push({"name": "reactionMeters", "value": 3});
                            parameterValues.push({"name": "simTime", "value": config.simTime});
                            parameterValues.push({
                                "name": "amountEvents",
                                "value": config.amountEvents
                            });
                            parameterValues.push({
                                "name": "amountAnomalies",
                                "value": config.amountAnomalies
                            });
                        }
                        newTestObject.config.push(parameterValues);
                    }


                }


                for (let property in data) {
                    if (data.hasOwnProperty(property)) {
                        newTestObject[property] = data[property];
                    }
                }


            } catch (e) {
                parameterValues = [];
                newTestObject.type = [];
                parameterValues.push({
                    "name": "ConfigName",
                    "value": 'ERROR'
                });
                parameterValues.push({
                    "name": "event",
                    "value": 0
                });
                parameterValues.push({"name": "anomaly", "value": 0});
                parameterValues.push({"name": "useNewData", "value": true});
                newTestObject.config.push(parameterValues);

                console.log("catched error")
            }

            // define if data should be reused
            newTestObject.useNewData = true;

            // define the rules and if they should be triggered during the test
            newTestObject.rules = rules;
            const radios = document.getElementsByName('executeRules');
            let i = 0;
            const length = radios.length;
            for (; i < length; i++) {
                if (radios[i].checked) {
                    var executeRulesTemp = radios[i].value;
                    break;
                }
            }


            if (checkSimSensor === false && checkRealSensor === false) {
                NotificationService.notify('Choose at least one sensor', 'error')
            }


            if (executeRulesTemp === 'undefined') {
                NotificationService.notify('A decision must be made.', 'error')
            }

            vm.executeRules = executeRulesTemp === 'true';
            newTestObject.triggerRules = executeRules;

            return newTestObject;


        }

        //Expose public methods
        return {
            executeTest: executeTest,
            stopTest: stopTest,
            getPDFList: getPDFList,
            editConfig: editConfig,
            getTestData: getTestData,
            pdfExists:pdfExists,
            registerTestDevice:registerTestDevice,
            registerTestActuator:registerTestActuator,
            registerOneDimSensor:registerOneDimSensor
        }
    }
]);

