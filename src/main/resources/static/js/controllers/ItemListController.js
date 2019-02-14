/* global app */

'use strict';

app.controller('ItemListController',
        ['$scope', '$route', 'list',
            function ($scope, $route, list) {
                var vm = this;

                //Public
                function pushItem(item) {
                    if (item) {
                        vm.items.splice(0, 0, item);
                    }
                }

               function removeItem(id) {
                 var itemList = vm.items;

                 for (var i = 0; i < itemList.length; i++) {
                   var curr = itemList[i];
                   if (curr.id == id) {
                     vm.items.splice(i, 1);
                     break;
                   }
                 }
               }
               
                //Expose
                angular.extend(vm,
                        {
                            items: list,
                            pushItem: pushItem,
                            removeItem: removeItem
                        });
            }
        ]);