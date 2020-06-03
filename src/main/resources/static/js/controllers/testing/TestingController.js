/* global app */


/**
 * Controller for the sensor list page.
 */
app.controller('TestingController',
    ['$scope', '$controller', '$interval', '$http', 'testList', 'addTest', 'deleteTest', 'ruleList', '$q', 'ComponentService', 'FileReader', 'ENDPOINT_URI',
        function ($scope, $controller, $interval, $http, testList, addTest, deleteTest, ruleList, $q, ComponentService, FileReader, ENDPOINT_URI) {

            var vm = this;
            vm.ruleList = ruleList;
            //Stores the parameters and their values as assigned by the user
            vm.parameterValues = [];
            //Settings objects that contains application settings for this page
            vm.useNewData = true;
            vm.testName = "";
            vm.rulesPDF = [];


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if the test list was retrieved successfully
                if (testList == null) {
                    NotificationService.notify("Could not retrieve test list.", "error");
                }
                //Refresh test select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });




            })();

            /**
             * Performs a server request in order to start a test given by its id.
             *
             * @param testId
             * @param item
             */
            function executeTest(testId, item) {
                console.log(testList);
                $http.post(ENDPOINT_URI + '/test-details/test/' + testId, testId.toString()).success(function successCallback(responseTest) {
                }, function (response) {
                    //Server request failed
                    console.log("Not successful");
                });
            }

            /**
             * Performs a server request in order to stop a test given by its id.
             *
             * @param testId
             */
            function stopTest(testId) {
                vm.http = $http.post(ENDPOINT_URI + '/test-details/test/stop/' + testId, testId.toString()).then(function (response) {
                }, function (response) {
                    //Server request failed
                    console.log("Not successful");
                });

            }


            /**
             * Sends a server request to find out if a test report is available for the specific test.
             *
             * @param testId
             * @param testName
             */
            function refreshTestEntry(testId, testName) {
                $http.get(ENDPOINT_URI + '/test-details/pdfExists/' + testId).success(function (response) {
                    if (response === true) {
                        document.getElementById(testName).disabled = false;
                        console.log(document.getElementById(testName).disabled);
                    } else {
                        document.getElementById(testName).disabled = true;
                        console.log(document.getElementById(testName).disabled);
                    }
                });
            }


            /**
             * Sends a server request to open the test report of a specific test fiven by its id.
             *
             * @param testID
             */
            function downloadPDF(testID) {
                window.open('api/test-details/downloadPDF/' + testID, '_blank');
            }






            function getDevice() {
                $http.get(ENDPOINT_URI + '/devices/search/findAll').success(function (response) {

                    $scope.device= 'LOADING';


                    console.log(response._embedded.devices);
                    $scope.device = "NOT_REGISTERED";
                    angular.forEach(response._embedded.devices, function (value) {
                        console.log(value.name);
                        if (value.name === "TestingDevice") {
                            $scope.device = "REGISTERED";
                        }

                    });

                });

            }



            function registerTestDevice(){
                param = {"username":"ubuntu","password":"","name":"TestingDevice","componentType":"Computer","ipAddress":"192.168.221.167","rsaKey":"-----BEGIN RSA PRIVATE KEY-----\nMIIEqgIBAAKCAQEA4zzAJS+xXz00YLyO8lvsSfY+6XX1mQs6pz7yioA6lO30mWMx\n95FP3rZiX2stId3VfEPKdPgLot7CoTMcSnQzDBR8bi1ej8c/FXzELb2kTQzZE7dX\nYaONvNfKGL27EMjhqRpL+rQeTGeyGqmr0WH7BeQ9nE6ylfxXzXAMWkTW6dv7go+j\n52xAS6dM5TZER/A2KvCgXisiQFzwqEHuXnoy9lpWHcSZzQL8Xkd9ZbGAr3ex0pEc\n8220d4KT8oATLBDZo/fJyGiQNR5sab8RlpecbGJoh0QJIdnU3Eq02HYSzAQ7a8cB\nYGEm1xtOQOxV2a4+8f/g9FSC3hjobwmSoNu/nQIDAQABAoIBACy3ytRGk3A7mjAj\nSzo0jsZrWCwXU5KfnBZHk/FflKe0QDtjQvUGOqKIX8mJTONqRVXj/VaRbbDKh6Cz\nbzDTtyv8aBRCh2Zh/m8bE3ww4sFq8tknbmG/jugHyzSdOc/uyEG/9A3NHl1I1sra\ncv6MeprJNLqq3ggYFatPDo9BFs4EZoaIxEMD3plHfENfJOu7IS0xRoe5foXYbnM3\nji7n243OBGPAdCZXJkhYNgRoZmwMeMOJWK7EmiiM60ZKpHl8C4jSuzQ132aK/NDH\n3xgr+1nI8i8CfAWBlP8hfCXJJ8EiS5lE94jnP7u49BhjbPgULaNPDDYpVh5/uTlP\nYV5iAcECggCBAO7y4xBuMc8G57lqepoZHtCjSPpXTEC6hE7+pmnwvi6gUZPV7Tx/\nC7JecekilTy3Z+MjU1jwy7Bu0L3EJsJBn2N5aGYVxGFHGqrfA/qhPeyJsIU2w2UZ\nm1BEgNjP7bMZSMCSYd2CU9mP5dt3vGpEU6oZgwe9jm5QghYVjHaB6NUNAoIAgQDz\nc+sqdCn+rIpE6uovtqXJ9l3psaz+egRLcWS0ZVtrdhmMPBz/rZ16KhqmA+aszjiP\n8JqO3uXiB1LR+ACc6tRzeCWXNWipgzJGvLfgZBwGHeFje+uMyd1nYUq3qd0zP81j\ntpZpIAlHlPc+UREqiUhJJkjP+tEwwznP4zaC4wkQ0QKCAIEA3bMRpf73y8P2X/xB\nQJSqGJ5Haa5xm2TyuXBf6s9pRU2OIwJLmOOvcJFcUxi5Kppok0AFZvITquFGX6uM\n4pOMVPkiOgVcLX2RapR81p+gGsUtuIu1AyqdBf5pJcDWJGQDMlke4Cy5q5RtihEw\nCdDXZ21AO4BOlF+yMtdPeezSoEkCggCBAIBzsiolPp8sRIxWcpgYQ+OLBUQvxjpD\nAQ8ZVmxEancJyjMO6LIS1dtGaeccedLFwFxaNAKcIykeehllRFWHJe+C/jqJKJ8A\nJT/jhRV1XL/xdiG6ma8gN5y7XeQIUTkgOeuZxETVbXACbm3H8knCQ4ytEZADI+sZ\npuBEX1eyGO9xAoIAgQCwh2QkYnGPQkY4d9ra+WcrUz8sb7CEu4IS42XNt8SFkyU4\nfkE7aE4bHufPnnTEZ4jIGk0/E1b8AhLh1suRpg3tltYEj5CJfF1UywoUGuHhQkzP\n7jZaNQ1xdB+0woK3IenLVpDjxWGZbZTxviim1v1lpLSJxfr/HfvW1DJc4x/iug==\n-----END RSA PRIVATE KEY-----","errors":{}};
                $http.post(ENDPOINT_URI + '/devices/', param).then(function success(response) {
                });
                getDevice();
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain test.
             *
             * @param data A data object that contains the id of the test that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {

                var testId = data.id;
                var testName = "";

                //Determines the tests's name by checking the list
                for (var i = 0; i < testList.length; i++) {
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
                            //Extend request parameters for routines and parameters
                            var parameters;
                            // random values Angle and Axis for the GPS-Sensor
                            var randomAngle = Math.floor((Math.random() * 361));
                            var randomAxis = Math.floor((Math.random() * 3));

                            // random values for the direction of the outlier and movoment for the acceleration Sensor
                            var directionOutlier = Math.floor(Math.random() * 6);
                            var directionMovement = Math.floor(Math.random() * 6);

                            if (vm.data.singleSelect === 'TestingTemperaturSensor' || vm.data.singleSelect === 'TestingFeuchtigkeitsSensor') {
                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5' || vm.testCase.singleSelect === '6') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                } else {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "room", "value": vm.room.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                }
                            } else if (vm.data.singleSelect === 'TestingTemperaturSensorPl' || vm.data.singleSelect === 'TestingFeuchtigkeitsSensorPl') {

                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5' || vm.testCase.singleSelect === '6') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                } else {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                    vm.parameterValues.push({"name": "room", "value": vm.room.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });

                                }
                            } else if (vm.data.singleSelect === 'TestingGPSSensor') {
                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5') {
                                    vm.parameterValues.push({"name": "who", "value": vm.humCat.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "latitude", "value": vm.latitude.singleSelect});
                                    vm.parameterValues.push({"name": "longitude", "value": vm.longitude.singleSelect});
                                    vm.parameterValues.push({"name": "hight", "value": vm.hight.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": vm.ractionMeters.singleSelect
                                    });
                                    vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                    vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                } else {
                                    vm.parameterValues.push({"name": "who", "value": vm.humCat.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "latitude", "value": vm.latitude.singleSelect});
                                    vm.parameterValues.push({"name": "longitude", "value": vm.longitude.singleSelect});
                                    vm.parameterValues.push({"name": "hight", "value": vm.hight.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": vm.ractionMeters.singleSelect
                                    });
                                    vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                    vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                }
                            } else if (vm.data.singleSelect === 'TestingGPSSensorPl') {
                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5') {

                                    vm.parameterValues.push({"name": "who", "value": vm.humCat.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "latitude", "value": vm.latitude.singleSelect});
                                    vm.parameterValues.push({"name": "longitude", "value": vm.longitude.singleSelect});
                                    vm.parameterValues.push({"name": "hight", "value": vm.hight.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": vm.ractionMeters.singleSelect
                                    });
                                    vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                    vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                } else {
                                    vm.parameterValues.push({"name": "who", "value": vm.humCat.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "latitude", "value": vm.latitude.singleSelect});
                                    vm.parameterValues.push({"name": "longitude", "value": vm.longitude.singleSelect});
                                    vm.parameterValues.push({"name": "hight", "value": vm.hight.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": vm.ractionMeters.singleSelect
                                    });
                                    vm.parameterValues.push({"name": "randomAngle", "value": randomAngle});
                                    vm.parameterValues.push({"name": "axis", "value": randomAxis});
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                }

                            } else if (vm.data.singleSelect === 'TestingBeschleunigungsSensor') {
                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});

                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "weightObject", "value": 0});
                                    vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                    vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                } else if (vm.testCase.singleSelect === '2') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({
                                        "name": "weightObject",
                                        "value": parseInt(vm.kg.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "sensitivityClass",
                                        "value": parseInt(vm.sensitivity.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": parseInt(vm.reactionMeter.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});
                                } else if (vm.testCase.singleSelect === '1') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});

                                    vm.parameterValues.push({"name": "weightObject", "value": 0});
                                    vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                    vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                }
                            } else if (vm.data.singleSelect === 'TestingBeschleunigungsSensorPl') {
                                if (vm.testCase.singleSelect === '3' || vm.testCase.singleSelect === '4' || vm.testCase.singleSelect === '5') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});

                                    vm.parameterValues.push({"name": "anomaly", "value": 0});
                                    vm.parameterValues.push({"name": "weightObject", "value": 0});
                                    vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                    vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                } else if (vm.testCase.singleSelect === '2') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({
                                        "name": "weightObject",
                                        "value": parseInt(vm.kg.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "sensitivityClass",
                                        "value": parseInt(vm.sensitivity.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "reactionMeters",
                                        "value": parseInt(vm.reactionMeter.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                } else if (vm.testCase.singleSelect === '1') {
                                    vm.parameterValues.push({
                                        "name": "event",
                                        "value": parseInt(vm.testCase.singleSelect)
                                    });
                                    vm.parameterValues.push({
                                        "name": "anomaly",
                                        "value": parseInt(vm.combination.singleSelect)
                                    });
                                    vm.parameterValues.push({"name": "useNewData", "value": true});
                                    vm.parameterValues.push({"name": "directionAnomaly", "value": directionOutlier});
                                    vm.parameterValues.push({"name": "directionMovement", "value": directionMovement});

                                    vm.parameterValues.push({"name": "weightObject", "value": 0});
                                    vm.parameterValues.push({"name": "sensitivityClass", "value": 0});
                                    vm.parameterValues.push({"name": "reactionMeters", "value": 3});
                                    vm.parameterValues.push({"name": "simTime", "value": vm.simTime.singleSelect});
                                    vm.parameterValues.push({
                                        "name": "amountEvents",
                                        "value": vm.simEvent.singleSelect
                                    });
                                    vm.parameterValues.push({
                                        "name": "amountAnomalies",
                                        "value": vm.simOutlier.singleSelect
                                    });
                                }
                            }

                            var newTestObject = {};

                            for (var property in data) {
                                if (data.hasOwnProperty(property)) {
                                    newTestObject[property] = data[property];
                                }
                            }

                            newTestObject.config = vm.parameterValues;
                            newTestObject.type = vm.data.singleSelect;

                            newTestObject.rules = vm.rules;

                            var test = vm.executeRules.singleSelect;
                            vm.test2 = test === 'true';
                            newTestObject.triggerRules = vm.test2;

                            return addTest(newTestObject);

                        }
                    }),
                deleteTestCtrl: $controller('DeleteItemController as deleteTestCtrl', {
                    $scope: $scope,
                    deleteItem: deleteTest,
                    confirmDeletion: confirmDelete
                }),
                executeTest: executeTest,
                editConfig: editConfig,
                stopTest: stopTest,
                downloadPDF: downloadPDF,
                refreshTestEntry: refreshTestEntry,
                getDevice: getDevice,
                registerTestDevice: registerTestDevice

            });
            // $watch 'addTest' result and add to 'testList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addTestCtrl.result;
                },
                function () {
                    //Callback
                    var test = vm.addTestCtrl.result;

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
                    var id = vm.deleteTestCtrl.result;
                    vm.testListCtrl.removeItem(id);
                }
            );

        }
    ]);