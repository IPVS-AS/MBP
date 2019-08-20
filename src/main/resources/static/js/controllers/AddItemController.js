/* global app */

'use strict';

app.controller('AddItemController', ['addItem', 'getDeviceKey', 'NotificationService', function (addItem, getDeviceKey, NotificationService) {
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
                        NotificationService.notify('Entitiy successfully created.', 'success')
                    },
                    function (errors) {
                        //Failure, add the received errors to the form
                        vm.item.errors = errors;

                        //Notify the user
                        NotificationService.notify('Could not create entitiy.', 'error')
                    }
            );

        }

        function receiveKey() {
            vm.item.errors = {};
            return getDeviceKey(vm.item).then(
                function (data) {
                    vm.result = data + 1234;
                    vm.success = 'Got device key';

                    //Notify the user
                    NotificationService.notify('Received device key.', 'success')
                },
                function (errors) {
                    //Failure, add the received errors to the form
                    vm.item.errors = errors;

                    //Notify the user
                    NotificationService.notify('Could not create entitiy.', 'error')
                }
            )
        }

        // expose
        angular.extend(vm,
                {
                    item: {},
                    addItem: addItemPromise,
                    getDeviceKey: receiveKey
        });
    }]);

