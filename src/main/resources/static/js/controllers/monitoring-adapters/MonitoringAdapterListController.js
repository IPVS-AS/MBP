/* global app */

/**
 * Controller for the monitoring adapter list page which extends the AdapterListController.
 */
app.controller('MonitoringAdapterListController',
    ['$scope', '$controller', 'deviceTypesList', 'monitoringAdapterList', 'addMonitoringAdapter', 'deleteMonitoringAdapter', 'parameterTypesList', 'NotificationService',
        function ($scope, $controller, deviceTypesList, monitoringAdapterList, addMonitoringAdapter, deleteMonitoringAdapter, parameterTypesList, NotificationService) {

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
                vm.deviceTypesList = deviceTypesList;
            })();


            //Extend the controller for the AdapterListController and pass all relevant data
            angular.extend(vm, $controller('AdapterListController',
                {
                    $scope: $scope,
                    adapterList: monitoringAdapterList,
                    addAdapter: addMonitoringAdapter,
                    deleteAdapter: deleteMonitoringAdapter,
                    parameterTypesList: parameterTypesList
                })
            );

            console.log("Controller:");
            console.log(vm);
        }]
);