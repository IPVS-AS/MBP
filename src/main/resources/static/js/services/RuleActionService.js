/* global app */

/**
 * Provides services for dealing with rule actions and retrieving a list of rules which use a certain rule action.
 */
app.factory('RuleActionService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {

    //URL under which the using rules can be retrieved
    const URL_GET_USING_RULES = ENDPOINT_URI + '/rules/by-ruleAction/';

    //Performs a server request in order to retrieve a list of all using components.
    function getUsingRules(ruleActionId) {
        return HttpService.getRequest(URL_GET_USING_RULES + ruleActionId);
    }

    //Expose public functions
    return {
        getUsingRules: getUsingRules
    };

}]);
