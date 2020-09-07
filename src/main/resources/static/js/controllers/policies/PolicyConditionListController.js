/* global app */

/**
 * Controller for the policy conditions list page.
 */
app.controller('PolicyConditionListController',
    ['$scope', '$controller', '$interval', 'policyConditionList', 'addPolicyCondition', 'deletePolicyCondition', 'NotificationService', 'PolicyConditionService',
        function ($scope, $controller, $interval, policyConditionList, addPolicyCondition, deletePolicyCondition, NotificationService, PolicyConditionService) {

            var vm = this;
            // var queryResult = $scope.$('#condition-builder').queryBuilder('getRules');

            /**
             * [Public]
             * Shows an alert to confirm the delete action.
             *
             * @param data A data object that contains the id of the policy condition that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var policyConditionId = data.id;
                var policyConditionName = "";

                // Determines the policy condition's name by checking the list
                for (var i = 0; i < policyConditionList.length; i++) {
                    if (policyConditionId === policyConditionList[i].id) {
                        policyConditionName = policyConditionList[i].name;
                        break;
                    }
                }
                // Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete policy condition',
                    type: 'warning',
                    html: "Are you sure you want to delete policy condition \"" + policyConditionName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            // Expose controllers
            angular.extend(vm, {
                policyConditionListCtrl: $controller('ItemListController as policyConditionListCtrl', {
                    $scope: $scope,
                    list: policyConditionList
                }),
                addPolicyConditionCtrl: $controller('AddItemController as addPolicyConditionCtrl', {
                    $scope: $scope,
                    addItem: addPolicyCondition
                }),
                deletePolicyConditionCtrl: $controller('DeleteItemController as deletePolicyConditionCtrl', {
                    $scope: $scope,
                    deleteItem: deletePolicyCondition,
                    confirmDeletion: confirmDelete
                })
            });

            // Watch addition of policy conditions and add them to the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.addPolicyConditionCtrl.result;
                },
                function () {
                    // Callback
                    var policyCondition = vm.addPolicyConditionCtrl.result;

                    // Make sure the result is valid
                    if (policyCondition) {
                        // Close modal on success
                        $("#addPolicyConditionModal").modal('toggle');

                        // Add policy condition to list
                        vm.policyConditionListCtrl.pushItem(policyCondition);
                    }
                }
            );

            // Watch deletion of policy conditions and remove them from the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.deletePolicyConditionCtrl.result;
                },
                function () {
                    // Callback
                    var id = vm.deletePolicyConditionCtrl.result;
                    vm.policyConditionListCtrl.removeItem(id);
                }
            );
        }
    ]);
