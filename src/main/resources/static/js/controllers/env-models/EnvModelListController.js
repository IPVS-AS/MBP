/* global app */

/**
 * Controller for the environment models list page.
 */
app.controller('EnvModelListController',
    ['$scope', '$controller', '$interval', 'envModelList', 'addEnvModel', 'deleteEnvModel',
        function ($scope, $controller, $interval, envModelList, addEnvModel, deleteEnvModel) {

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Initialize wizard
                $(document).ready(function () {

                });
            })();

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
                componentList: componentList
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