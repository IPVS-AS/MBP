/* global app */

/**
 * Provides services for managing the application settings.
 */
app.factory('SettingsService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_SETTINGS = ENDPOINT_URI + '/settings';
        const URL_DOCUMENTATION_META_DATA = ENDPOINT_URI + '/docs';

        /**
         * [Public]
         * Performs a server request in order to retrieve the current settings.
         *
         * @returns {*}
         */
        function getSettings() {
            return $http.get(URL_SETTINGS);
        }

        /**
         * [Public]
         * Performs a server request in order to save new settings.
         * @param settings The settings object to save
         * @returns {*}
         */
        function saveSettings(settings) {
            return $http({
                method: 'POST',
                url: URL_SETTINGS,
                data: settings,
                headers: {'Content-Type': 'application/json'}
            });
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve meta data about the documentation of the REST interface.
         *
         * @returns {*}
         */
        function getDocumentationMetaData() {
            return $http.get(URL_DOCUMENTATION_META_DATA);
        }

        //Expose public methods
        return {
            getSettings: getSettings,
            saveSettings: saveSettings,
            getDocumentationMetaData: getDocumentationMetaData
        }
    }
]);

