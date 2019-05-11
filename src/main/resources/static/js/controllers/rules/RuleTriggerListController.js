/* global app */

/**
 * Controller for the rule triggers list page.
 */
app.controller('RuleTriggerListController',
    ['$scope', '$controller', '$interval', 'ruleTriggerList', 'addRuleTrigger', 'deleteRuleTrigger',
        function ($scope, $controller, $interval, ruleTriggerList, addRuleTrigger, deleteRuleTrigger) {
            var vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule trigger.
             *
             * @param data A data object that contains the id of the rule trigger that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var ruleTriggerId = data.id;
                var ruleTriggerName = "";

                //Determines the rule trigger's name by checking the list
                for (var i = 0; i < ruleTriggerList.length; i++) {
                    if (ruleTriggerId == ruleTriggerList[i].id) {
                        ruleTriggerName = ruleTriggerList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete rule trigger',
                    type: 'warning',
                    html: "Are you sure you want to delete rule trigger \"" + ruleTriggerName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                ruleTriggerListCtrl: $controller('ItemListController as ruleTriggerListCtrl', {
                    $scope: $scope,
                    list: ruleTriggerList
                }),
                addRuleTriggerCtrl: $controller('AddItemController as addRuleTriggerCtrl', {
                    $scope: $scope,
                    addItem: addRuleTrigger
                }),
                deleteRuleTriggerCtrl: $controller('DeleteItemController as deleteRuleTriggerCtrl', {
                    $scope: $scope,
                    deleteItem: deleteRuleTrigger,
                    confirmDeletion: confirmDelete
                })
            });

            //Watch addition of rule triggers and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addRuleTriggerCtrl.result;
                },
                function () {
                    //Callback
                    var ruleTrigger = vm.addRuleTriggerCtrl.result;

                    //Make sure the result is valid
                    if (ruleTrigger) {
                        //Close modal on success
                        $("#addRuleTriggerModal").modal('toggle');

                        //Add rule trigger to list
                        vm.ruleTriggerListCtrl.pushItem(ruleTrigger);
                    }
                }
            );

            //Watch deletion of rule triggers and remove them from the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.deleteRuleTriggerCtrl.result;
                },
                function () {
                    //Callback
                    var id = vm.deleteRuleTriggerCtrl.result;
                    vm.ruleTriggerListCtrl.removeItem(id);
                }
            );
        }
    ]);