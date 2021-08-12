/* global app */

/**
 * Controller for the device list page.
 */
app.controller('EntityTypesListController',
    ['$scope', '$controller', '$q',
        'deviceTypesList', 'actuatorTypesList', 'sensorTypesList', 'accessControlPolicyList',
        'addDeviceType', 'addActuatorType', 'addSensorType',
        'deleteDeviceType', 'deleteActuatorType', 'deleteSensorType', 'FileReader',
        function ($scope, $controller, $q, deviceTypesList, actuatorTypesList, sensorTypesList, accessControlPolicyList,
                  addDeviceType, addActuatorType, addSensorType,
                  deleteDeviceType, deleteActuatorType, deleteSensorType, FileReader) {
            let vm = this;

            vm.dzIconOptions = {
                paramName: 'icon',
                addRemoveLinks: true,
                previewTemplate: document.querySelector('#dropzone-icon-template').innerHTML,
                createImageThumbnails: true,
                maxFilesize: 1,
                maxFiles: 1,
                uploadMultiple: false,
                parallelUploads: 1,
                acceptedFiles: "image/*"
            };

            vm.dzDeviceIconMethods = {};
            vm.dzActuatorIconMethods = {};
            vm.dzSensorIconMethods = {};

            vm.dzDeviceTypeIconCallbacks = {
                'addedfile': function (file) {
                    if (vm.addDeviceTypeCtrl.item.icon && vm.addDeviceTypeCtrl.item.icon.length > 0) {
                        vm.dzDeviceIconMethods.removeFile(vm.addDeviceTypeCtrl.item.icon[0]);
                    } else {
                        vm.addDeviceTypeCtrl.item.icon = [];
                    }
                    vm.addDeviceTypeCtrl.item.icon[0] = file;
                },
                'removedfile': function (file) {
                    vm.addDeviceTypeCtrl.item.icon = [];
                }
            };

            vm.dzActuatorTypeIconCallbacks = {
                'addedfile': function (file) {
                    if (vm.addActuatorTypeCtrl.item.icon && vm.addActuatorTypeCtrl.item.icon.length > 0) {
                        vm.dzActuatorIconMethods.removeFile(vm.addActuatorTypeCtrl.item.icon[0]);
                    } else {
                        vm.addActuatorTypeCtrl.item.icon = [];
                    }
                    vm.addActuatorTypeCtrl.item.icon[0] = file;
                },
                'removedfile': function (file) {
                    vm.addActuatorTypeCtrl.item.icon = [];
                }
            };

            vm.dzSensorTypeIconCallbacks = {
                'addedfile': function (file) {
                    if (vm.addSensorTypeCtrl.item.icon && vm.addSensorTypeCtrl.item.icon.length > 0) {
                        vm.dzSensorIconMethods.removeFile(vm.addSensorTypeCtrl.item.icon[0]);
                    } else {
                        vm.addSensorTypeCtrl.item.icon = [];
                    }
                    vm.addSensorTypeCtrl.item.icon[0] = file;
                },
                'removedfile': function (file) {
                    vm.addSensorTypeCtrl.item.icon = [];
                }
            };

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                // Refresh policy select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function (e) {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * Reads a given file from the user's disk and returns a promise
             * for the file read operation.
             *
             * @param file The file to read
             * @returns {*} The result promise
             */
            function readFile(file) {
                //Sanity check
                if ((file === undefined) || (file == null)) {
                    return $q.reject();
                }

                //Read file
                return FileReader.readAsDataURL(file, $scope);
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain entity type.
             *
             * @param typeCategoryName Name of the entity type category
             * @param typesList Reference to the corresponding list of entity types
             * @param data A data object that contains the id of the entity type that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(typeCategoryName, typesList, data) {
                var entityTypeId = data.id;
                var entityTypeName = "";

                //Determines the entity type's name by checking the given list
                for (var i = 0; i < typesList.length; i++) {
                    if (entityTypeId === typesList[i].id) {
                        entityTypeName = typesList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete ' + typeCategoryName + ' type',
                    type: 'warning',
                    html: "Are you sure you want to delete " + typeCategoryName + " type \"" + entityTypeName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            // Expose controller
            angular.extend(vm, {
                deviceTypeListCtrl: $controller('ItemListController as deviceTypeListCtrl', {
                    $scope: $scope,
                    list: deviceTypesList
                }),
                actuatorTypeListCtrl: $controller('ItemListController as actuatorTypeListCtrl', {
                    $scope: $scope,
                    list: actuatorTypesList
                }),
                sensorTypeListCtrl: $controller('ItemListController as sensorTypeListCtrl', {
                    $scope: $scope,
                    list: sensorTypesList
                }),
                addDeviceTypeCtrl: $controller('AddItemController as addDeviceTypeCtrl', {
                    $scope: $scope,
                    entity: 'device type',
                    addItem: function (data) {
                        //Extend request for icon
                        return readFile(data.icon[0]).then(function (response) {
                            data.icon = response || null;
                            return addDeviceType(data);
                        }, function (response) {
                            return $q.reject(response);
                        });
                    }
                }),
                addActuatorTypeCtrl: $controller('AddItemController as addActuatorTypeCtrl', {
                    $scope: $scope,
                    entity: 'actuator type',
                    addItem: function (data) {
                        //Extend request for icon
                        return readFile(data.icon[0]).then(function (response) {
                            data.icon = response || null;
                            return addActuatorType(data);
                        }, function (response) {
                            return $q.reject(response);
                        });
                    }
                }),
                addSensorTypeCtrl: $controller('AddItemController as addSensorTypeCtrl', {
                    $scope: $scope,
                    entity: 'sensor type',
                    addItem: function (data) {
                        //Check if icon is available
                        if (!data.hasOwnProperty(("icon"))) {
                            data.icon = "";
                            return addSensorType(data);
                        }

                        //Read icon and extend request
                        return readFile(data.icon[0]).then(function (response) {
                            data.icon = response || null;
                            return addSensorType(data);
                        }, function (response) {
                            return $q.reject(response);
                        });
                    }
                }),
                deleteDeviceTypeCtrl: $controller('DeleteItemController as deleteDeviceTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDeviceType,
                    confirmDeletion: confirmDelete.bind(null, 'device', deviceTypesList)
                }),
                deleteActuatorTypeCtrl: $controller('DeleteItemController as deleteActuatorTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteActuatorType,
                    confirmDeletion: confirmDelete.bind(null, 'actuator', actuatorTypesList)
                }),
                deleteSensorTypeCtrl: $controller('DeleteItemController as deleteSensorTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteSensorType,
                    confirmDeletion: confirmDelete.bind(null, 'sensor', sensorTypesList)
                }),
                accessControlPolicyList: accessControlPolicyList
            });

            //Watch addition of device types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addDeviceTypeCtrl.result;
                },
                //Callback
                function () {
                    let data = vm.addDeviceTypeCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addDeviceTypeModal").modal('toggle');

                        //Reset dropzone
                        vm.addDeviceTypeCtrl.item.icon = [];
                        vm.dzDeviceIconMethods.removeAllFiles();

                        //Add new item to list
                        vm.deviceTypeListCtrl.pushItem(data);
                    }
                }
            );

            //Watch errors during addition of device types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addDeviceTypeCtrl.item.errors;
                },
                //Callback
                function () {
                    let error = vm.addDeviceTypeCtrl.item.errors;
                    if (error) {
                        //Reset dropzone
                        vm.addDeviceTypeCtrl.item.icon = [];
                        vm.dzDeviceIconMethods.removeAllFiles();
                    }
                }
            );

            //Watch addition of actuator types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addActuatorTypeCtrl.result;
                },
                //Callback
                function () {
                    let data = vm.addActuatorTypeCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addActuatorTypeModal").modal('toggle');

                        //Reset dropzone
                        vm.addActuatorTypeCtrl.item.icon = [];
                        vm.dzActuatorIconMethods.removeAllFiles();

                        //Add new item to list
                        vm.actuatorTypeListCtrl.pushItem(data);
                    }
                }
            );

            //Watch errors during addition of actuator types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addActuatorTypeCtrl.item.errors;
                },
                //Callback
                function () {
                    let error = vm.addActuatorTypeCtrl.item.errors;
                    if (error) {
                        //Reset dropzone
                        vm.addActuatorTypeCtrl.item.icon = [];
                        vm.dzActuatorIconMethods.removeAllFiles();
                    }
                }
            );

            //Watch addition of sensor types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addSensorTypeCtrl.result;
                },
                //Callback
                function () {
                    let data = vm.addSensorTypeCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addSensorTypeModal").modal('toggle');

                        //Reset dropzone
                        vm.addSensorTypeCtrl.item.icon = [];
                        vm.dzSensorIconMethods.removeAllFiles();

                        //Add new item to list
                        vm.sensorTypeListCtrl.pushItem(data);
                    }
                }
            );

            //Watch errors during addition of sensor types
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.addSensorTypeCtrl.item.errors;
                },
                //Callback
                function () {
                    let error = vm.addSensorTypeCtrl.item.errors;
                    if (error) {
                        //Reset dropzone
                        vm.addSensorTypeCtrl.item.icon = [];
                        vm.dzSensorIconMethods.removeAllFiles();
                    }
                }
            );

            //Watch deletion of device types and remove them from the list
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.deleteDeviceTypeCtrl.result;
                },
                //Callback
                function () {
                    let id = vm.deleteDeviceTypeCtrl.result;
                    vm.deviceTypeListCtrl.removeItem(id);
                }
            )

            //Watch deletion of actuator types and remove them from the list
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.deleteActuatorTypeCtrl.result;
                },
                //Callback
                function () {
                    let id = vm.deleteActuatorTypeCtrl.result;
                    vm.actuatorTypeListCtrl.removeItem(id);
                }
            );

            //Watch deletion of sensor types and remove them from the list
            $scope.$watch(
                //Value being watched
                function () {
                    return vm.deleteSensorTypeCtrl.result;
                },
                //Callback
                function () {
                    let id = vm.deleteSensorTypeCtrl.result;
                    vm.sensorTypeListCtrl.removeItem(id);
                }
            );
        }
    ]);