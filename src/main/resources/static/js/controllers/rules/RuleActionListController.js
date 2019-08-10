/* global app */

/**
 * Controller for the rule actions list page.
 */
app.controller('RuleActionListController',
    ['$scope', '$controller', '$interval', 'ruleActionList', 'ruleActionTypesList', 'deviceList', 'addRuleAction', 'deleteRuleAction',
        function ($scope, $controller, $interval, ruleActionList, ruleActionTypesList, deviceList, addRuleAction, deleteRuleAction) {
            var vm = this;

            vm.ruleActionTypesList = ruleActionTypesList;
            vm.deviceList = deviceList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for rule action types list
                if (ruleActionTypesList.length < 1) {
                    NotificationService.notify("Could not load rule action types.", "error");
                }

                console.log("Types:");
                console.log(ruleActionTypesList);
                console.log("Devices:");
                console.log(deviceList);
            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule action.
             *
             * @param data A data object that contains the id of the rule action that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var ruleActionId = data.id;
                var ruleActionName = "";

                //Determines the rule action's name by checking the list
                for (var i = 0; i < ruleActionList.length; i++) {
                    if (ruleActionId === ruleActionList[i].id) {
                        ruleActionName = ruleActionList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete rule action',
                    type: 'warning',
                    html: "Are you sure you want to delete rule action \"" + ruleActionName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                ruleActionListCtrl: $controller('ItemListController as ruleActionListCtrl', {
                    $scope: $scope,
                    list: ruleActionList
                }),
                addRuleActionCtrl: $controller('AddItemController as addRuleActionCtrl', {
                    $scope: $scope,
                    //TODO
                    addItem: function (data) {
                        return addRuleAction(data);
                    }
                }),
                deleteRuleActionCtrl: $controller('DeleteItemController as deleteRuleActionCtrl', {
                    $scope: $scope,
                    deleteItem: deleteRuleAction,
                    confirmDeletion: confirmDelete
                })
            });

            //Watch addition of rule actions and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addRuleActionCtrl.result;
                },
                function () {
                    //Callback
                    var ruleAction = vm.addRuleActionCtrl.result;

                    //Make sure the result is valid
                    if (ruleAction) {
                        //Close modal on success
                        $("#addRuleActionModal").modal('toggle');

                        //Add rule action to list
                        vm.ruleActionListCtrl.pushItem(ruleAction);
                    }
                }
            );

            //Watch deletion of rule actions and remove them from the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.deleteRuleActionCtrl.result;
                },
                function () {
                    //Callback
                    var id = vm.deleteRuleActionCtrl.result;
                    vm.ruleActionListCtrl.removeItem(id);
                }
            );
        }
    ]);