/**
 * Controller for the dynamic peripherals list page.
 */
app.controller('DynamicPeripheralListController',
    ['$scope', '$controller', 'dynamicPeripheralList', 'addDynamicPeripheral', 'deleteDynamicPeripheral', 'operatorList', 'deviceTemplateList', 'requestTopicList', 'DiscoveryService', 'NotificationService',
        function ($scope, $controller, dynamicPeripheralList, addDynamicPeripheral, deleteDynamicPeripheral, operatorList, deviceTemplateList, requestTopicList, DiscoveryService, NotificationService) {

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
             * Shows an alert that asks the user if he is sure that he wants to delete a certain dynamic peripheral.
             *
             * @param data A data object that contains the id of the dynamic peripheral that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let dynamicPeripheralId = data.id;
                let name = "";

                //Determines the name of the dynamic peripheral by checking the list
                for (let i = 0; i < dynamicPeripheralList.length; i++) {
                    if (dynamicPeripheralId === dynamicPeripheralList[i].id) {
                        name = dynamicPeripheralList[i].name;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete dynamic peripheral',
                    icon: 'warning',
                    html: "Are you sure you want to delete the dynamic peripheral with name \"<strong>" + name + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                dynamicPeripheralListCtrl: $controller('ItemListController as dynamicPeripheralListCtrl', {
                    $scope: $scope,
                    list: dynamicPeripheralList
                }),
                addDynamicPeripheralCtrl: $controller('AddItemController as addDynamicPeripheralCtrl', {
                    $scope: $scope,
                    entity: 'dynamic peripheral',
                    addItem: addDynamicPeripheral
                }),
                deleteDynamicPeripheralCtrl: $controller('DeleteItemController as deleteDynamicPeripheralCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDynamicPeripheral,
                    confirmDeletion: confirmDelete
                }),
                operatorList: operatorList,
                deviceTemplateList: deviceTemplateList,
                requestTopicList: requestTopicList
            });

            //Watch controller result of dynamic peripheral additions
            $scope.$watch(() => vm.addDynamicPeripheralCtrl.result, (data) => {
                    //Sanity check
                    if (!data) return;

                    //Add dynamic peripheral to list
                    vm.dynamicPeripheralListCtrl.pushItem(data);

                    //Close modal on success
                    $("#addDynamicPeripheralModal").modal('toggle');
                }
            );

            //Watch controller result of dynamic peripheral deletions
            $scope.$watch(() => vm.deleteDynamicPeripheralCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, remove dynamic peripheral from list
                vm.dynamicPeripheralListCtrl.removeItem(vm.deleteDynamicPeripheralCtrl.result);
            });
        }
    ]);