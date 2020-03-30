/* global app */

/**
 * Controller for the rule actions list page.
 */
app.controller('RuleActionListController',
    ['$scope', '$controller', '$interval', 'ruleActionList', 'ruleActionTypesList', 'actuatorList', 'sensorList',
        'addRuleAction', 'deleteRuleAction', 'RuleService', 'NotificationService', 'DeviceService',
        function ($scope, $controller, $interval, ruleActionList, ruleActionTypesList, actuatorList, sensorList,
                  addRuleAction, deleteRuleAction, RuleService, NotificationService, DeviceService) {
            //Array of colors to be used for the different rule action types
            const ACTION_TYPES_COLORS = ['bg-pink', 'bg-purple', 'bg-deep-purple', 'bg-indigo', 'bg-blue',
                'bg-light-blue', 'bg-cyan', 'bg-teal', 'bg-green', 'bg-light-green', 'bg-lime', 'bg-yellow',
                'bg-amber', 'bg-orange', 'bg-deep-orange'];

            var vm = this;

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
                for (var i = 0; i < ruleActionTypesList.length; i++) {
                    var colorIndex = i % ACTION_TYPES_COLORS.length;
                    ruleActionTypesList[i].color = ACTION_TYPES_COLORS[colorIndex];
                }
            })();

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
                    if (response.data && response.data.success) {
                        //Test succeeded
                        NotificationService.notify("Action test succeeded.", "success")
                    } else {
                        //Test failed
                        NotificationService.notify("Action test failed.", "warning")
                    }
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
                var ruleActionId = data.id;
                var ruleActionName = "";

                //Determines the rule action's name by checking the list
                for (var i = 0; i < ruleActionList.length; i++) {
                    if (ruleActionId === ruleActionList[i].id) {
                        ruleActionName = ruleActionList[i].name;
                        break;
                    }
                }

                return DeviceService.getUsingRules(data.id).then(function (result) {
                    var affectedWarning = "";

                    console.log(" ENTERED IN THE FUNTION ");

                    //If list is not empty, create a message that contains the names of all affected components
                    if (result.data.length > 0) {

                        console.log("daataa");

                        affectedWarning = "<br/><br/><strong>The following components are currently " +
                            "using this device and will be deleted as well:</strong><br/>";

                        for (var i = 0; i < result.data.length; i++) {
                            affectedWarning += "- ";
                            affectedWarning += result.data[i].name;
                            affectedWarning += " (" + result.data[i].component + ")";
                            affectedWarning += "<br/>";
                        }
                    }
                    console.log(" noooo daataa");

                    //Show the alert to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete rule action',
                        type: 'warning',
                        html: "Are you sure you want to delete rule action \"" + ruleActionName + "\"?" + affectedWarning,
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                }, function () {
                    console.log(" ERROOO ");
                    NotificationService.notify("Could not retrieve affected components.", "error");
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
