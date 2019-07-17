/* global app */

/**
 * Controller for the rule triggers list page.
 */
app.controller('RuleTriggerListController',
    ['$scope', '$controller', '$interval', 'ruleTriggerList', 'addRuleTrigger', 'deleteRuleTrigger', 'actuatorList', 'sensorList', 'monitoringComponentList',
        function ($scope, $controller, $interval, ruleTriggerList, addRuleTrigger, deleteRuleTrigger, actuatorList, sensorList, monitoringComponentList) {
            //ID of the add trigger wizard container
            const WIZARD_CONTAINER_ID = "add-trigger-wizard";

            var vm = this;

            //Holds the wizard object for adding triggers
            let wizard = null;

            //Create array of component categories for query editor
            let componentList = [{
                name: 'Sensors',
                resourceName: 'sensor',
                icon: 'settings_remote',
                list: sensorList,
            }, {
                name: 'Actuators',
                resourceName: 'actuator',
                icon: 'wb_incandescent',
                list: actuatorList
            }, {
                name: 'Monitoring',
                resourceName: 'monitoring',
                icon: 'timeline',
                list: monitoringComponentList
            }];

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize wizard
                $(document).ready(initWizard);
            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain rule trigger.
             *
             * @param data A data object that contains the id of the rule trigger that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let ruleTriggerId = data.id;
                let ruleTriggerName = "";

                //Determines the rule trigger's name by checking the list
                for (let i = 0; i < ruleTriggerList.length; i++) {
                    if (ruleTriggerId === ruleTriggerList[i].id) {
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

            /**
             * [Public]
             * Requests the CEP query builder directive to generate a query string for the current query as
             * defined by the user.
             */
            function requestQueryString() {
                return vm.queryEditorApi.requestQueryString();
            }

            /**
             * [Private]
             * Initializes the wizard that allows to add new triggers.
             */
            function initWizard() {
                wizard = $('#' + WIZARD_CONTAINER_ID).steps({
                    bodyTag: "section",
                    onStepChanging: function (event, currentIndex, newIndex) {
                        if ((currentIndex === 1) && (newIndex > 1)) {
                            //Request query string from CEP query editor
                            let queryString = requestQueryString();

                            //Check if a query string could be generated
                            if(queryString == null){
                                return false;
                            }

                            vm.addRuleTriggerCtrl.item.query = queryString;
                        }
                        return true;
                    }
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
                }),
                componentList: componentList
            });

            //Watch addition of rule triggers and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addRuleTriggerCtrl.result;
                },
                function () {
                    //Callback
                    let ruleTrigger = vm.addRuleTriggerCtrl.result;

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
                    let id = vm.deleteRuleTriggerCtrl.result;
                    vm.ruleTriggerListCtrl.removeItem(id);
                }
            );
        }
    ]);