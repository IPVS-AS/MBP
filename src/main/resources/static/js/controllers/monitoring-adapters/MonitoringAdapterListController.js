/* global app */

/**
 * Controller for the monitoring adapter list page that extends the AdapterListController.
 */
app.controller('MonitoringAdapterListController',
    ['$scope', '$controller', 'monitoringAdapterList', 'addMonitoringAdapter', 'deleteMonitoringAdapter',
        function ($scope, $controller, monitoringAdapterList, addMonitoringAdapter, deleteMonitoringAdapter) {

            var vm = this;

            //Extend the controller for the AdapterListController and pass all relevant data
            angular.extend(vm, $controller('AdapterListController',
                {
                    $scope: $scope,
                    adapterList: monitoringAdapterList,
                    addAdapter: addMonitoringAdapter,
                    deleteAdapter: deleteMonitoringAdapter
                })
            );
        }]
);