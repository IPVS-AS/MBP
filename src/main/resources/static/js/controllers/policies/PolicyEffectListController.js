/* global app */

/**
 * Controller for the policy effects list page.
 */
app.controller('PolicyEffectListController',
    ['$scope', '$controller', '$interval', 'policyEffectList', 'addPolicyEffect', 'deletePolicyEffect', 'NotificationService', 'PolicyEffectService',
        function ($scope, $controller, $interval, policyEffectList, addPolicyEffect, deletePolicyEffect, NotificationService, PolicyEffectService) {

            var vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                // Refresh policy effect action type select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker({
                        showTick: true,
                        refresh: true
                    });
                });
            })();

            /**
             * [Public]
             * Shows an alert to confirm the delete action.
             *
             * @param data A data object that contains the id of the policy effect that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var policyEffectId = data.id;
                var policyEffectName = "";

                // Determines the policy effect's name by checking the list
                for (var i = 0; i < policyEffectList.length; i++) {
                    if (policyEffectId === policyEffectList[i].id) {
                        policyEffectName = policyEffectList[i].name;
                        break;
                    }
                }
                // Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete policy effect',
                    type: 'warning',
                    html: "Are you sure you want to delete policy effect \"" + policyEffectName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            // Expose controllers
            angular.extend(vm, {
                policyEffectListCtrl: $controller('ItemListController as policyEffectListCtrl', {
                    $scope: $scope,
                    list: policyEffectList
                }),
                addPolicyEffectCtrl: $controller('AddItemController as addPolicyEffectCtrl', {
                    $scope: $scope,
                    entity: 'policy effect',
                    addItem: addPolicyEffect
                }),
                deletePolicyEffectCtrl: $controller('DeleteItemController as deletePolicyEffectCtrl', {
                    $scope: $scope,
                    deleteItem: deletePolicyEffect,
                    confirmDeletion: confirmDelete
                })
            });

            // Watch addition of policy effects and add them to the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.addPolicyEffectCtrl.result;
                },
                function () {
                    // Callback
                    var policyEffect = vm.addPolicyEffectCtrl.result;

                    // Make sure the result is valid
                    if (policyEffect) {
                        // Close modal on success
                        $("#addPolicyEffectModal").modal('toggle');

                        // Add policy effect to list
                        vm.policyEffectListCtrl.pushItem(policyEffect);
                    }
                }
            );

            // Watch deletion of policy effects and remove them from the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.deletePolicyEffectCtrl.result;
                },
                function () {
                    // Callback
                    var id = vm.deletePolicyEffectCtrl.result;
                    vm.policyEffectListCtrl.removeItem(id);
                }
            );
        }
    ]);
