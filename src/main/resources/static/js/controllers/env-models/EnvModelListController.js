/* global app */

/**
 * Controller for the environment models list page.
 */
app.controller('EnvModelListController',
    ['$scope', '$controller', '$timeout', 'envModelList', 'addEnvModel', 'updateEnvModel', 'deleteEnvModel',
        'keyPairList', 'operatorList', 'deviceTypesList', 'actuatorTypesList', 'sensorTypesList',
        'EnvModelService', 'NotificationService',
        function ($scope, $controller, $timeout, envModelList, addEnvModel, updateEnvModel, deleteEnvModel,
                  keyPairList, operatorList, deviceTypesList, actuatorTypesList, sensorTypesList,
                  EnvModelService, NotificationService) {
            //Get required DOM elements
            const MODEL_EDIT_ENVIRONMENT = $("#model-edit-card");
            const MODEL_PROGRESS_BAR = $("#model-progress");
            const MODEL_ERROR_MESSAGE = $("#model-error-message");

            //Save current scope
            let vm = this;

            //Whether the current model of the modelling tool has been changed and needs to be saved
            vm.saveNecessary = false;

            //Stores properties of the current model (name, description)
            vm.modelProperties = {name: "", description: ""};

            //Text to display on the progress bar
            vm.progressBarText = "";

            //Array of error messages to display
            vm.errorMessageList = "";

            //Internal fields
            let modelSubscription = null; //Subscription to model events
            let isNewModel = true; //Whether the currently edited model is a new one
            let currentModelID = null; //The ID of the currently edited model or null if it is a new model
            let ignorePropertyUpdate = false; //True, if updates of model properties should be ignored for save indication

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize
                $(document).ready(function () {
                    //Hide progress bar
                    hideProgress();

                    //Hide error message
                    hideErrorMessage();

                    //Enable tooltips
                    $('[data-toggle="tooltip"]').tooltip({
                        delay: {"show": 500, "hide": 0}
                    }).on('click', () => {
                        //Hide all tooltips on button click
                        $timeout(() => {
                            $(".tooltip").tooltip("hide");
                        }, 500);
                    });
                });
            })();

            function saveNewModel(modelObject) {
                vm.addEnvModelCtrl.item = modelObject;
                vm.addEnvModelCtrl.addItem().then(function () {
                    //Check for success
                    if (vm.addEnvModelCtrl.success) {
                        //Success, switch from new to existing model
                        isNewModel = false;
                        currentModelID = vm.addEnvModelCtrl.result.id;
                    } else {
                        //Failure handling
                        //TODO
                        console.log(vm.addEnvModelCtrl.item.errors);
                    }
                });
            }

            function saveExistingModel(modelObject) {
                //Pass model object to controller
                vm.updateEnvModelCtrl.item = modelObject;
                //Pass model ID to the controller
                vm.updateEnvModelCtrl.item.id = currentModelID;

                //Create request for updating the model
                vm.updateEnvModelCtrl.updateItem().then(function (data) {
                    //Check for success
                    if (vm.updateEnvModelCtrl.success) {
                        //          console.log(data);
                        //        console.log(vm.updateEnvModelCtrl.result)
                    } else {
                        //Failure handling
                        //TODO
                        console.log(vm.updateEnvModelCtrl.item.errors);
                    }
                });
            }

            function subscribeModel(modelId) {
                /**
                 * Callback for entity state change events.
                 * @param event Event object that has been sent
                 */
                let onEntityStateChange = function (event) {
                    //Parse event data
                    let eventData = JSON.parse(event.data);

                    //Get new entity state
                    let entityState = eventData.entityState.toLowerCase();

                    //Just update node state in the environment model tool accordingly
                    vm.envModelToolApi.updateNodeState(eventData.nodeId, entityState);
                }

                /**
                 * Callback for received component values.
                 * @param event Event object that has been sent
                 */
                let onComponentValueReceived = function (event) {
                    //Parse event data
                    let eventData = JSON.parse(event.data);

                    //Display component value
                    vm.envModelToolApi.displayComponentValue(eventData.nodeId, eventData.unit, eventData.value);
                }

                //Close old subscription if existing
                if (modelSubscription != null) {
                    modelSubscription.close();
                }

                //Subscribe model and pass callback functions
                modelSubscription = EnvModelService.subscribeModel(modelId, onEntityStateChange,
                    onComponentValueReceived);
            }

            /**
             * Loads the states of all registered entities of the current model and updates the environment model
             * tool with them.
             */
            function loadEntityStates() {
                //Show progress
                showProgress("Loading entity states...")

                //Perform request to load the entity states
                EnvModelService.getEntityStates(currentModelID).then(function (response) {
                    //Get entity states
                    let entityStates = response;

                    //Iterate over all entities
                    for (let nodeId of Object.keys(entityStates)) {
                        //Get state of the current entity
                        let state = entityStates[nodeId].toLowerCase();

                        //Update environment model tool accordingly
                        vm.envModelToolApi.updateNodeState(nodeId, state);
                    }

                    //Hide the progress bar
                    hideProgress();

                }, function () {
                    //Failure
                    NotificationService.notify("Failed to load the entity states.", "error");
                    hideProgress();
                });
            }

            /**
             * Shows the progress bar, indicating an action in progress. Optionally, a text may be
             * passed that is supposed to be displayed on the progress bar.
             * @param text The text to display on the progress bar
             */
            function showProgress(text) {
                //Sanity check
                if ((typeof text) === "undefined") {
                    text = "";
                }

                //Set progress bar text
                vm.progressBarText = text;

                //Show progress bar with animation
                MODEL_PROGRESS_BAR.slideDown();
            }

            /**
             * Hides the progress bar.
             */
            function hideProgress() {
                //Hide progress bar with animation
                MODEL_PROGRESS_BAR.slideUp(() => {
                    //Clear progress bar text
                    vm.progressBarText = "";
                });
            }

            /**
             * Displays an object of errors as list of error messages.
             * @param errorObject The error object to display
             */
            function showErrorList(errorObject) {
                //Clear error message array
                vm.errorMessageList = [];

                //Iterate over all fields of the error object
                for (let fieldId in errorObject) {
                    //Check for error field
                    if (!errorObject.hasOwnProperty(fieldId)) {
                        continue;
                    }

                    //Add error message
                    vm.errorMessageList.push(errorObject[fieldId]);
                }

                //Display the message
                showErrorMessage();
            }

            /**
             * Displays the error message.
             */
            function showErrorMessage() {
                //Show container
                MODEL_ERROR_MESSAGE.slideDown();
            }

            /**
             * Hides the error message.
             */
            function hideErrorMessage() {
                //Reset error message array
                vm.errorMessageList = [];

                //Hide container
                MODEL_ERROR_MESSAGE.slideUp();
            }

            /**
             * Handles failures of environment model related action requests by processing the response object.
             *
             * @param response The response of the failed request
             * @param defaultMessage A default message to display as notification
             */
            function handleActionRequestFailure(response, defaultMessage) {
                //Get field errors
                let fieldErrors = response.fieldErrors;

                //Check if there are field errors
                if (fieldErrors && (!$.isEmptyObject(fieldErrors))) {
                    showErrorList(fieldErrors);
                } else {
                    hideErrorMessage();
                }

                //Check if global error message was provided
                if (response.globalMessage) {
                    NotificationService.notify(response.globalMessage, "error");
                } else {
                    //Failure
                    NotificationService.notify(defaultMessage, "error");
                }

                //Hide progress bar
                hideProgress();
            }

            /**
             * [Public]
             * Called when the components of the current model are supposed to be registered.
             */
            function registerComponents() {
                //Show progress bar
                showProgress("Registering...");

                //Perform request
                EnvModelService.registerComponents(currentModelID).then(function (response) {
                    //Success
                    NotificationService.notify("Component registration succeeded.", "success");
                    hideProgress();
                    hideErrorMessage();
                }, function (response) {
                    //Handle failure
                    handleActionRequestFailure(response, "Component registration failed.");
                });
            }

            /**
             * [Public]
             * Called when the components of the current model are supposed to be deployed.
             */
            function deployComponents() {
                //Show progress bar
                showProgress("Deploying...");

                //Perform request
                EnvModelService.deployComponents(currentModelID).then(function (response) {
                    //Success
                    NotificationService.notify("Deployment succeeded.", "success");
                    hideProgress();
                    hideErrorMessage();
                }, function (response) {
                    //Handle failure
                    handleActionRequestFailure(response, "Deployment failed.");
                });
            }


            /**
             * [Public]
             * Called when the components of the current model are supposed to be undeployed.
             */
            function undeployComponents() {
                //Show progress bar
                showProgress("Undeploying...");

                //Perform request
                EnvModelService.undeployComponents(currentModelID).then(function (response) {
                    //Success
                    NotificationService.notify("Undeployment succeeded.", "success");
                    hideProgress();
                    hideErrorMessage();
                }, function (response) {
                    //Handle failure
                    handleActionRequestFailure(response, "Undeployment failed.");
                });
            }


            /**
             * [Public]
             * Called when the components of the current model are supposed to be started.
             */
            function startComponents() {
                //Show progress bar
                showProgress("Starting...");

                //Perform request
                EnvModelService.startComponents(currentModelID).then(function (response) {
                    //Success
                    NotificationService.notify("Components were starteds.", "success");
                    hideProgress();
                    hideErrorMessage();
                }, function (response) {
                    //Handle failure
                    handleActionRequestFailure(response, "Failed to start components.");
                });
            }

            /**
             * [Public]
             * Called when the components of the current model are supposed to be stopped.
             */
            function stopComponents() {
                //Show progress bar
                showProgress("Stopping...");

                //Perform request
                EnvModelService.stopComponents(currentModelID).then(function (response) {
                    //Success
                    NotificationService.notify("Components were stopped.", "success");
                    hideProgress();
                    hideErrorMessage();
                }, function (response) {
                    //Handle failure
                    handleActionRequestFailure(response, "Failed to stop components.");
                });
            }

            /**
             * [Public]
             * Called, when the user wants to create a new model.
             */
            function createNewModel() {
                //Hide modelling tool with callback
                MODEL_EDIT_ENVIRONMENT.slideUp(400, function () {
                    //Load empty model so that the user may create a new one
                    vm.envModelToolApi.loadEmptyModel();

                    //Ignore properties update, since it is not done by the user
                    ignorePropertyUpdate = true;

                    //Set default model properties
                    vm.modelProperties = {
                        name: "Unnamed Model",
                        description: ""
                    };

                    //Set edit mode
                    isNewModel = true;
                    currentModelID = null;

                    //New model, so no save necessary
                    vm.saveNecessary = false;

                    //Show modelling tool again
                    MODEL_EDIT_ENVIRONMENT.slideDown();
                });
            }

            /**
             * [Public]
             * Called, when the user wants to edit a model of the model list.
             */
            function editModel(modelID) {
                //Will hold the model that is supposed to be edited
                let modelToEdit = null;

                //Iterate over all models in the model list to find the matching model
                for (let i = 0; i < envModelList.length; i++) {
                    //Check for matching ID
                    if (envModelList[i].id === modelID) {
                        //Model found
                        modelToEdit = envModelList[i];
                        break;
                    }
                }

                //Check if model could be found
                if (modelToEdit == null) {
                    return;
                }

                //Hide modelling tool with callback
                MODEL_EDIT_ENVIRONMENT.slideUp(400, function () {
                    //Ignore properties update, since it is not done by the user
                    ignorePropertyUpdate = true;

                    //Set model properties
                    vm.modelProperties = {
                        name: modelToEdit.name,
                        description: modelToEdit.description
                    };

                    //Set edit mode
                    isNewModel = false;
                    currentModelID = modelToEdit.id;

                    //New model, so no save necessary
                    vm.saveNecessary = false;

                    //Display modelling tool again
                    MODEL_EDIT_ENVIRONMENT.slideDown(400, function () {
                        //Load model to make it editable
                        vm.envModelToolApi.loadModel(modelToEdit.modelJSON);

                        //Subscribe to model
                        subscribeModel(currentModelID);

                        //Update states of all registered entities
                        loadEntityStates();
                    });
                });
            }

            /**
             * [Public]
             * Called, when the user wants to save a model by clicking on the save button of the modelling tool
             * menu bar.
             */
            function saveModel() {
                //Get and parse model name and description
                let modelName = vm.modelProperties.name.trim() || "";
                let modelDescription = vm.modelProperties.description.trim() || "";

                //Export current model as JSON string
                let modelJSON = vm.envModelToolApi.getModelJSON();

                //Create object for the model
                let modelObject = {
                    name: modelName,
                    description: modelDescription,
                    modelJSON: modelJSON
                };

                //Check if the current model is a new model
                if (isNewModel) {
                    //New model, create it
                    saveNewModel(modelObject);
                } else {
                    //Existing model, update it
                    saveExistingModel(modelObject);
                }

                //Model was saved, no save necessary anymore
                vm.saveNecessary = false;
            }

            /**
             * [Public]
             * Callback that is triggered in case the current model of the environment modelling tool has changed.
             */
            function onModelChanged() {
                //Model has been changed and needs to be saved
                vm.saveNecessary = true;
                //$scope.$digest();
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain environment model.
             *
             * @param data A data object that contains the id of the environment model that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let envModelId = data.id;
                let envModelName = "";

                //Determines the environment model's name by checking the list
                for (let i = 0; i < envModelList.length; i++) {
                    if (envModelId === envModelList[i].id) {
                        envModelName = envModelList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete environment model',
                    type: 'warning',
                    html: "Are you sure you want to delete environment model \"" + envModelName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                envModelListCtrl: $controller('ItemListController as envModelListCtrl', {
                    $scope: $scope,
                    list: envModelList
                }),
                addEnvModelCtrl: $controller('AddItemController as addEnvModelCtrl', {
                    $scope: $scope,
                    entity: 'environment model',
                    addItem: addEnvModel
                }),
                updateEnvModelCtrl: $controller('UpdateItemController as updateEnvModelCtrl', {
                    $scope: $scope,
                    updateItem: updateEnvModel
                }),
                deleteEnvModelCtrl: $controller('DeleteItemController as deleteEnvModelCtrl', {
                    $scope: $scope,
                    deleteItem: deleteEnvModel,
                    confirmDeletion: confirmDelete
                }),
                keyPairList: keyPairList,
                operatorList: operatorList,
                deviceTypesList: deviceTypesList,
                actuatorTypesList: actuatorTypesList,
                sensorTypesList: sensorTypesList,
                registerComponents: registerComponents,
                deployComponents: deployComponents,
                undeployComponents: undeployComponents,
                startComponents: startComponents,
                stopComponents: stopComponents,
                createNewModel: createNewModel,
                editModel: editModel,
                saveModel: saveModel,
                onModelChanged: onModelChanged
            });

            //Watch change of model properties and indicate necessary saving
            $scope.$watch(function () {
                //Value being watched
                return vm.modelProperties.name + vm.modelProperties.description;
            }, function () {
                //Callback on change; check if property update is supposed to be ignored
                if (ignorePropertyUpdate) {
                    //Update ignored, but take next one serious
                    ignorePropertyUpdate = false;
                } else {
                    //Do not ignore, indicate a necessary save
                    vm.saveNecessary = true;
                }
            });

            //Watch addition of environment models and add them to the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addEnvModelCtrl.result;
                },
                function () {
                    //Callback
                    let envModel = vm.addEnvModelCtrl.result;

                    //Make sure the result is valid
                    if (envModel) {
                        //Add environment model to list
                        vm.envModelListCtrl.pushItem(envModel);
                    }
                }
            );

            //Watch update of environment models and update them in the list accordingly
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.updateEnvModelCtrl.result;
                },
                function () {
                    //Callback
                    let updatedModel = vm.updateEnvModelCtrl.result;
                    vm.envModelListCtrl.updateItem(updatedModel);
                }
            );

            //Watch deletion of environment models and remove them from the list
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.deleteEnvModelCtrl.result;
                },
                function () {
                    //Callback
                    let id = vm.deleteEnvModelCtrl.result;
                    vm.envModelListCtrl.removeItem(id);
                }
            );
        }
    ]);