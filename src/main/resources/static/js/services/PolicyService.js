/* global app */

/**
 * Provides services for managing policies.
 */
app.factory('PolicyService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        // Endpoint URIs
        const PATH_PART_POLICY_ACCESS_TYPES = ENDPOINT_URI + '/policy/accessType';

        function getPolicyAccessTypes() {
            return $http.get(PATH_PART_POLICY_ACCESS_TYPES);
        }

        // Expose public methods
        return {
            getPolicyAccessTypes: getPolicyAccessTypes
        }
    }
]);

