/* global app */

'use strict';

app.factory('CrudService', ['$http', '$q', 'restUri', function ($http, $q, restUri) {
        return {
            countItems: function (category) {
                return $http.get(restUri + '/' + category)
                        .then(
                                function (response) {
                                    if (typeof response.data === 'object') {
                                        console.log(response.data);
                                        return response.data.page.totalElements;
                                    } else {
                                        // invalid response
                                        return $q.reject(response);
                                    }
                                },
                                function (errResponse) {
                                    return $q.reject(errResponse);
                                }
                        );
            },

            fetchAllItems: function (category) {
                return $http.get(restUri + '/' + category)
                        .then(
                                function (response) {
                                    if (typeof response.data === 'object') {
                                        return response.data._embedded[category];
                                    } else {
                                        // invalid response
                                        return $q.reject(response);
                                    }
                                },
                                function (errResponse) {
                                    return $q.reject(errResponse);
                                }
                        );
            },

            fetchSpecificItem: function (category, id) {
                return $http.get(restUri + '/' + category + '/' + id)
                        .then(
                                function (response) {
                                    if (typeof response.data === 'object') {
                                        return response.data;
                                    } else {
                                        // invalid response
                                        return $q.reject(response);
                                    }
                                },
                                function (errResponse) {
                                    return $q.reject(errResponse);
                                }
                        );
            },

            addItem: function (category, data) {
                return $http({
                    method: 'POST',
                    url: restUri + '/' + category,
                    data: JSON.stringify(data), // pass in data as strings
                    headers: {'Content-Type': 'application/json'}  // set the headers so angular passing info as form data (not request payload)
                }).then(function (response) {
                    if (typeof response.data === 'object') {
                        return response.data;
                    } else {
                        // invalid response
                        return $q.reject(response);
                    }
                }, function (response) {
                    // something went wrong
                    // if has data for errors, parse/map it
                    if (response.data.errors) {
                        var parsed = {parsed: true, response: response};
                        var errors = response.data.errors;
                        for (var i in errors) {
                            if (errors[i].property) {
                                if (!parsed[errors[i].property]) {
                                    parsed[errors[i].property] = errors[i];
                                }
                            }
                        }
                        return $q.reject(parsed);
                    }

                    return $q.reject(response);
                });
            }
        };
    }]);


