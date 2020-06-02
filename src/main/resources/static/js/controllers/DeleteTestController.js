/* global app */

'use strict';

app.controller('DeleteTestController', ['deleteItem', 'confirmDeletion', '$http', 'ENDPOINT_URI','NotificationService',
    function (deleteItem, confirmDeletion, $http,ENDPOINT_URI, NotificationService) {
        var vm = this;

        /**
         * [Public]
         * Shows an alert that asks the user if he is sure that he wants to delete a specific item. If the user
         * confirms, the item will be deleted. If no alert function is defined in confirmDeletion, the item
         * will be deleted without asking the user.
         */
        function deleteItemWithConfirm() {
            //Check if an alert function is defined
            if (typeof confirmDeletion !== 'undefined') {
                //Function is defined, ask the user
                confirmDeletion(vm.item).then(function (result) {
                    //Check if the user confirmed the deletion
                    if (result.value) {
                        deleteItemPromise();

                    }
                });
                return;
            }
            //No alert function defined, delete item without asking the user
            deleteItemPromise();
        }

        /**
         * [Private]
         * Creates a server request with promise for the deletion of a specific item and a server request to delete the test report and the corresponding diagram of the simulated data.
         * @returns The promise of the server request
         */
        function deleteItemPromise() {
            vm.item.errors = {};
            console.log(vm.item.id);
            $http.post(ENDPOINT_URI + '/test-details/deleteTestreport/' + vm.item.id).then(function success(response) {
                console.log(response);
            });
            //Create deletion request and return the resulting promise
            return deleteItem(vm.item).then(
                function (data) {
                    //Success
                    vm.result = data;
                    vm.success = 'Deleted successfully';

                    //Clean the form
                    vm.item = {};

                    //Notify the user
                    NotificationService.notify('Entitiy successfully deleted.', 'success')
                },
                function (errors) {
                    //Failure, add the received errors to the form
                    vm.item.errors = errors;

                    //Notify the user
                    NotificationService.notify('Could not delete entitiy.', 'error');
                }
            );
        }

        //Expose
        angular.extend(vm,
            {
                item: {},
                deleteItem: deleteItemWithConfirm
            });
    }]);

