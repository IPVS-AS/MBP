/* global app */

'use strict';

/**
 * Controller for updating resource items.
 */
app.controller('UpdateItemController', ['updateItem', 'NotificationService', function (updateItem, NotificationService) {
    let vm = this;

    /**
     * [Public]
     * Updates the item.
     * @returns {*} Promise
     * @constructor
     */
    function updateItemPromise() {
        vm.item.errors = {};
        return updateItem(vm.item).then(
            function (data) {
                //Success
                vm.result = data;
                vm.success = 'Updated successfully!';

                //Clean the item object
                vm.item = {};

                //Notify the user
                NotificationService.notify('Entity successfully updated.', 'success')
            },
            function (errors) {
                //Failure, update errors
                vm.item.errors = errors;

                //Notify the user
                NotificationService.notify('Could not update entity.', 'error')
            }
        );

    }

    // Expose
    angular.extend(vm,
        {
            item: {},
            updateItem: updateItemPromise
        });
}]);

