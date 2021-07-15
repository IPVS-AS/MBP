/* global app */

app.controller('OperatorListController',
    ['$scope', '$rootScope', '$controller', '$q', 'operatorList', 'dataModelList', 'operatorPreprocessing', 'addOperator', 'deleteOperator', 'FileReader', 'parameterTypesList', 'OperatorService', 'NotificationService',
        function ($scope, $rootScope, $controller, $q, operatorList, dataModelList, operatorPreprocessing, addOperator, deleteOperator, FileReader, parameterTypesList, OperatorService, NotificationService) {
            let vm = this;

            vm.dataModelList = dataModelList;

            // Constant list of the operators for the sensor simulators, that can be included in the test
            const SIMULATOR_LIST = {
                TEMPERATURE: 'TESTING_TemperatureSensor',
                TEMPERATURE_PL: 'TESTING_TemperatureSensorPl',
                HUMIDITY: 'TESTING_HumiditySensor',
                HUMIDITY_PL: 'TESTING_HumiditySensorPl',
                ACTUATOR: 'TESTING_Actuator'
            };

            vm.dzServiceOptions = {
                paramName: 'serviceFile',
                maxFilesize: '100',
                maxFiles: 1
            };

            vm.dzServiceCallbacks = {
                'addedfile': function (file) {
                    vm.addOperatorCtrl.item.serviceFile = file;
                }
            };
            vm.dzRoutinesOptions = {
                paramName: 'routinesFile',
                addRemoveLinks: true,
                previewTemplate: document.querySelector('#tpl').innerHTML,
                createImageThumbnails: false,
                maxFilesize: '100',
                maxFiles: 99
            };


            vm.dzRoutinesCallbacks = {
                'addedfile': function (file) {
                    if (!vm.addOperatorCtrl.item.routineFiles) {
                        vm.addOperatorCtrl.item.routineFiles = [];
                    }
                    vm.addOperatorCtrl.item.routineFiles.push(file);
                },
                'removedfile': function (file) {
                    //Sanity check
                    if (vm.addOperatorCtrl.item.routineFiles) {
                        vm.addOperatorCtrl.item.routineFiles.splice(vm.addOperatorCtrl.item.routineFiles.indexOf(file), 1);
                    } else {
                        vm.addOperatorCtrl.item.routineFiles = [];
                    }
                },

            };

            //Dropzone methods are injected into this object
            vm.dzMethods = {};

            //List of added parameters
            vm.parameters = [];

            //If a message broker with OAuth is used, each operator must specify a device code parameter
            let deviceCodeParameter = {
                name: "device_code",
                type: "Text",
                unit: "",
                mandatory: true
            };

            //Decide whether the device code parameter is needed
            if ($rootScope.hasOwnProperty("mbpinfo") &&
                (["LOCAL_SECURE", "REMOTE_SECURE"].includes($rootScope.mbpinfo.brokerLocation))) {
                vm.parameters = [deviceCodeParameter];
            }

            //Set parameter types list
            vm.parameterTypes = parameterTypesList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                getListWoSimulators()
                //Validity check for parameter types
                if (parameterTypesList.length < 1) {
                    NotificationService.notify("Could not load parameter types.", "error");
                }

                //Modify each operator according to the preprocessing function (if provided)
                if (operatorPreprocessing) {
                    for (let i = 0; i < operatorList.length; i++) {
                        operatorPreprocessing(operatorList[i]);
                    }
                }

                // Refresh select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            //public
            function addParameter() {
                let parameter = {
                    name: "",
                    type: "",
                    unit: "",
                    mandatory: false
                };
                vm.parameters.push(parameter);
            }

            //public
            function deleteParameter(index) {
                vm.parameters.splice(index, 1);
            }

            /**
             * Reads given files from the user's disk and returns a promise
             * containing the combined promises for all asynchronous
             * file read operations.
             *
             * @param files The files to read
             * @returns {*} The combined result promises
             */
            function readFiles(files) {
                //Sanity check
                if ((files === undefined) || files.constructor !== Array) {
                    return $q.all([]);
                }

                return FileReader.readMultipleAsDataURL(files, $scope);
            }

            function getListWoSimulators() {
                let tempOperatorList = operatorList;

                angular.forEach(SIMULATOR_LIST, function (value) {
                    operatorList.some(function (operator) {
                        if (operator.name === value) {
                            const index = tempOperatorList.indexOf(operator);
                            if (index !== -1) {
                                tempOperatorList.splice(index, 1)
                            }
                        }
                    });

                });

                $scope.simExists = tempOperatorList.length;

            }


            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain operator. It also
             * shows a list of all components that are affected by this deletion.
             *
             * @param data A data object that contains the id of the operator that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let operatorId = data.id;
                let operatorName = "";

                //Determines the operator's name by checking all operators in the operator list
                for (let i = 0; i < operatorList.length; i++) {
                    if (operatorId === operatorList[i].id) {
                        operatorName = operatorList[i].name;
                        break;
                    }
                }

                //Ask the server for all components that use this operator
                return OperatorService.getUsingComponents(data.id).then(function (result) {
                    //Check if list is empty
                    if (result.length > 0) {
                        //Not empty, entity cannot be deleted
                        let errorText = "The operator <strong>" + operatorName + "</strong> is still used by the " +
                            "following components and thus cannot be deleted:<br/><br/>";

                        //Iterate over all affected components
                        for (let i = 0; i < result.length; i++) {
                            errorText += "- " + result[i].name + " (" + result[i].component + ")<br/>";
                        }


                        // Show error message
                        Swal.fire({
                            icon: 'error',
                            title: 'Deletion impossible',
                            html: errorText
                        })

                        // Return new promise as result
                        return Promise.resolve({value: false});
                    }

                    //Show confirm prompt to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete operator',
                        icon: 'warning',
                        html: "Are you sure you want to delete the operator \"<strong>" + operatorName + "</strong>\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                addParameter: addParameter,
                deleteParameter: deleteParameter,
                operatorListCtrl: $controller('ItemListController as operatorListCtrl',
                    {
                        $scope: $scope,
                        list: operatorList
                    }),
                addOperatorCtrl: $controller('AddItemController as addOperatorCtrl',
                    {
                        $scope: $scope,
                        entity: 'operator',
                        addItem: function (data) {
                            //Extend request parameters for routines, their hashes and parameters
                            return readFiles(data.routineFiles)
                                .then(function (response) {
                                    data.unit = data.unit || "";
                                    data.routines = response;
                                    data.parameters = vm.parameters;
                                    return addOperator(data);
                                }, function (response) {
                                    return $q.reject(response);
                                });
                        }
                    }),
                deleteOperatorCtrl: $controller('DeleteItemController as deleteOperatorCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteOperator,
                        confirmDeletion: confirmDelete
                    }),
            });

            // $watch 'addItem' result and add to 'itemList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addOperatorCtrl.result;
                },
                function () {
                    //Callback
                    let data = vm.addOperatorCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addOperatorModal").modal('toggle');

                        //Call pre processing function
                        if (operatorPreprocessing) {
                            operatorPreprocessing(data);
                        }

                        //Add new item to list
                        vm.operatorListCtrl.pushItem(data);

                        getListWoSimulators();

                        //Clear dropzone files
                        vm.dzMethods.removeAllFiles(true);

                        //Clear parameter array
                        vm.parameters.length = 0;
                    }
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteOperatorCtrl.result;
                },
                function () {
                    let id = vm.deleteOperatorCtrl.result;
                    vm.operatorListCtrl.removeItem(id);
                    getListWoSimulators();
                }
            );

            //Enable popovers
            $(document).ready(function () {
                $('[data-toggle="popover"]').popover();
            });
        }
    ]
);
