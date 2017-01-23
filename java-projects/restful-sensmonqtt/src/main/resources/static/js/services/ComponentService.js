/* global app */

app.factory('ComponentService', ['$http', '$q', function ($http, $q) {
        return {
            getValues: function (idref = undefined) {
                var url = '';
                var params = {};
                if (idref) {
                    url = 'http://localhost:8080/sensmonqtt/api/valueLogs/search/findAllByIdrefOrderByDateDesc';
                    params.idref = idref;

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
                            if (typeof response.data === 'object') {
                                console.log('isDeployed got data');
                                return response.data === 'true';
                            } else {
                                console.log('isDeployed invalid data');
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

