/* global app */

/**
 * Controller for the monitoring adapter list page which extends the AdapterListController.
 */
app.controller('MonitoringAdapterListController',
    ['$scope', '$controller', '$timeout', 'deviceTypesList', 'monitoringAdapterList', 'addMonitoringAdapter', 'deleteMonitoringAdapter', 'parameterTypesList', 'NotificationService',
        function ($scope, $controller, $timeout, deviceTypesList, monitoringAdapterList, addMonitoringAdapter, deleteMonitoringAdapter, parameterTypesList, NotificationService) {
            //Array of colors to be used for the different device types
            const DEVICE_TYPES_COLORS = ['bg-pink', 'bg-purple', 'bg-deep-purple', 'bg-indigo', 'bg-blue',
                'bg-light-blue', 'bg-cyan', 'bg-teal', 'bg-green', 'bg-light-green', 'bg-lime', 'bg-yellow',
                'bg-amber', 'bg-orange', 'bg-deep-orange'];


            var vm = this;

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
                for (var i = 0; i < deviceTypesList.length; i++) {
                    var colorIndex = i % DEVICE_TYPES_COLORS.length;
                    deviceTypesList[i].color = DEVICE_TYPES_COLORS[colorIndex];
                }

                //Store list of available device types
                vm.deviceTypesList = deviceTypesList;

                //Refresh device type select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function (e) {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * [Private]
             * Creates an array of device types of an adapter and adds it to the adapter object.
             *
             * @param adapter The adapter to preprocess
             */
            function monitoringAdapterPreprocessing(adapter) {
                //Check where the device types for this adapter are stored
                if (!adapter.deviceTypes) {
                    adapter.deviceTypes = adapter._embedded.deviceTypes;
                }

                //List to collect all type ids
                var typesIdList = [];

                //Iterate over all device type objects of this adapter
                for (var i = 0; i < adapter.deviceTypes.length; i++) {
                    //Add device type id to list
                    typesIdList.push(adapter.deviceTypes[i].id);

                    //Find and add matching color for this device type
                    adapter.deviceTypes[i].color = 'label-default';
                    for (var j = 0; j < deviceTypesList.length; j++) {
                        if (deviceTypesList[j].id === adapter.deviceTypes[i].id) {
                            adapter.deviceTypes[i].color = deviceTypesList[j].color;
                            break;
                        }
                    }
                }
                adapter.deviceTypesList = typesIdList;
            }

            //Extend the controller for the AdapterListController and pass all relevant data
            angular.extend(vm, $controller('AdapterListController',
                {
                    $scope: $scope,
                    adapterList: monitoringAdapterList,
                    adapterPreprocessing: monitoringAdapterPreprocessing,
                    addAdapter: addMonitoringAdapter,
                    deleteAdapter: deleteMonitoringAdapter,
                    parameterTypesList: parameterTypesList
                })
            );
        }]
);