/* global app */

'use strict';

app.controller('DeleteItemController', ['deleteItem', 'confirmDeletion', function (deleteItem, confirmDeletion) {
        var vm = this;

        // public
        function deleteItemWithConfirm(){
            if (typeof confirmDeletion !== 'undefined'){
                confirmDeletion(vm.item).then(function(confirm){
                    if(confirm){
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
                        vm.success = 'Deleted successfully!';
                        
                        // clean form
                        vm.item = {};
                    },
                    function (errors) {
                        // fail - add form errors
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

