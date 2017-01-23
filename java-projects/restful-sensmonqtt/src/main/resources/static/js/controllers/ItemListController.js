/* global app */

'use strict';

app.controller('ItemListController',
        ['$scope', '$route', 'list',
            function ($scope, $route, list) {
                var vm = this;

                // public
                function push(item) {
                    if (item) {
                        vm.items.splice(0, 0, item);
                    }
                }

                // expose
                angular.extend(vm,
                        {
                            items: list,
                            push: push
                        });
            }
        ]);