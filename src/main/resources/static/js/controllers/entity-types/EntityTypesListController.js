/* global app */

/**
 * Controller for the device list page.
 */
app.controller('EntityTypesListController',
    ['$scope', '$controller', '$q',
        'deviceTypesList', 'actuatorTypesList', 'sensorTypesList',
        'addDeviceType', 'addActuatorType', 'addSensorType',
        'deleteDeviceType', 'deleteActuatorType', 'deleteSensorType', 'FileReader',
        function ($scope, $controller, $q, deviceTypesList, actuatorTypesList, sensorTypesList,
                  addDeviceType, addActuatorType, addSensorType,
                  deleteDeviceType, deleteActuatorType, deleteSensorType, FileReader) {
            let vm = this;

            vm.dzIconOptions = {
                paramName: 'icon',
                addRemoveLinks: true,
                previewTemplate: document.querySelector('#tpl').innerHTML,
                createImageThumbnails: true,
                maxFilesize: 1,
                maxFiles: 1,
                acceptedFiles: "image/*"
            };

            vm.dzIconMethods = {};

            vm.dzDeviceTypeIconCallbacks = {
                'addedfile': function (file) {
                    vm.addDeviceTypeCtrl.item.icon = vm.addDeviceTypeCtrl.item.icon || [];
                    vm.addDeviceTypeCtrl.item.icon.push(file);
                },
                'removedfile': function (file) {
                    vm.addDeviceTypeCtrl.item.icon.splice(vm.addDeviceTypeCtrl.item.icon.indexOf(file), 1);
                }
            };

            vm.dzActuatorTypeIconCallbacks = {
                'addedfile': function (file) {
                    vm.addActuatorTypeCtrl.item.icon = vm.addActuatorTypeCtrl.item.icon || [];
                    vm.addActuatorTypeCtrl.item.icon.push(file);
                },
                'removedfile': function (file) {
                    vm.addActuatorTypeCtrl.item.icon.splice(vm.addActuatorTypeCtrl.item.icon.indexOf(file), 1);
                }
            };

            vm.dzSensorTypeIconCallbacks = {
                'addedfile': function (file) {
                    vm.addSensorTypeCtrl.item.icon = vm.addSensorTypeCtrl.item.icon || [];
                    vm.addSensorTypeCtrl.item.icon.push(file);
                },
                'removedfile': function (file) {
                    vm.addSensorTypeCtrl.item.icon.splice(vm.addSensorTypeCtrl.item.icon.indexOf(file), 1);
                }
            };

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

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
             * @param data A data object that contains the id of the entity type that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var deviceTypeId = data.id;
                var deviceTypeName = "";

                //Determines the device type's name by checking the list
                for (var i = 0; i < deviceTypesList.length; i++) {
                    if (deviceTypeId === deviceTypesList[i].id) {
                        deviceTypeName = deviceTypesList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete entity type',
                    type: 'warning',
                    html: "Are you sure you want to delete entity type \"" + deviceTypeName + "\"?",
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
                    addItem: addActuatorType
                }),
                addSensorTypeCtrl: $controller('AddItemController as addSensorTypeCtrl', {
                    $scope: $scope,
                    addItem: addSensorType
                }),
                deleteDeviceTypeCtrl: $controller('DeleteItemController as deleteDeviceTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDeviceType,
                    confirmDeletion: confirmDelete
                }),
                deleteActuatorTypeCtrl: $controller('DeleteItemController as deleteActuatorTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteActuatorType,
                    confirmDeletion: null
                }),
                deleteSensorTypeCtrl: $controller('DeleteItemController as deleteSensorTypeCtrl', {
                    $scope: $scope,
                    deleteItem: deleteSensorType,
                    confirmDeletion: null
                })
            });

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
                        vm.dzIconMethods.removeAllFiles();

                        //Add new item to list
                        vm.deviceTypeListCtrl.pushItem(data);
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
            );
        }
    ]);