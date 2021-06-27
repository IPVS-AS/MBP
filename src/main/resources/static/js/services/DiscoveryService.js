/**
 * Provides a collection discovery-related service functions.
 */
app.factory('DiscoveryService', ['ENDPOINT_URI', 'HttpService', function (ENDPOINT_URI, HttpService) {
    //Base URL for discovery
    const URL_DISCOVERY = ENDPOINT_URI + '/discovery/';

    //URL suffixes for the various requests
    const URL_SUFFIX_TEST_TOPIC = 'getRepositories/';

    /**
     * [Public]
     * Performs a server request in order to retrieve information about the repositories that are available
     * for a certain request topic, given by its ID. This way, it can be tested whether a registered request topics
     * works as intended by the user.
     *
     * @param requestTopicId The id of the request topic to test
     * @returns {*} The resulting promise of the request
     */
    function testRequestTopic(requestTopicId) {
        return HttpService.getRequest(URL_DISCOVERY + URL_SUFFIX_TEST_TOPIC + requestTopicId);
    }

    //Expose public functions
    return {
        testRequestTopic: testRequestTopic
    };
}]);
