/* global app */

/**
 * Controller for the environment models list page.
 */
app.controller('EnvModelListController',
    ['$scope', '$controller', '$interval', 'envModelList', 'addEnvModel', 'deleteEnvModel', 'adapterList', 'deviceTypesList',
        function ($scope, $controller, $interval, envModelList, addEnvModel, deleteEnvModel, adapterList, deviceTypesList) {

            //Save current scope
            let vm = this;

            //Whether the current model of the modelling tool has been changed and needs to be saved
            vm.saveNecessary = false;

            //Internal fields
            let isNewModel = true; //Whether the currently edited model is a new one
            let modelID = null; //The ID of the currently edited model or null if it is a new model

            console.log("Model list:");
            console.log(envModelList);

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize
                $(document).ready(function () {
                    //Enable tooltips
                    $('[data-toggle="tooltip"]').tooltip({
                        delay: {"show": 500, "hide": 0}
                    }).on('click', function () {
                        //Hide tooltip in button click
                        $(this).tooltip("hide");
                    });
                });
            })();

            function createNewModel(modelObject) {
                vm.addEnvModelCtrl.item = modelObject;
                vm.addEnvModelCtrl.addItem().then(function (data) {
                    console.log(data);
                });
            }

            function updateExistingModel(modelObject) {

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
                    createNewModel(modelObject);
                } else {
                    //Existing model, update it
                    updateExistingModel(modelObject);
                }

                //Model was saved, no save necessary anymore
                vm.saveNecessary = false;
            }

            /**
             * [Public]
             * Callback that is triggered in case the current model of the environment modelling tool has changed.
             */
            function onModelChanged() {
                console.log("Model changed");

                //Model has been changed and needs to be saved
                vm.saveNecessary = true;
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
                    addItem: addEnvModel
                }),
                deleteEnvModelCtrl: $controller('DeleteItemController as deleteEnvModelCtrl', {
                    $scope: $scope,
                    deleteItem: deleteEnvModel,
                    confirmDeletion: confirmDelete
                }),
                adapterList: adapterList,
                deviceTypesList: deviceTypesList,
                saveModel: saveModel,
                onModelChanged: onModelChanged
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