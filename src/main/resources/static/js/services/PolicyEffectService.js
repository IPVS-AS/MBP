/* global app */

/**
 * Provides services for dealing with policy conditions.
 */
app.factory('PolicyEffectService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {

    // URI for the endpoint for policies using this effect
    const PATH_PATH_POLICY_BY_EFFECT = ENDPOINT_URI + '/policies/byEffect/';

    // Performs a server request in order to retrieve a list of all policies using this effect
    function getPoliciesUsingThisEffect(policyEffectId) {
        return HttpService.getRequest(PATH_PATH_POLICY_BY_EFFECT + policyEffectId);
    }

    // Expose public functions
    return {
        getPoliciesUsingThisEffect: getPoliciesUsingThisEffect
    };

}]);
