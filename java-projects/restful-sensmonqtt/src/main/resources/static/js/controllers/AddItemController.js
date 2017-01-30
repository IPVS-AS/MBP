/* global app */

'use strict';

app.controller('AddItemController', ['addItem', function (addItem) {
        var vm = this;

        // public
        function addItemPromise() {
            vm.item.errors = {};
            return addItem(vm.item).then(
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
                    addItem: addItemPromise
                });
    }]);

