/* global app */

/**
 * Provides services for dealing with policy conditions.
 */
app.factory('PolicyConditionService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    // URI for the endpoint for policies using this condition
    const PATH_PATH_POLICY_BY_CONDITION = ENDPOINT_URI + '/policy/byCondition/';

    // Performs a server request in order to retrieve a list of all policies using this condition
    function getPoliciesUsingThisCondition(policyConditionId) {
        return $http.get(PATH_PATH_POLICY_BY_CONDITION + policyConditionId);
    }

    // Expose public functions
    return {
        getPoliciesUsingThisCondition: getPoliciesUsingThisCondition
    };

}]);
