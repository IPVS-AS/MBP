/* global app */

'use strict';

app.controller('DeleteItemController', ['deleteItem', function (deleteItem) {
        var vm = this;

        // public
        function deleteItemPromise() {
            vm.item.errors = {};
            return deleteItem(vm.item).then(
                    function (data) {
                        // success
                        vm.result = data;
                        vm.success = 'Registered successfully!';
                        
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
                    deleteItem: deleteItemPromise
                });
    }]);

