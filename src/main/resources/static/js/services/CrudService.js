/* global app */

'use strict';

app.factory('CrudService', ['$resource', '$q', 'ENDPOINT_URI', function ($resource, $q, ENDPOINT_URI) {
    var Item = $resource(ENDPOINT_URI + '/:category/:id', {category: '@category', id: '@id'}, {
        update: {
            method: 'PUT'
        }
    });

    var ItemSearch = $resource(ENDPOINT_URI + '/:category/search/:query', {category: '@category', query: '@query'});

    return {
        ItemResource: Item,

        countItems: function (category) {
            return Item.get({category: category}).$promise.then(
                function (data) {
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

        getPage: function (category, params) {
            var p = params || {};

            p.category = category;

            p.page = p.page || 0;
            p.size = p.size || 10;
            //p.sort = null || p.sort;

            return Item.get(p).$promise.then(
                function (data) {
                    return data;
                }, function (response) {
                    $q.reject(response);
                }
            );
        },

        searchPage: function (category, query, params) {
            var p = params || {};

            p.category = category;
            p.query = query;

            p.page = p.page || 0;
            p.size = p.size || 10;
            //p.sort = null || p.sort;

            return ItemSearch.get(p).$promise.then(
                function (data) {
                    return data;
                }, function (response) {
                    $q.reject(response);
                }
            );
        },

        fetchAllItems: function (category) {
            return Item.get({category: category}).$promise.then(
                function (data) {
                    //Extend received object for empty category list if none available
                    data._embedded = data._embedded || {};
                    data._embedded[category] = data._embedded[category] || [];

                    //Return list
                    return data._embedded[category];
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
        },

        deleteItem: function (category, data) {
            return Item.delete({category: category, id: data.id}).$promise.then(
                function (response) {
                    return data.id;
                },
                function (response) {
                    $q.reject(response);
                }
            );
        },

        updateItem: function (category, data) {
            return Item.update({category: category, id: data.id}, data).$promise.then(
                function (response) {
                    return response;
                },
                function (response) {
                    $q.reject(response);
                }
            );
        }
    };
}]);


