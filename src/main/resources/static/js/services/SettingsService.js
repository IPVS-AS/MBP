/* global app */

/**
 * Provides services for managing the application settings.
 */
app.factory('SettingsService', ['HttpService', 'ENDPOINT_URI',
    function (HttpService, ENDPOINT_URI) {
        //URLs for server requests
        const URL_SETTINGS = ENDPOINT_URI + '/settings';
        const URL_DEFAULT_OPERATORS = ENDPOINT_URI + '/settings/default-operators';
        const URL_DOCUMENTATION_META_DATA = ENDPOINT_URI + '/docs';

        /**
         * [Public]
         * Performs a server request in order to add default operators.
         * @returns {*}
         */
        function addDefaultOperators() {
            return HttpService.postRequest(URL_DEFAULT_OPERATORS);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the current settings.
         *
         * @returns {*}
         */
        function getSettings() {
            return HttpService.getRequest(URL_SETTINGS);
        }

        /**
         * [Public]
         * Performs a server request in order to save new settings.
         * @param settings The settings object to save
         * @returns {*}
         */
        function saveSettings(settings) {
            return HttpService.postRequest(URL_SETTINGS, settings, null);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve meta data about the documentation of the REST interface.
         *
         * @returns {*}
         */
        function getDocumentationMetaData() {
            return HttpService.getRequest(URL_DOCUMENTATION_META_DATA);
        }

        //Expose public methods
        return {
            addDefaultOperators: addDefaultOperators,
            getSettings: getSettings,
            saveSettings: saveSettings,
            getDocumentationMetaData: getDocumentationMetaData
        }
    }
]);

