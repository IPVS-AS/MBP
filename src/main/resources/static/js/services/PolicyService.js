/* global app */

/**
 * Provides services for managing policies.
 */
app.factory('PolicyService', ['HttpService', '$resource', '$q', 'ENDPOINT_URI',
    function (HttpService, $resource, $q, ENDPOINT_URI) {
        // Endpoint URIs
        const PATH_PART_POLICY_ACCESS_TYPES = ENDPOINT_URI + '/policies/accessTypes';

        function getPolicyAccessTypes() {
            return HttpService.getRequest(PATH_PART_POLICY_ACCESS_TYPES);
        }

        // Expose public methods
        return {
            getPolicyAccessTypes: getPolicyAccessTypes
        }
    }
]);

