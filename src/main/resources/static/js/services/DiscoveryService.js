/**
 * Provides a collection discovery-related service functions.
 */
app.factory('DiscoveryService', ['ENDPOINT_URI', 'HttpService', 'ComponentService',
    function (ENDPOINT_URI, HttpService, ComponentService) {
        //Base URL for discovery and dynamic deployments
        const URL_DISCOVERY = ENDPOINT_URI + '/discovery/';
        const URL_DYNAMIC_DEPLOYMENTS = URL_DISCOVERY + 'dynamic-deployments';

        //URL suffixes for the various requests
        const URL_SUFFIX_TEST_TOPIC = 'getRepositories/';
        const URL_SUFFIX_TEST_DEVICE_TEMPLATE = 'testDeviceTemplate';
        const URL_SUFFIX_ACTIVATE_DEPLOYMENT = '/activate';
        const URL_SUFFIX_DEACTIVATE_DEPLOYMENT = '/deactivate';

        //Category name
        const URL_CATEGORY_NAME = 'discovery/dynamic-deployment';

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

        /**
         * [Public]
         * Performs a server request in order to retrieve the data of a certain dynamic deployment object, given by its ID.
         *
         * @param dynamicDeployment The ID of the dynamic deployment to retrieve
         */
        function getDynamicDeployment(dynamicDeployment) {
            return HttpService.getOne('discovery/dynamic-deployments', dynamicDeployment);
        }

        /**
         * [Public]
         * Performs a server request in order to activate a certain dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment to activate
         */
        function activateDynamicDeployment(dynamicDeploymentId) {
            return HttpService.postRequest(URL_DYNAMIC_DEPLOYMENTS + '/' + dynamicDeploymentId + URL_SUFFIX_ACTIVATE_DEPLOYMENT);
        }

        /**
         * [Public]
         * Performs a server request in order to deactivate a certain dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment to deactivate
         */
        function deactivateDynamicDeployment(dynamicDeploymentId) {
            return HttpService.postRequest(URL_DYNAMIC_DEPLOYMENTS + '/' + dynamicDeploymentId + URL_SUFFIX_DEACTIVATE_DEPLOYMENT);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the value log stats of a certain dynamic deployment, given by
         * its ID. Optionally, a unit may be provided in which the values are supposed to be displayed.
         *
         * @param dynamicDeploymentId The id of the dynamic deployment for which the stats are supposed to be retrieved
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getValueLogStats(dynamicDeploymentId, unit) {
            //Delegate the call to the component service
            return ComponentService.getValueLogStats(dynamicDeploymentId, URL_CATEGORY_NAME, unit);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the value logs for a certain dynamic deployment, given
         * by its ID.
         *
         * @param dynamicDeploymentId The id of the dynamic deployment for which the logs are supposed to be retrieved
         * @param pageDetails Page details object (size, order etc.) that specifies the logs to retrieve
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getValueLogs(dynamicDeploymentId, pageDetails, unit) {
            //Delegate the call to the component service
            return ComponentService.getValueLogs(dynamicDeploymentId, URL_CATEGORY_NAME, pageDetails, unit);
        }

        /**
         * [Public]
         * Performs a server request in order to delete all recorded value logs of a certain dynamic deployment,
         * given by its ID.
         *
         * @param dynamicDeploymentId The id of the dynamic deployment whose value logs are supposed to be deleted
         * @returns {*}
         */
        function deleteValueLogs(dynamicDeploymentId) {
            //Delegate the call to the component service
            return ComponentService.deleteValueLogs(dynamicDeploymentId, URL_CATEGORY_NAME);
        }

        //Expose public functions
        return {
            testDeviceTemplate: testDeviceTemplate,
            testRequestTopic: testRequestTopic,
            getDynamicDeployment: getDynamicDeployment,
            activateDynamicDeployment: activateDynamicDeployment,
            deactivateDynamicDeployment: deactivateDynamicDeployment,
            getValueLogStats: getValueLogStats,
            getValueLogs: getValueLogs,
            deleteValueLogs: deleteValueLogs
        };
    }]);
