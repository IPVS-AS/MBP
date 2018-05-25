/* global app */

app.factory('ComponentService', ['$http', '$resource', '$q', 'ENDPOINT_URI', function ($http, $resource, $q, ENDPOINT_URI) {
        return {
            COMPONENT: {
                SENSOR: 'SENSOR',
                ACTUATOR: 'ACTUATOR'
            },

            getValues: function (component, idref, parameters) {
                var url = ENDPOINT_URI;
                
                var params = {} || parameters;
        
                if (idref) {
                    url += '/valueLogs/search/findAllByIdref';
                    params.idref = idref;
                } else if(component) {
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
                ;
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

            deploy: function (data, url) {
                if (data.constructor === Array) {
                    // if receveid an Array, put it in an object
                    data = {
                        class: '',
                        pinset: data
                    };
                }

                console.log(data);
                return $http({
                    method: 'POST',
                    url: url,
                    params: data,
                    headers: {'Content-Type': 'application/json'}  // set the headers so angular passing info as form data (not request payload)
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

