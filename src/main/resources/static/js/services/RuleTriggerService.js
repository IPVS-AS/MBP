/* global app */

/**
 * Provides services for dealing with rule trigger and retrieving a list of rules which use a certain rule trigger.
 */
app.factory('RuleTriggerService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {

    //URL under which the using rules can be retrieved
    const URL_GET_USING_RULES = ENDPOINT_URI + '/rules/by-ruleTrigger/';

    //Performs a server request in order to retrieve a list of all using components.
    function getUsingRules(ruleTriggerId) {
        return HttpService.getRequest(URL_GET_USING_RULES + ruleTriggerId);
    }

    //Expose public functions
    return {
        getUsingRules: getUsingRules
    };

}]);
