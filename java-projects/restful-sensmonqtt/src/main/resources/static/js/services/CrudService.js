/* global app */

'use strict';

app.factory('CrudService', ['$resource', '$http', '$q', 'ENDPOINT_URI', function ($resource, $http, $q, ENDPOINT_URI) {
        var Item = $resource(ENDPOINT_URI + '/:category/:id', {category: '@category', id: '@id'});

        return {
            ItemResource: Item,

            countItems: function (category) {
                return Item.get({category: category}).$promise.then(
                        function (data) {
                            console.log(data);
                            if (data.page.totalElements) {
                                return data.page.totalElements;
                            } else {
                                return $q.reject(data);
                            }
                        },
                        function (response) {
                            return $q.reject(response);
                        }
                );
            },

            fetchAllItems: function (category) {
                return Item.get({category: category}).$promise.then(
                        function (data) {
                            if (data._embedded[category]) {
                                return data._embedded[category];
                            } else {
                                return $q.reject(data);
                            }
                        },
                        function (response) {
                            return $q.reject(response);
                        }
                );
            },

            fetchSpecificItem: function (category, id) {
                return Item.get({category: category, id: id}).$promise.then(
                        function (data) {
                            return data;
                        },
                        function (response) {
                            $q.reject(response);
                        }
                );
            },

            addItem: function (category, data) {
                return Item.save({category: category}, data).$promise.then(
                        function (data) {
                            return data;
                        },
                        function (response) {
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
                        }
                );
            }
        };
    }]);


