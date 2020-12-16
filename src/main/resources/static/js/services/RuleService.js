/* global app */

/**
 * Provides services for managing rules.
 */
app.factory('RuleService', ['HttpService', '$resource', '$q', 'ENDPOINT_URI',
    function (HttpService, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_GEt_RULE_ACTION_TYPES = ENDPOINT_URI + '/rule-actions/types';
        const URL_TEST_RULE_ACTION = ENDPOINT_URI + '/rule-actions/test/';
        const URL_ENABLE_RULE = ENDPOINT_URI + '/rules/enable/';
        const URL_DISABLE_RULE = ENDPOINT_URI + '/rules/disable/';

        /**
         * [Public]
         * Performs a server request in order to retrieve all rule action types.
         *
         * @returns {*}
         */
        function getRuleActionTypes() {
            return HttpService.getRequest(URL_GEt_RULE_ACTION_TYPES);
        }

        /**
         * [Public]
         * Performs a server request in order to test a rule action given by its id.
         * @param actionId The id of the rule action to test
         * @returns {*}
         */
        function testRuleAction(actionId) {
            return HttpService.postRequest(URL_TEST_RULE_ACTION + actionId);
        }

        /**
         * [Public]
         * Performs a server request in order to enable a certain rule given by its id and returns the
         * resulting promise.
         *
         * @param ruleId The id of the rule to enable
         * @returns {*}
         */
        function enableRule(ruleId) {
            return HttpService.postRequest(URL_ENABLE_RULE + ruleId);
        }

        /**
         * [Public]
         * Performs a server request in order to disable a certain rule given by its id and returns the
         * resulting promise.
         *
         * @param ruleId The id of the rule to disable
         * @returns {*}
         */
        function disableRule(ruleId) {
            return HttpService.postRequest(URL_DISABLE_RULE + ruleId);
        }

        //Expose public methods
        return {
            getRuleActionTypes: getRuleActionTypes,
            testRuleAction: testRuleAction,
            enableRule: enableRule,
            disableRule: disableRule
        }
    }
]);

