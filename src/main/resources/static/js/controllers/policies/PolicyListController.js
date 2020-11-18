/* global app */

/**
 * Controller for the policies list page.
 */
app.controller('PolicyListController',
    ['$scope', '$controller', '$interval', 'policyList', 'addPolicy', 'deletePolicy', 'policyConditionList', 'policyEffectList', 'PolicyService', 'NotificationService',
        function ($scope, $controller, $interval, policyList, addPolicy, deletePolicy, policyConditionList, policyEffectList, PolicyService, NotificationService) {

            var vm = this;

            // Expose condition and effect lists
            vm.policyConditionList = policyConditionList;
            vm.policyEffectList = policyEffectList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                // Check if the policy list was retrieved successfully
                if (policyList == null) {
                    NotificationService.notify("Could not retrieve policy list.", "error");
                }

                // Refresh policy action select picker when the modal is opened
                $('.modal').on('shown.bs.modal', function () {
                    $('.selectpicker').selectpicker('refresh');
                });
            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain policy.
             *
             * @param data A data object that contains the id of the policy that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var policyId = data.id;
                var policyName = "";

                // Determines the policy's name by checking the list
                for (var i = 0; i < policyList.length; i++) {
                    if (policyId === policyList[i].id) {
                        policyName = policyList[i].name;
                        break;
                    }
                }

                // Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete Policy',
                    type: 'warning',
                    html: "Are you sure you want to delete policy \"" + policyName + "\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            // Expose controllers
            angular.extend(vm, {
                policyListCtrl: $controller('ItemListController as policyListCtrl', {
                    $scope: $scope,
                    list: policyList
                }),
                addPolicyCtrl: $controller('AddItemController as addPolicyCtrl', {
                    $scope: $scope,
                    entity: 'policy',
                    addItem: addPolicy
                }),
                deletePolicyCtrl: $controller('DeleteItemController as deletePolicyCtrl', {
                    $scope: $scope,
                    deleteItem: deletePolicy,
                    confirmDeletion: confirmDelete
                })
            });

            // Watch addition of policies and add them to the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.addPolicyCtrl.result;
                },
                function () {
                    // Callback
                    var policy = vm.addPolicyCtrl.result;

                    // Make sure the result is valid
                    if (policy) {
                        // Close modal on success
                        $("#addPolicyModal").modal('toggle');

                        // Add policy to list
                        vm.policyListCtrl.pushItem(policy);
                    }
                }
            );

            // Watch deletion of policies and remove them from the list
            $scope.$watch(
                function () {
                    // Value being watched
                    return vm.deletePolicyCtrl.result;
                },
                function () {
                    // Callback
                    var id = vm.deletePolicyCtrl.result;
                    vm.policyListCtrl.removeItem(id);
                }
            );
        }
    ]);