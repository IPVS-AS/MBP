/* global app */

'use strict';

/**
 * Controller for managing a list of resource items.
 */
app.controller('ItemListController',
    ['$scope', '$route', 'list',
        function ($scope, $route, list) {
            let vm = this;

            //Current term for filtering
            vm.filterTerm = "";

            /**
             * [Public]
             * Pushes a new item to the item list.
             * @param item The item to push
             */
            function pushItem(item) {
                //Sanity check
                if (!item) {
                    return;
                }

                //Push item
                vm.items.splice(0, 0, item);
            }

            /**
             * [Public]
             * Removes an item with a certain ID from the item list.
             * @param id The ID of the element to remove
             */
            function removeItem(id) {
                //Retrieve item list
                let itemList = vm.items;

                //Iterate over all items to find the one with the matching ID
                for (let i = 0; i < itemList.length; i++) {
                    //Check for matching ID
                    if (itemList[i].id === id) {
                        //Item found, remove it from list
                        vm.items.splice(i, 1);
                        break;
                    }
                }
            }

            /**
             * [Public]
             * Updates an item in the item list with new data. In order to be able to locate the item in the list,
             * the ID of the item must stay the same, though.
             * @param updatedItem The new data of the item, including its ID
             */
            function updateItem(updatedItem) {
                //Sanity check
                if (!updatedItem) {
                    return;
                }

                //Retrieve item list
                let itemList = vm.items;

                //Iterate over all items to find the one with the matching ID
                for (let i = 0; i < itemList.length; i++) {
                    //Check for matching ID
                    if (itemList[i].id === updatedItem.id) {
                        //Item found, update it
                        vm.items[i] = updatedItem;
                        break;
                    }
                }
            }

            /**
             * Returns whether a given item is supposed to be displayed or needs to be filtered out
             * based on the current filter term.
             *
             * @param item The item to check
             * @returns {boolean} True, if the item is supposed to be displayed; false otherwise
             */
            function filterByName(item) {
                //Trim filter term and convert to lowercase
                let filterTerm = vm.filterTerm.trim().toLowerCase();

                //Sanity checks
                if ((item == null) || (typeof item === 'undefined') || (!item.hasOwnProperty('name'))) {
                    return false;
                } else if (filterTerm === "") {
                    return true;
                }

                //Get item name, trim it and convert to lowercase
                let itemName = item.name.trim().toLowerCase();

                //Check if filter term is a substring of the item name
                return itemName.includes(filterTerm);
            }

            //Expose
            angular.extend(vm,
                {
                    items: list,
                    pushItem: pushItem,
                    removeItem: removeItem,
                    updateItem: updateItem,
                    nameFilter: filterByName,
                });
        }
    ]);