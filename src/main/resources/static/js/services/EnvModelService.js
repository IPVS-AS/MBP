/* global app */

/**
 * Provides services for managing environment models.
 */
app.factory('EnvModelService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_BASE =  ENDPOINT_URI + '/env-models/';
        const URL_SUFFIX_REGISTER  = '/register';

        /**
         * [Public]
         * Performs a server request in order to register all components of a given model.
         * @param modelID The ID of the affected model
         * @returns {*}
         */
        function registerComponents(modelID) {
            return $http.post(URL_BASE + modelID + URL_SUFFIX_REGISTER);
        }

        //Expose public methods
        return {
            registerComponents: registerComponents,
        }
    }
]);

