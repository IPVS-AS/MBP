/* global app */

/**
 * Controller for the rule triggers list page.
 */
app.controller('RuleTriggerListController',
    ['$scope', '$controller', '$interval', 'ruleTriggerList', 'addRuleTrigger', 'deleteRuleTrigger', 'actuatorList', 'sensorList', 'monitoringComponentList', 'RuleTriggerService', 'NotificationService',
        function ($scope, $controller, $interval, ruleTriggerList, addRuleTrigger, deleteRuleTrigger, actuatorList, sensorList, monitoringComponentList, RuleTriggerService, NotificationService) {
            //Selectors for various DOM elements
            const SELECTOR_ADD_TRIGGER_CARD = "#add-trigger-card";
            const SELECTOR_WIZARD_CONTAINER = "#add-trigger-wizard";
            const SELECTOR_TRIGGER_NAME = "#trigger-name";
            const SELECTOR_TRIGGER_DESCRIPTION = "#trigger-description";
            const SELECTOR_TRIGGER_QUERY = "#trigger-query";
            const SELECTOR_CREATE_ERRORS = "#create-errors";

            let vm = this;

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

                //Ask the server for all rules that use this rule trigger
                return RuleTriggerService.getUsingRules(ruleTriggerId).then(function (result) {
                    //Check if list is empty
                    if (result.length > 0) {
                        //Not empty, entity cannot be deleted
                        let errorText = "The rule condition <strong>" + ruleTriggerName + "</strong> is still used by " +
                            "the following rules and thus cannot be deleted:<br/><br/>";

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
                        title: 'Delete rule condition',
                        icon: 'warning',
                        html: "Are you sure you want to delete the rule condition \"<strong>" + ruleTriggerName +
                            "</strong>\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            /**
             * [Public]
             * Requests the CEP query builder directive to generate a query string for the current query as
             * defined by the user.
             */
            function getQueryString() {
                return vm.queryEditorApi.getQueryString();
            }

            /**
             * [Private]
             * Initializes the wizard that allows to add new triggers and its subcomponents.
             */
            function initWizard() {
                /**
                 * Lets the wizard step back to a step at a certain index.
                 * @param targetIndex The index to step back to
                 */
                function stepBack(targetIndex) {
                    let currentIndex = wizard.steps("getCurrentIndex");

                    if (currentIndex <= targetIndex) {
                        return;
                    }

                    autoStep = true;
                    for (let i = 0; i < (currentIndex - targetIndex); i++) {
                        wizard.steps("previous");
                    }
                    autoStep = false;
                }

                /**
                 * Resets the wizard and all its input elements
                 */
                function resetWizard() {
                    nameInput.val('');
                    descriptionInput.val('');
                    vm.queryEditorApi.reset();
                    queryInput.val('');

                    //Go back to first wizard step
                    stepBack(0);

                    //Hide trigger addition card
                    addTriggerCard.collapse("hide");
                }


                let generatedQuery = "";
                let autoStep = false;
                let failFinish = false;

                //Create wizard
                wizard = $(SELECTOR_WIZARD_CONTAINER).steps({
                    bodyTag: "section",
                    onStepChanging: function (event, currentIndex, newIndex) {
                        if (autoStep) {
                            return true;
                        }

                        //Forward jump from first tab
                        if ((currentIndex === 0) && (newIndex > 0)) {
                            //Get name as provided by the user
                            let name = nameInput.val().trim();

                            //Validate name
                            if ((!name) || (name.length < 1)) {
                                nameInputParent.addClass("focused error");
                                nameInputGroup.addClass("has-error");
                                nameInputHelpBlock.html("The name must not be empty.");
                                return false;
                            }
                            //Check for name duplicates
                            else if (ruleTriggerList.map(trigger => trigger.name).indexOf(name) !== -1) {
                                nameInputParent.addClass("focused error");
                                nameInputGroup.addClass("has-error");
                                nameInputHelpBlock.html("The name is already registered.");
                                return false;
                            } else {
                                //No validation issues
                                nameInputParent.removeClass("focused error");
                                nameInputGroup.removeClass("has-error");
                                nameInputHelpBlock.empty();
                            }
                        }
                        //Forward jump from second tab
                        else if ((currentIndex === 1) && (newIndex > 1)) {
                            //Request query string from CEP query editor
                            generatedQuery = getQueryString();

                            //Check if a query string could be generated
                            if (generatedQuery == null) {
                                return false;
                            }

                            //Hide error container initially
                            errorsContainer.hide();

                            //Insert generated query string
                            queryInput.val(generatedQuery);
                        }
                        //Backward jump from third tab
                        else if ((currentIndex === 2) && (newIndex < 2)) {

                            if (generatedQuery === queryInput.val()) {
                                return true;
                            }

                            Swal.fire({
                                title: 'Step back',
                                icon: 'warning',
                                html: "Are you sure you want to step back? All changes done to the query will be lost!",
                                showCancelButton: true,
                                confirmButtonText: 'Step back',
                                confirmButtonClass: 'bg-red',
                                focusConfirm: false,
                                cancelButtonText: 'Cancel'
                            }).then(function (result) {

                                if (result.value) {
                                    stepBack(newIndex);
                                } else {
                                    wizard.steps("add", {
                                        title: "temp",
                                        content: ""
                                    });
                                    autoStep = true;
                                    wizard.steps("next");
                                    wizard.steps("previous");
                                    autoStep = false;
                                    wizard.steps("remove", (currentIndex + 1));
                                }

                            });

                            return false;
                        }

                        return true;
                    },
                    onFinishing: function (event, currentIndex) {
                        if (failFinish) {
                            failFinish = false;
                            return false;
                        }

                        vm.addRuleTriggerCtrl.item.name = nameInput.val().trim();
                        vm.addRuleTriggerCtrl.item.description = descriptionInput.val().trim();
                        vm.addRuleTriggerCtrl.item.query = queryInput.val();

                        vm.addRuleTriggerCtrl.addItem().then(function (data) {
                            let errors = vm.addRuleTriggerCtrl.item.errors;

                            //Hide and clear the container
                            errorsContainer.hide();
                            errorsContainerList.empty();

                            if (typeof (errors) === "undefined") {
                                resetWizard();
                                return;
                            }

                            //Iterate over all errors
                            for (let key in errors) {
                                if (!errors.hasOwnProperty(key) || (typeof (errors[key].message) === "undefined")) {
                                    continue;
                                }

                                let errorItem = $('<li>').html(errors[key].message);
                                errorsContainerList.append(errorItem);
                            }

                            failFinish = true;
                            wizard.steps("finish");

                            //Show errors container
                            errorsContainer.slideDown();
                        });

                        return true;
                    }
                });

                const addTriggerCard = $(SELECTOR_ADD_TRIGGER_CARD);

                const nameInput = $(SELECTOR_TRIGGER_NAME);
                const nameInputParent = nameInput.parent();
                const nameInputGroup = nameInputParent.parent();
                const nameInputHelpBlock = nameInputParent.next('span.help-block');
                const descriptionInput = $(SELECTOR_TRIGGER_DESCRIPTION);
                const queryInput = $(SELECTOR_TRIGGER_QUERY);

                const errorsContainer = $(SELECTOR_CREATE_ERRORS);
                const errorsContainerList = errorsContainer.find('ul');
                const errorsContainerButton = errorsContainer.children('button');

                //Allow hiding the errors container on button click
                errorsContainerButton.on('click', function () {
                    errorsContainer.hide();
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
                    entity: 'rule trigger',
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
