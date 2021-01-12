/* global app */

/**
 * Controller for the rules list page.
 */
app.controller('RuleListController',
    ['$scope', '$controller', '$interval', 'ruleList', 'addRule', 'deleteRule', 'ruleActionList', 'ruleTriggerList',
        'RuleService', 'NotificationService',
        function ($scope, $controller, $interval, ruleList, addRule, deleteRule, ruleActionList, ruleTriggerList,
                  RuleService, NotificationService) {

            let vm = this;

            //Expose rule action and trigger lists
            vm.ruleActionList = ruleActionList;
            vm.ruleTriggerList = ruleTriggerList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Check if the rule list was retrieved successfully
                if (ruleList == null) {
                    NotificationService.notify("Could not retrieve rule list.", "error");
                }

                //Prepare and extend rule list
                prepareRuleList();

                //Refresh rule action select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * [Private]
             * Prepares and extends each rule in the rule list by data and functions.
             */
            function prepareRuleList() {
                //Extend rule list with further functions
                for (let i = 0; i < ruleList.length; i++) {
                    //Get current rule
                    let rule = ruleList[i];

                    //Extend rule for toggle function
                    rule.onToggle = createOnToggleFunction(rule.id);
                }
            }


            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule.
             *
             * @param data A data object that contains the id of the rule that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let ruleId = data.id;
                let ruleName = "";

                //Determines the rule's name by checking the list
                for (let i = 0; i < ruleList.length; i++) {
                    if (ruleId === ruleList[i].id) {
                        ruleName = ruleList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete rule',
                    icon: 'warning',
                    html: "Are you sure you want to delete rule \"<strong>" + ruleName + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            /**
             * [Private]
             * Returns a function that toggles the enable state for a rule with a certain id.
             * @param ruleId The id of rule
             * @returns {Function}
             */
            function createOnToggleFunction(ruleId) {
                //Create function and return it
                return function () {
                    //Try to find a rule with this id
                    let rule = null;
                    for (let i = 0; i < ruleList.length; i++) {
                        if (ruleList[i].id === ruleId) {
                            rule = ruleList[i];
                        }
                    }
                    //Rule not found?
                    if (rule == null) {
                        return;
                    }

                    //Check what the user wants
                    if (rule.enabled) {
                        //Enable rule
                        RuleService.enableRule(rule.id).then(function (response) {
                            //Success, notify user
                            rule.enabled = true;
                            NotificationService.notify('Rule enabled successfully.', 'success');
                        }, function () {
                            //Failure
                            rule.enabled = false;
                            NotificationService.notify('Failed to enable rule.', 'error');
                        });
                    } else {
                        //Disable rule
                        RuleService.disableRule(rule.id).then(function (response) {
                            //Success, notify user
                            rule.enabled = false;
                            NotificationService.notify('Rule disabled successfully.', 'success');
                        }, function () {
                            //Failure
                            rule.enabled = true;
                            NotificationService.notify('Failed to disable rule.', 'error');
                        });
                    }
                };
            }

            //Expose controllers
            angular.extend(vm, {
                ruleListCtrl: $controller('ItemListController as ruleListCtrl', {
                    $scope: $scope,
                    list: ruleList
                }),
                addRuleCtrl: $controller('AddItemController as addRuleCtrl', {
                    $scope: $scope,
                    entity: 'rule',
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
                    let rule = vm.addRuleCtrl.result;

                    //Make sure the result is valid
                    if (rule) {
                        //Extend rule for toggle function
                        rule.onToggle = createOnToggleFunction(rule.id);

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
                    let id = vm.deleteRuleCtrl.result;
                    vm.ruleListCtrl.removeItem(id);
                }
            );
        }
    ]);