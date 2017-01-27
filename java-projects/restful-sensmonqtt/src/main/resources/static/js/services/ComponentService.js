/* global app */

app.factory('ComponentService', ['$http', '$q', function ($http, $q) {
        return {
            COMPONENT: {
                SENSOR: 'SENSOR',
                ACTUATOR: 'ACTUATOR'
            },

            getValues: function (component, idref) {
                var url = '';
                var params = {};
                if (idref) {
                    url = 'http://localhost:8080/sensmonqtt/api/valueLogs/search/findAllByIdrefOrderByDateDesc';
                    params.idref = idref;
                } else if(component) {
                    url = 'http://localhost:8080/sensmonqtt/api/valueLogs/search/findAllByComponentOrderByDateDesc';
                    params.component = component;
                } else {
                    url = 'http://localhost:8080/sensmonqtt/api/valueLogs/';
                    params.sort = 'date,desc';
                }

                console.log('GET ' + url + ', ' + params);

                return $http({
                    method: 'GET',
                    url: url,
                    params: params
                })
                        .then(
                                function (response) {
                                    if (typeof response.data === 'object') {
                                        return response.data._embedded.valueLogs;
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

