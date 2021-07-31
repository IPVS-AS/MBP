/**
 * Controller for the dynamic deployments list page.
 */
app.controller('DynamicDeploymentListController',
    ['$scope', '$controller', '$timeout', 'dynamicDeploymentList', 'addDynamicDeployment', 'deleteDynamicDeployment',
        'operatorList', 'deviceTemplateList', 'DiscoveryService', 'NotificationService',
        function ($scope, $controller, $timeout, dynamicDeploymentList, addDynamicDeployment, deleteDynamicDeployment,
                  operatorList, deviceTemplateList, DiscoveryService, NotificationService) {

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Refresh select picker when the modal is opened
                $('.modal').on('shown.bs.modal', () => {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * [Public]
             * Toggles the activation intention of a certain dynamic deployment, given by its ID.
             *
             * @param id The ID of the dynamic deployment
             */
            function toggleActivationIntention(id) {
                //Find dynamic deployment with the given ID
                let dynamicDeployment = dynamicDeploymentList.find(d => d.id === id);

                //Null check
                if (dynamicDeployment == null) return;

                //Check whether activating or deactivating is desired
                if (dynamicDeployment.activatingIntended) {
                    //Activate the deployment
                    DiscoveryService.activateDynamicDeployment(id).then(function () {
                        //Notify the user
                        NotificationService.notify("The dynamic deployment was activated.", "success");

                        //Update the status
                        reloadDeploymentState(id);
                    });
                } else {
                    //Deactivate the deployment
                    DiscoveryService.deactivateDynamicDeployment(id).then(function () {
                        //Notify the user
                        NotificationService.notify("The dynamic deployment was deactivated.", "success");

                        //Update the status
                        reloadDeploymentState(id);
                    });
                }
            }


            /**
             * [Public]
             * Reloads and updates the deployment state of a certain dynamic deployment, given by its ID.
             *
             * @param id The ID of the dynamic deployment
             */
            function reloadDeploymentState(id) {
                //Find dynamic deployment with the given ID
                let dynamicDeployment = dynamicDeploymentList.find(d => d.id === id);

                //Null check
                if (dynamicDeployment == null) return;

                //Set status to loading
                dynamicDeployment.lastState = 'loading';

                //Retrieve details data about the dynamic deployment
                DiscoveryService.getDynamicDeployment(id).then(function (data) {
                    //Update fields
                    $timeout(function () {
                        dynamicDeployment.inProgress = data.inProgress;
                        dynamicDeployment.lastState = data.lastState;
                        dynamicDeployment.activatingIntended = data.activatingIntended;
                    }, 10);
                }, function () {
                    //Failed, set fields to failure
                    $timeout(function () {
                        dynamicDeployment.inProgress = false;
                        dynamicDeployment.lastState = "";
                    }, 10);
                });
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain dynamic deployment.
             *
             * @param data A data object that contains the id of the dynamic deployment that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let dynamicDeploymentId = data.id;
                let name = "";

                //Determines the name of the dynamic deployment by checking the list
                for (let i = 0; i < dynamicDeploymentList.length; i++) {
                    if (dynamicDeploymentId === dynamicDeploymentList[i].id) {
                        name = dynamicDeploymentList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete dynamic deployment',
                    icon: 'warning',
                    html: "Are you sure you want to delete the dynamic deployment with name \"<strong>" + name + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }


            //Watch controller result of dynamic deployment additions
            $scope.$watch(() => vm.addDynamicDeploymentCtrl.result, (data) => {
                    //Sanity check
                    if (!data) return;

                    //Add dynamic deployment to list
                    vm.dynamicDeploymentListCtrl.pushItem(data);

                    //Close modal on success
                    $("#addDynamicDeploymentModal").modal('toggle');
                }
            );

            //Watch controller result of dynamic deployment deletions
            $scope.$watch(() => vm.deleteDynamicDeploymentCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, remove dynamic deployment from list
                vm.dynamicDeploymentListCtrl.removeItem(vm.deleteDynamicDeploymentCtrl.result);
            });

            //Expose controllers
            angular.extend(vm, {
                dynamicDeploymentListCtrl: $controller('ItemListController as dynamicDeploymentListCtrl', {
                    $scope: $scope,
                    list: dynamicDeploymentList
                }),
                addDynamicDeploymentCtrl: $controller('AddItemController as addDynamicDeploymentCtrl', {
                    $scope: $scope,
                    entity: 'dynamic deployment',
                    addItem: addDynamicDeployment
                }),
                deleteDynamicDeploymentCtrl: $controller('DeleteItemController as deleteDynamicDeploymentCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDynamicDeployment,
                    confirmDeletion: confirmDelete
                }),
                operatorList: operatorList,
                deviceTemplateList: deviceTemplateList,
                toggleActivationIntention: toggleActivationIntention,
                reloadDeploymentState: reloadDeploymentState
            });
        }
    ]);