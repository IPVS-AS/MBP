'use strict';

app.controller('AddItemController', ['addItem', function (addItem) {
        var vm = this;

        // public
        function addItemPromise() {
            return addItem(vm.item).then(
                    function (data) {
                        // success
                        vm.result = data;
                        // clean form
                        vm.item = {};
                    },
                    function (errors) {
                        // fail
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

