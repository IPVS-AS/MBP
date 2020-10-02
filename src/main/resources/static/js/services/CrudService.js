/* global app */

'use strict';

app.factory('CrudService', ['$resource', '$q', 'ENDPOINT_URI', 'HttpService',
    function ($resource, $q, ENDPOINT_URI, HttpService) {
        let Item = $resource(ENDPOINT_URI + '/:category/:id', {category: '@category', id: '@id'}, {
            update: {
                method: 'PUT'
            }
        });

        var ItemSearch = $resource(ENDPOINT_URI + '/:category/search/:query', {category: '@category', query: '@query'});

        return {
            ItemResource: Item,

            countItems: function (category) {
                return HttpService.count(category);
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
                return HttpService.getAll(category);
            },

            fetchSpecificItem: function (category, id) {
                return HttpService.getOne(category, id);
            },

            addItem: function (category, data) {
                return HttpService.addOne(category, data);
            },

            deleteItem: function (category, id) {
                return HttpService.deleteOne(category, id);
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


