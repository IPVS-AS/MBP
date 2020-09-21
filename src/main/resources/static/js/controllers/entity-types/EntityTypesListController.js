/* global app */

/**
 * Controller for the device list page.
 */
app.controller('EntityTypesListController',
    ['$scope', '$controller',
        'deviceTypesList', 'actuatorTypesList', 'sensorTypesList',
        'addDeviceType', 'addActuatorType', 'addSensorType',
        'deleteDeviceType', 'deleteActuatorType', 'deleteSensorType',
        function ($scope, $controller, deviceTypesList, actuatorTypesList, sensorTypesList,
                  addDeviceType, addActuatorType, addSensorType,
                  deleteDeviceType, deleteActuatorType, deleteSensorType) {
            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                console.log(deviceTypesList);
                console.log(actuatorTypesList);
                console.log(sensorTypesList);
            })();

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
                    addItem: addDeviceType
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
                    confirmDeletion: null
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
        }
    ]);