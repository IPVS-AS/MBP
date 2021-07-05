/**
 * Provides a collection discovery-related service functions.
 */
app.factory('DiscoveryService', ['ENDPOINT_URI', 'HttpService', function (ENDPOINT_URI, HttpService) {
    //Base URL for discovery
    const URL_DISCOVERY = ENDPOINT_URI + '/discovery/';

    //URL suffixes for the various requests
    const URL_SUFFIX_TEST_TOPIC = 'getRepositories/';
    const URL_SUFFIX_TEST_DEVICE_TEMPLATE = 'testDeviceTemplate';

    /**
     * [Public]
     * Performs a server request in order to retrieve a ranked representation of device descriptions that match the
     * requirements of a given device template from discovery repositories that are available under a given collection
     * of request topics, provided as collection of their IDs. This way, the query results of a device template can
     * be tested before the template is actually created.
     *
     * @param deviceTemplate The device template to test
     * @param requestTopicIds The set of request topic IDs
     * @returns {*} The resulting promise of the request
     */
    function testDeviceTemplate(deviceTemplate, requestTopicIds) {
        return HttpService.postRequest(URL_DISCOVERY + URL_SUFFIX_TEST_DEVICE_TEMPLATE, {
            'deviceTemplate': deviceTemplate,
            'requestTopics': requestTopicIds
        });
    }

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
        testDeviceTemplate: testDeviceTemplate,
        testRequestTopic: testRequestTopic
    };
}]);
