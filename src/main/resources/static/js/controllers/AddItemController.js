/* global app */

'use strict';

app.controller('AddItemController', ['addItem', 'NotificationService', function (addItem, NotificationService) {
        var vm = this;

        // public
        function addItemPromise() {
            vm.item.errors = {};
            return addItem(vm.item).then(
                    function (data) {
                        //Success
                        vm.result = data;
                        vm.success = 'Registered successfully!';

                        //Clean the form
                        vm.item = {};

                        //Notify the user
                        NotificationService.notify('Entity successfully created.', 'success')
                    },
                    function (errors) {
                        //Failure, add the received errors to the form
                        vm.item.errors = errors;

                        //Notify the user
                        NotificationService.notify('Could not create entity.', 'error')
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

