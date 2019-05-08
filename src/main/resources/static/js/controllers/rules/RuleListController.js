/* global app */

/**
 * Controller for the rules list page.
 */
app.controller('RuleListController',
    ['$scope', '$controller', '$interval', 'ruleList', 'addRule', 'deleteRule', 'ruleActionList', 'ruleTriggerList',
        function ($scope, $controller, $interval, ruleList, addRule, deleteRule, ruleActionList, ruleTriggerList) {
            var vm = this;

            //Expose rule action and trigger lists
            vm.ruleActionList = ruleActionList;
            vm.ruleTriggerList = ruleTriggerList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule.
             *
             * @param data A data object that contains the id of the rule that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var ruleId = data.id;
                var ruleName = "";

                //Determines the rule's name by checking the list
                for (var i = 0; i < ruleList.length; i++) {
                    if (ruleId == ruleList[i].id) {
                        ruleName = ruleList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete rule',
                    type: 'warning',
                    html: "Are you sure you want to delete rule \"" + ruleName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                ruleListCtrl: $controller('ItemListController as ruleListCtrl', {
                    $scope: $scope,
                    list: ruleList
                }),
                addRuleCtrl: $controller('AddItemController as addRuleCtrl', {
                    $scope: $scope,
                    addItem: addRule
                }),
                deleteRuleCtrl: $controller('DeleteItemController as deleteRuleCtrl', {
                    $scope: $scope,
                    deleteItem: deleteRule,
                    confirmDeletion: confirmDelete
                })
            });

            //Watch addition of rules and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addRuleCtrl.result;
                },
                function () {
                    //Callback
                    var rule = vm.addRuleCtrl.result;

                    //Make sure the result is valid
                    if (rule) {
                        //Close modal on success
                        $("#addRuleModal").modal('toggle');

                        //Add rule to list
                        vm.ruleListCtrl.pushItem(rule);
                    }
                }
            );

            //Watch deletion of rules and remove them from the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.deleteRuleCtrl.result;
                },
                function () {
                    //Callback
                    var id = vm.deleteRuleCtrl.result;
                    vm.ruleListCtrl.removeItem(id);
                }
            );
        }
    ]);