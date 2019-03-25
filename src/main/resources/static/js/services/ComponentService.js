/* global app */

/**
 * Provides services for dealing with components, retrieving their states and calculating stats.
 */
app.factory('ComponentService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {

        //URL prefix for requests
        const URL_PREFIX = ENDPOINT_URI + '/';
        //URL suffix under which the deployment state of all components can be retrieved
        const URL_SUFFIX_GET_ALL_COMPONENT_STATES = '/state';
        //URL suffix under which the deployment state of a certain component can be retrieved
        const URL_SUFFIX_GET_COMPONENT_STATE = '/state/';
        //URL suffix under which the deployment state of a certain component can be retrieved
        const URL_SUFFIX_GET_VALUE_LOG_STATS = '/stats/';

        const URL_SUFFIX_GET_VALUE_LOGS = '/valueLogs';

        //Performs a server request in order to retrieve the deployment states of all components of a certain type.
        function getAllComponentStates(component) {
            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_ALL_COMPONENT_STATES);
        }

        //Performs a server request in order to retrieve the deployment state of a certain component.
        function getComponentState(componentId, component) {
            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_COMPONENT_STATE + componentId);
        }

        /* Performs a server request in order to retrieve the value log stats of a certain component. Optionally,
        a unit may be provided in which the values are supposed to be displayed. */
        function getValueLogStats(componentId, component, unit) {
            var parameters = {};

            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_VALUE_LOG_STATS + componentId, {
                params: parameters
            });
        }

        function getValueLogs(componentId, component, pageDetails, unit) {
            var parameters = pageDetails;

            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            return $http.get(URL_PREFIX + component + 's/' + componentId + URL_SUFFIX_GET_VALUE_LOGS, {
                params: parameters
            });
        }

        return {
            COMPONENT: {
                SENSOR: 'SENSOR',
                ACTUATOR: 'ACTUATOR'
            },
            getAllComponentStates: getAllComponentStates,
            getComponentState: getComponentState,
            getValueLogStats: getValueLogStats,
            getValueLogs: getValueLogs,
            isDeployed: function (url) {
                return $http({
                    method: 'GET',
                    url: url
                }).then(
                    function (response) {
                        if (response.data !== undefined) {
                            console.log('isDeployed got data ' + response.data);
                            return response.data === true
                                || response.data === 'true'
                                || response.data.status === 'true';
                        } else {
                            console.log('isDeployed invalid data');
                            console.log(response);
                            return $q.reject(response);
                        }
                    },
                    function (response) {
                        console.log('isDeployed error');
                        return $q.reject(response);
                    });
            },

            deploy: function (parameterList, url) {
                return $http({
                    method: 'POST',
                    url: url,
                    data: parameterList,
                    headers: {'Content-Type': 'application/json'}
                });
            },

            undeploy: function (url) {
                return $http({
                    method: 'DELETE',
                    url: url
                });
            }
        };
    }
]);

