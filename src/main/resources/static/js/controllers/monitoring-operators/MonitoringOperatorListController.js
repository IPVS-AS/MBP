/* global app */

/**
 * Controller for the monitoring operator list page which extends the OperatorListController.
 */
app.controller('MonitoringOperatorListController',
    ['$scope', '$controller', '$timeout', 'deviceTypesList', 'monitoringOperatorList', 'dataModelList', 'addMonitoringOperator', 'deleteMonitoringOperator', 'parameterTypesList', 'NotificationService',
        function ($scope, $controller, $timeout, deviceTypesList, monitoringOperatorList, dataModelList, addMonitoringOperator, deleteMonitoringOperator, parameterTypesList, NotificationService) {
            //Array of colors to be used for the different device types
            const DEVICE_TYPES_COLORS = ['bg-pink', 'bg-purple', 'bg-deep-purple', 'bg-indigo', 'bg-blue',
                'bg-light-blue', 'bg-cyan', 'bg-teal', 'bg-green', 'bg-light-green', 'bg-lime', 'bg-yellow',
                'bg-amber', 'bg-orange', 'bg-deep-orange'];


            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for device types
                if (deviceTypesList.length < 1) {
                    NotificationService.notify('Could not load available device types.', 'error');
                    return;
                }

                //Extend device types for color
                for (let i = 0; i < deviceTypesList.length; i++) {
                    let colorIndex = i % DEVICE_TYPES_COLORS.length;
                    deviceTypesList[i].color = DEVICE_TYPES_COLORS[colorIndex];
                }

                //Store list of available device types
                vm.deviceTypesList = deviceTypesList;

                //Refresh device type select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * [Private]
             * Creates an array of device types of an operator and adds it to the operator object.
             *
             * @param operator The operator to preprocess
             */
            function monitoringOperatorPreprocessing(operator) {
                //Check where the device types for this operator are stored
                if (!operator.deviceTypes) {
                    operator.deviceTypes = operator._embedded.deviceTypes;
                }

                //List to collect all type ids
                let typesIdList = [];

                //Iterate over all device type objects of this operator
                for (let i = 0; i < operator.deviceTypes.length; i++) {
                    //Add device type id to list
                    typesIdList.push(operator.deviceTypes[i].id);

                    //Find and add matching color for this device type
                    operator.deviceTypes[i].color = 'label-default';
                    for (let j = 0; j < deviceTypesList.length; j++) {
                        if (deviceTypesList[j].id === operator.deviceTypes[i].id) {
                            operator.deviceTypes[i].color = deviceTypesList[j].color;
                            break;
                        }
                    }
                }
                operator.deviceTypesList = typesIdList;
            }

            //Extend the controller for the OperatorListController and pass all relevant data
            angular.extend(vm, $controller('OperatorListController',
                {
                    $scope: $scope,
                    operatorList: monitoringOperatorList,
                    dataModelList: dataModelList,
                    operatorPreprocessing: monitoringOperatorPreprocessing,
                    addOperator: addMonitoringOperator,
                    deleteOperator: deleteMonitoringOperator,
                    parameterTypesList: parameterTypesList
                })
            );
        }]
);