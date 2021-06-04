/* global app */

/*
 * Controller for the settings page.
 */
app.controller('DeviceTemplateListController',
    ['$scope', 'NotificationService',
        function ($scope, NotificationService) {
            //Find relevant DOM elements
            // const ELEMENT_LOGS_TABLE = $("#exception-logs-table");

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();


            //Expose functions that are used externally
            angular.extend(vm, {});
        }
    ]);
