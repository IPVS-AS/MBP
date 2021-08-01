/**
 * Provides a collection discovery-related service functions.
 */
app.factory('DiscoveryService', ['ENDPOINT_URI', 'HttpService', 'ComponentService',
    function (ENDPOINT_URI, HttpService, ComponentService) {
        //Base URL for discovery and dynamic deployments
        const URL_DISCOVERY = ENDPOINT_URI + '/discovery/';
        const URL_DEVICE_TEMPLATES = URL_DISCOVERY + 'device-templates';
        const URL_DYNAMIC_DEPLOYMENTS = URL_DISCOVERY + 'dynamic-deployments';

        //URL suffixes for the various requests
        const URL_SUFFIX_TEST_TOPIC = 'getRepositories/';
        const URL_SUFFIX_TEST_DEVICE_TEMPLATE = 'testDeviceTemplate';
        const URL_SUFFIX_ACTIVATE_DEPLOYMENT = '/activate';
        const URL_SUFFIX_DEACTIVATE_DEPLOYMENT = '/deactivate';
        const URL_SUFFIX_DISCOVERY_LOGS = '/logs';
        const URL_SUFFIX_REFRESH_CANDIDATE_DEVICES = '/refreshCandidateDevices'

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
         * @returns {*|void} The resulting promise of the request
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
         * @returns {*|void} The resulting promise of the request
         */
        function testRequestTopic(requestTopicId) {
            return HttpService.getRequest(URL_DISCOVERY + URL_SUFFIX_TEST_TOPIC + requestTopicId);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the data of a certain dynamic deployment object, given by its ID.
         *
         * @param dynamicDeployment The ID of the dynamic deployment to retrieve
         * @returns {*|void} The resulting promise of the request
         */
        function getDynamicDeployment(dynamicDeployment) {
            return HttpService.getOne('discovery/dynamic-deployments', dynamicDeployment);
        }

        /**
         * [Public]
         * Performs a server request in order to activate a certain dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment to activate
         * @returns {*|void} The resulting promise of the request
         */
        function activateDynamicDeployment(dynamicDeploymentId) {
            return HttpService.postRequest(URL_DYNAMIC_DEPLOYMENTS + '/' + dynamicDeploymentId + URL_SUFFIX_ACTIVATE_DEPLOYMENT);
        }

        /**
         * [Public]
         * Performs a server request in order to deactivate a certain dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment to deactivate
         * @returns {*|void} The resulting promise of the request
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
         * @returns {*|void} The resulting promise of the request
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
         * @returns {*|void} The resulting promise of the request
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
         * @param dynamicDeploymentId The ID of the dynamic deployment whose value logs are supposed to be deleted
         * @returns {*|void} The resulting promise of the request
         * */
        function deleteValueLogs(dynamicDeploymentId) {
            //Delegate the call to the component service
            return ComponentService.deleteValueLogs(dynamicDeploymentId, URL_CATEGORY_NAME);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve a page of discovery logs, based on a given page configuration,
         * for a certain dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment to retrieve the discovery logs for
         * @param pageSize The desired size of the page to retrieve
         * @param pageNumber The desired number of the page to retrieve
         * @param sortOrder The desired sorting order within the page as string
         * @returns {*|void} The resulting promise of the request
         */
        function getDiscoveryLogs(dynamicDeploymentId, pageSize, pageNumber, sortOrder) {
            return HttpService.getRequest(URL_DYNAMIC_DEPLOYMENTS + '/' + dynamicDeploymentId + URL_SUFFIX_DISCOVERY_LOGS, {
                'size': pageSize,
                'page': pageNumber,
                'sort': sortOrder
            });
        }

        /**
         * [Public]
         * Performs a server request in order to delete all discovery logs that are currently available for a certain
         * dynamic deployment, given by its ID.
         *
         * @param dynamicDeploymentId The ID of the dynamic deployment for which the logs are supposed to be deleted
         * @returns @returns {*|void} The promise of the resulting request
         */
        function deleteDiscoveryLogs(dynamicDeploymentId) {
            return HttpService.deleteRequest(URL_DYNAMIC_DEPLOYMENTS + '/' + dynamicDeploymentId + URL_SUFFIX_DISCOVERY_LOGS);
        }

        /**
         * [Public]
         * Performs a server request in order to update the candidate devices and the corresponding subscriptions at
         * the discovery repositories for a certain device template, given by its ID.
         *
         * @param deviceTemplateId The ID of the pertaining device template
         * @returns @returns {*|void} The promise of the resulting request
         */
        function refreshCandidateDevices(deviceTemplateId) {
            return HttpService.postRequest(URL_DEVICE_TEMPLATES + '/' + deviceTemplateId + URL_SUFFIX_REFRESH_CANDIDATE_DEVICES);
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
            deleteValueLogs: deleteValueLogs,
            getDiscoveryLogs: getDiscoveryLogs,
            deleteDiscoveryLogs: deleteDiscoveryLogs,
            refreshCandidateDevices: refreshCandidateDevices
        };
    }]);
