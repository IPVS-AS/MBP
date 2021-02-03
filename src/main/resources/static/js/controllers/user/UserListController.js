/* global app */

/**
 * Controller for the user management page.
 */
app.controller('UserListController',
    ['$scope', '$controller', 'userList', 'deleteUser', 'UserService', 'NotificationService',
        function ($scope, $controller, userList, deleteUser, UserService, NotificationService) {

            let vm = this;


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                console.log(userList);
            })();


            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain user.
             *
             * @param data A data object that contains the id of the user  that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let userID = data.id;
                let userName = "";

                //Determines the key pair's name by checking all key pairs in the list
                for (let i = 0; i < userList.length; i++) {
                    if (userID === userList[i].id) {
                        userName = userList[i].name;
                        break;
                    }
                }

                //Show confirm prompt to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete user',
                    icon: 'warning',
                    html: "Are you sure you want to delete the user \"<strong>" + userName + "</strong>\"?",
                    showCancelButton: true,
                    confirmButtonText: 'Delete',
                    confirmButtonClass: 'bg-red',
                    focusConfirm: false,
                    cancelButtonText: 'Cancel'
                });
            }


            //Expose controllers
            angular.extend(vm, {
                userListCtrl: $controller('ItemListController as userListCtrl',
                    {
                        $scope: $scope,
                        list: userList
                    }),
                deleteUserCtrl: $controller('DeleteItemController as deleteUserCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteUser,
                        confirmDeletion: confirmDelete
                    })
            });
        }
    ]);