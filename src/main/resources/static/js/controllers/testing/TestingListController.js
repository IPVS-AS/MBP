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
            };

            const RERUN_IDENTIFIER = 'RERUN_';


            const vm = this;
            vm.ruleList = [];
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


                checkSensorReg();
                getRealSensors();
                ruleListSelection();


                $scope.realSensorList = vm.realSensorList;
                $scope.availableSensors = vm.availableSensors;


                //Refresh test select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });

            })();


            function ruleListSelection() {
                angular.forEach(ruleList, function (rule) {
                    if (!rule.name.includes(RERUN_IDENTIFIER)) {
                        vm.ruleList.push(rule);

                    }
                });

            }


            /**
             * [Public]
             *
             * Check if the Sensor-Simulator for the Test is registered.
             *
             * @param sensorSimulator to be checked
             */

            function checkSensorReg() {
                // go through every registered sensor and search for the sensor simulator
                if (sensorList.length > 0) {
                    angular.forEach(sensorList, function (sensor) {
                        if (Object.values(SIMULATOR_LIST).indexOf(sensor.name) > -1) {
                            vm.availableSensors.push(sensor.name);
                        }
                    })
                }
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
                        if (simulator === sensor.name) {
                            realSensor = false;
                        }
                    });

                    if (sensor.name.includes(RERUN_IDENTIFIER)) {
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
                            let selectedSimulators = vm.selectedSensors;

                            let parameterValuesReal = vm.parameterVal;



                            const newTestObject = TestService.getTestData(selectedSimulators, selectedSensorsReal, parameterValuesReal, vm.config, vm.rules, ruleList, vm.executeRules, data);


                            return addTest(newTestObject);

                        }
                    }),
                deleteTestCtrl: $controller('DeleteTestController as deleteTestCtrl', {
                    $scope: $scope,
                    deleteItem: deleteTest,
                    confirmDeletion: confirmDelete
                }),

                accessControlPolicyList: accessControlPolicyList,


                addSimulators: addSimulators,
                addRealSensor: addRealSensor,
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

