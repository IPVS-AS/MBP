/* global app */

/**
 * Controller for the rule triggers list page.
 */
app.controller('RuleTriggerListController',
    ['$scope', '$controller', '$interval', 'ruleTriggerList', 'addRuleTrigger', 'deleteRuleTrigger', 'actuatorList', 'sensorList', 'monitoringComponentList',
        function ($scope, $controller, $interval, ruleTriggerList, addRuleTrigger, deleteRuleTrigger, actuatorList, sensorList, monitoringComponentList) {
            //Selectors for certain DOM elements
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
             * Initializes the wizard that allows to add new triggers and its subcomponents.
             */
            function initWizard() {
                let generatedQuery = "";
                let autoStep = false;

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
                                nameInputHelpBlock.html("");
                            }
                        }
                        //Forward jump from second tab
                        else if ((currentIndex === 1) && (newIndex > 1)) {
                            //Request query string from CEP query editor
                            generatedQuery = requestQueryString();

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
                                type: 'warning',
                                html: "Are you sure you want to step back? All changes done to the query will be lost!",
                                showCancelButton: true,
                                confirmButtonText: 'Step back',
                                confirmButtonClass: 'bg-red',
                                focusConfirm: false,
                                cancelButtonText: 'Cancel'
                            }).then(function (result) {
                                autoStep = true;

                                if (result.value) {
                                    for (let i = 0; i < (currentIndex - newIndex); i++) {
                                        wizard.steps("previous");
                                    }
                                } else {
                                    wizard.steps("add", {
                                        title: "temp",
                                        content: ""
                                    });
                                    wizard.steps("next");
                                    wizard.steps("previous");
                                    wizard.steps("remove", (currentIndex + 1));
                                }

                                autoStep = false;
                            });

                            return false;
                        }

                        return true;
                    },
                    onFinishing: async function (event, currentIndex) {
                        vm.addRuleTriggerCtrl.item.name = nameInput.val().trim();
                        vm.addRuleTriggerCtrl.item.description = descriptionInput.val().trim();
                        vm.addRuleTriggerCtrl.item.query = queryInput.val();

                        return await vm.addRuleTriggerCtrl.addItem().then(function (data) {
                            let errors = vm.addRuleTriggerCtrl.item.errors;

                            //Hide and clear the container
                            errorsContainer.hide();
                            errorsContainerList.html('');

                            if (typeof (errors) === "undefined") {
                                return true;
                            }

                            for (let key in errors) {
                                if (!errors.hasOwnProperty(key) || (typeof (errors[key].message) === "undefined")) {
                                    continue;
                                }

                                let errorItem = $('<li>').html(errors[key].message);
                                errorsContainerList.append(errorItem);
                            }

                            //Show errors container
                            errorsContainer.slideDown();

                            return false;
                        });
                    }
                });

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