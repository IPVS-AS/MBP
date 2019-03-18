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

        //Performs a server request in order to retrieve the deployment states of all components of a certain type.
        function getAllComponentStates(component) {
            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_ALL_COMPONENT_STATES);
        }

        //Performs a server request in order to retrieve the deployment state of a certain component.
        function getComponentState(componentId, component) {
            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_COMPONENT_STATE + componentId);
        }

        //Performs a server request in order to retrieve the value log stats of a certain component.
        function getValueLogStats(componentId, component) {
            return $http.get(URL_PREFIX + component + URL_SUFFIX_GET_VALUE_LOG_STATS + componentId);
        }

        return {
            COMPONENT: {
                SENSOR: 'SENSOR',
                ACTUATOR: 'ACTUATOR'
            },
            getAllComponentStates: getAllComponentStates,
            getComponentState: getComponentState,
            getValueLogStats: getValueLogStats,
            getValues: function (component, idref, parameters) {
                var url = ENDPOINT_URI;

                var params = {} || parameters;

                if (idref) {
                    url += '/valueLogs/search/findAllByIdref';
                    params.idref = idref;
                } else if (component) {
                    url += '/valueLogs/search/findAllByComponent';
                    params.component = component;
                } else {
                    url += '/valueLogs';
                    params.sort = 'date,desc';
                }

                return $http({
                    method: 'GET',
                    url: url,
                    params: params
                })
                    .then(
                        function (response) {
                            if (typeof response.data === 'object') {
                                return response.data;
                            } else {
                                return $q.reject(response);
                            }
                        },
                        function (response) {
                            return $q.reject(response);
                        });
            },

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

