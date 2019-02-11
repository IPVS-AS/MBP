/* global app */

'use strict';

app.controller('DeleteItemController', ['deleteItem', 'confirmDeletion', 'NotificationService',
    function (deleteItem, confirmDeletion, NotificationService) {
        var vm = this;

        // public
        function deleteItemWithConfirm(){
            if (typeof confirmDeletion !== 'undefined'){
                confirmDeletion(vm.item).then(function(result){
                    if(result.value){
                        deleteItemPromise();
                    }
                });
                return;
            }
            deleteItemPromise();
        }

        function deleteItemPromise() {
            vm.item.errors = {};

            return deleteItem(vm.item).then(
                    function (data) {
                        // success
                        vm.result = data;
                        vm.success = 'Deleted successfully';
                        
                        // clean form
                        vm.item = {};

                        //Notify the user
                        NotificationService.notify('Successfully deleted.', 'success')
                    },
                    function (errors) {
                        // Fail - add form errors
                        vm.item.errors = errors;
                    }
            );

        }

        // expose
        angular.extend(vm,
                {
                    item: {},
                    deleteItem: deleteItemWithConfirm
                });
    }]);

