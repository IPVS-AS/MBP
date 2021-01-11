/* global app */

app.controller('OperatorListController',
    ['$scope', '$controller', '$q', 'operatorList', 'operatorPreprocessing', 'addOperator', 'deleteOperator', 'FileReader', 'parameterTypesList', 'OperatorService', 'NotificationService',
        function ($scope, $controller, $q, operatorList, operatorPreprocessing, addOperator, deleteOperator, FileReader, parameterTypesList, OperatorService, NotificationService) {
            var vm = this;

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
                    vm.addOperatorCtrl.item.routineFiles.splice(vm.addOperatorCtrl.item.routineFiles.indexOf(file), 1);
                },

            };

            vm.dzMethods = {};

            /**
             * The device code parameter is necessary for every operator.
             * @type {{unit: string, name: string, type: string, mandatory: boolean}}
             */
            var deviceCodeParameter = {
                name: "device_code",
                type: "Text",
                unit: "",
                mandatory: true
            };
            vm.parameters = [deviceCodeParameter];
            vm.parameterTypes = parameterTypesList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for parameter types
                if (parameterTypesList.length < 1) {
                    NotificationService.notify("Could not load parameter types.", "error");
                }

                //Modify each operator according to the preprocessing function (if provided)
                if (operatorPreprocessing) {
                    for (var i = 0; i < operatorList.length; i++) {
                        operatorPreprocessing(operatorList[i]);
                    }
                }
            })();

            //public
            function addParameter() {
                var parameter = {
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

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain operator. It also
             * shows a list of all components that are affected by this deletion.
             *
             * @param data A data object that contains the id of the operator that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var operatorId = data.id;
                var operatorName = "";

                //Determines the operator's name by checking all operators in the operator list
                for (var i = 0; i < operatorList.length; i++) {
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
                    var data = vm.addOperatorCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addOperatorModal").modal('toggle');

                        //Call pre processing function
                        if (operatorPreprocessing) {
                            operatorPreprocessing(data);
                        }

                        //Add new item to list
                        vm.operatorListCtrl.pushItem(data);

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
                    var id = vm.deleteOperatorCtrl.result;
                    vm.operatorListCtrl.removeItem(id);
                }
            );

            //Enable popovers
            $(document).ready(function () {
                $('[data-toggle="popover"]').popover();
            });
        }
    ]
);
