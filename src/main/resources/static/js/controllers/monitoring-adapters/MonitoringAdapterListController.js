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

                //Store list of available device types
                vm.deviceTypesList = deviceTypesList;

                //Store list of available filters and add an "all" filter to the beginning
                vm.typeFiltersList = deviceTypesList.slice();
                vm.typeFiltersList.unshift({
                    id: 'all',
                    name: 'All',
                    component: 'DEVICE'
                });
            })();

            /**
             * [Private]
             * Creates an array of device types of an adapter and adds it to the adapter object.
             *
             * @param adapter The adapter to preprocess
             */
            function monitoringAdapterPreprocessing(adapter) {
                var deviceTypes = null;

                //Check where the device types for this adapter are stored
                if(adapter.deviceTypes){
                    deviceTypes = adapter.deviceTypes;
                }else{
                    deviceTypes = adapter._embedded.deviceTypes;
                }

                //List to collect all type ids
                var typesIdList = [];

                //Iterate over all device type objects of this adapter
                for (var j = 0; j < deviceTypes.length; j++) {
                    typesIdList.push(deviceTypes[j].id);
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