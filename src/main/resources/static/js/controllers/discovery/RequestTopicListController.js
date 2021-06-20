/**
 * Controller for the request topic list page.
 */
app.controller('RequestTopicListController',
    ['$scope', '$controller', 'requestTopicList', 'addRequestTopic', 'deleteRequestTopic', 'NotificationService',
        function ($scope, $controller, requestTopicList, addRequestTopic, deleteRequestTopic, NotificationService) {

            let vm = this;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain request topic.
             *
             * @param data A data object that contains the id of the request topic that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let requestTopicId = data.id;
                let suffix = "";

                //Determines the topic's suffix by checking the list
                for (let i = 0; i < requestTopicList.length; i++) {
                    if (requestTopicId === requestTopicList[i].id) {
                        suffix = requestTopicList[i].suffix;
                        break;
                    }
                }

                //Show the alert to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete request topic',
                    icon: 'warning',
                    html: "Are you sure you want to delete the request topic with suffix \"<strong>" + suffix + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }

            //Expose controllers
            angular.extend(vm, {
                requestTopicListCtrl: $controller('ItemListController as requestTopicListCtrl', {
                    $scope: $scope,
                    list: requestTopicList
                }),
                addRequestTopicCtrl: $controller('AddItemController as addRequestTopicCtrl', {
                    $scope: $scope,
                    entity: 'request topic',
                    addItem: addRequestTopic
                }),
                deleteRequestTopicCtrl: $controller('DeleteItemController as deleteRequestTopicCtrl', {
                    $scope: $scope,
                    deleteItem: deleteRequestTopic,
                    confirmDeletion: confirmDelete
                })
            });

            //Watch controller result of request topic additions
            $scope.$watch(() => vm.addRequestTopicCtrl.result, (data) => {
                    //Sanity check
                    if (!data) return;

                    //Add request topic to list
                    vm.requestTopicListCtrl.pushItem(data);

                    //Close modal on success
                    $("#addRequestTopicModal").modal('toggle');
                }
            );

            //Watch controller result of request topic deletions
            $scope.$watch(() => vm.deleteRequestTopicCtrl.result, (data) => {
                //Sanity check
                if (!data) return;

                //Callback, remove request topic from list
                vm.requestTopicListCtrl.removeItem(vm.deleteRequestTopicCtrl.result);
            });
        }
    ]);