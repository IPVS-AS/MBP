/* global app */

/**
 * Controller for the rule actions list page.
 */
app.controller('RuleActionListController',
    ['$scope', '$controller', '$interval', 'ruleActionList', 'ruleActionTypesList', 'actuatorList', 'sensorList',
        'addRuleAction', 'deleteRuleAction', 'RuleService', 'NotificationService', 'RuleActionService',
        function ($scope, $controller, $interval, ruleActionList, ruleActionTypesList, actuatorList, sensorList,
                  addRuleAction, deleteRuleAction, RuleService, NotificationService, RuleActionService) {
            //Array of colors to be used for the different rule action types
            const ACTION_TYPES_COLORS = ['bg-pink', 'bg-purple', 'bg-deep-purple', 'bg-indigo', 'bg-blue',
                'bg-light-blue', 'bg-cyan', 'bg-teal', 'bg-green', 'bg-light-green', 'bg-lime', 'bg-yellow',
                'bg-amber', 'bg-orange', 'bg-deep-orange'];

            let vm = this;

            vm.ruleActionTypesList = ruleActionTypesList;
            vm.actuatorList = actuatorList;
            vm.sensorList = sensorList;
            vm.componentList = actuatorList.concat(sensorList);

            //Extend actuator and sensor objects for type field
            vm.actuatorList.forEach(actuator => actuator.type = 'Actuators');
            vm.sensorList.forEach(sensor => sensor.type = 'Sensors');

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for rule action types list
                if (ruleActionTypesList.length < 1) {
                    NotificationService.notify("Could not load rule action types.", "error");
                }

                //Extend rule action types for color
                for (let i = 0; i < ruleActionTypesList.length; i++) {
                    let colorIndex = i % ACTION_TYPES_COLORS.length;
                    ruleActionTypesList[i].color = ACTION_TYPES_COLORS[colorIndex];
                }

                // Refresh rule action select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    refreshSelectPicker()
                });
                // Refresh select pickers when action type is changed
                $('.selectpicker').on('changed.bs.select', function () {
                    refreshSelectPicker()
                });

            })();

            /**
             * Refresh select pickers
             */
            function refreshSelectPicker() {
                $('.selectpicker').selectpicker({
                    showTick: true,
                    refresh: true
                });
            }

            /**
             * [Public]
             * Performs a server request in order to test a rule action given by its id. The result is displayed
             * in an user notification subsequently.
             *
             * @param actionId The id of the rule action to test
             */
            function testRuleAction(actionId) {
                //Execute request
                RuleService.testRuleAction(actionId).then(function (response) {
                    NotificationService.notify("Action test succeeded.", "success")
                }, function (response) {
                    //Server request failed
                    NotificationService.notify("Unable to perform action test.", "error")
                });
            }

            /**
             * [Public]
             * Returns the color class for a certain rule action type given by its ID.
             *
             * @param actionTypeId The id of the action type
             */
            function getActionTypeColor(actionTypeId) {
                let actionType = ruleActionTypesList.find(function (type) {
                    return type.id === actionTypeId;
                });

                if (actionType) {
                    return actionType.color;
                }

                return "bg-grey";
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule action.
             *
             * @param data A data object that contains the id of the rule action that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let ruleActionId = data.id;
                let ruleActionName = "";

                //Determines the rule action's name by checking the list
                for (let i = 0; i < ruleActionList.length; i++) {
                    if (ruleActionId === ruleActionList[i].id) {
                        ruleActionName = ruleActionList[i].name;
                        break;
                    }
                }

                //Ask the server for all rules that use this rule action
                return RuleActionService.getUsingRules(data.id).then(function (result) {
                    //Check if list is empty
                    if (result.length > 0) {
                        //Not empty, entity cannot be deleted
                        let errorText = "The rule action <strong>" + ruleActionName + "</strong> is still used by the " +
                            "following rules and thus cannot be deleted:<br/><br/>";

                        //Iterate over all affected entities
                        for (let i = 0; i < result.length; i++) {
                            errorText += "- " + result[i].name + "<br/>";
                        }

                        // Show error message
                        Swal.fire({
                            icon: 'error',
                            title: 'Deletion impossible',
                            html: errorText
                        })

                        // Return new promise as result
                        return Promise.resolve({value: false});
                    }

                    //Show confirm prompt to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete rule action',
                        icon: 'warning',
                        html: "Are you sure you want to delete the rule action \"<strong>" + ruleActionName +
                            "</strong>\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
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
                    entity: 'rule action',
                    addItem: addRuleAction
                }),
                deleteRuleActionCtrl: $controller('DeleteItemController as deleteRuleActionCtrl', {
                    $scope: $scope,
                    deleteItem: deleteRuleAction,
                    confirmDeletion: confirmDelete
                }),
                testRuleAction: testRuleAction,
                getActionTypeColor: getActionTypeColor
            });

            //Watch addition of rule actions and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addRuleActionCtrl.result;
                },
                function () {
                    //Callback
                    let ruleAction = vm.addRuleActionCtrl.result;

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
                    let id = vm.deleteRuleActionCtrl.result;
                    vm.ruleActionListCtrl.removeItem(id);
                }
            );
        }
    ]);
