/* global app */

/*
 * Controller for the settings page.
 */
app.controller('SettingsController',
    ['$scope', '$http', '$q', 'ENDPOINT_URI', 'settings', 'documentationMetaData', 'SettingsService', 'NotificationService',
        function ($scope, $http, $q, ENDPOINT_URI, settings, documentationMetaData, SettingsService, NotificationService) {
            let vm = this;

            //Extend controller for settings and meta data object
            vm.settings = settings;
            vm.documentationMetaData = documentationMetaData;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if settings could be loaded
                if (settings == null) {
                    NotificationService.notify("Could not load application settings.", "error");
                }
                //Check if documentation meta data could be loaded
                if (documentationMetaData == null) {
                    NotificationService.notify("Could not load documentation meta data.", "error");
                }
            })();

            /**
             * Issues a REST request in order to save the settings.
             *
             * @returns The created REST request
             */
            function saveSettings() {
                //Create REST request with the settings object as payload
                return $http({
                    data: vm.settings,
                    method: 'POST',
                    url: url
                }).then(
                    //Success callback
                    function (response) {
                        NotificationService.notify('The settings were saved successfully.', 'success');
                    },
                    //Error callback
                    function (response) {
                        NotificationService.notify('The settings could not be saved.', 'error');
                        return $q.reject(response);
                    });
            }

            //Expose functions that are triggered externally
            angular.extend(vm, {
                saveSettings: saveSettings
            });
        }
    ]);
