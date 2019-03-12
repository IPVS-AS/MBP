/* global app */

/**
 * Controller for the monitoring adapter list page which extends the AdapterListController.
 */
app.controller('MonitoringAdapterListController',
    ['$scope', '$controller', 'deviceTypesList', 'monitoringAdapterList', 'addMonitoringAdapter', 'deleteMonitoringAdapter', 'parameterTypesList', 'NotificationService',
        function ($scope, $controller, deviceTypesList, monitoringAdapterList, addMonitoringAdapter, deleteMonitoringAdapter, parameterTypesList, NotificationService) {

            var vm = this;

            //Initial adapter filter configuration
            vm.typeFilter = 'all';

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for device types
                if (deviceTypesList.length < 1) {
                    NotificationService.notify('Could not load available device types.', 'error');
                    return;
                }
                //Add "all" device type filter to the beginning
                deviceTypesList.unshift({
                    id: 'all',
                    name: 'All',
                    component: 'DEVICE'
                });

                vm.deviceTypesList = deviceTypesList;
            })();

            /**
             * [Private]
             * Creates an array of device types of an adapter and adds it to the adapter object.
             *
             * @param adapter The adapter to preprocess
             */
            function monitoringAdapterPreprocessing(adapter) {
                var typesList = [];

                //Iterate over all device type objects of this adapter
                for (var j = 0; j < adapter.deviceTypes.length; j++) {
                    typesList.push(adapter.deviceTypes[j].id);
                }
                adapter.deviceTypesList = typesList;
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