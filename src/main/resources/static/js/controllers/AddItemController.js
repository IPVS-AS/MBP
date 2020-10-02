/* global app */

'use strict';

app.controller('AddItemController', ['$rootScope', 'addItem', 'NotificationService', function ($rootScope, addItem, NotificationService) {
        let vm = this;

        // public
        function addItemPromise() {
            vm.item.errors = {};
            return addItem(vm.item).then(
                    function (data) {
                        //Success
                        vm.result = data;
                        vm.success = 'Registered successfully!';

                        //Clean the item object
                        vm.item = {};

                        //Notify the user
                        NotificationService.notify('Entity successfully created.', 'success')
                    },
                    function (errors) {
                        //Failure, add the received errors to the item object
                        vm.item.errors = errors;

                        //Notify the user
                        //NotificationService.notify('Could not create entity.', 'error')
                    }
            ).then(function (){
                //Trigger angular $watch checks
                $rootScope.$digest();
            });
        }

        // expose
        angular.extend(vm,
                {
                    item: {},
                    addItem: addItemPromise,
        });
    }]);

