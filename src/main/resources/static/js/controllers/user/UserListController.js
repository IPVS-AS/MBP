/* global app */

/**
 * Controller for the user management page.
 */
app.controller('UserListController',
    ['$scope', '$controller', 'userList', 'deleteUser', 'UserService', 'NotificationService',
        function ($scope, $controller, userList, deleteUser, UserService, NotificationService) {

            let vm = this;

            //Set object for password change
            vm.newPassword = {
                password: '',
                userId: ''
            };


            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
            })();

            /**
             * [Public]
             *
             * Performs a server request in order to promote a user, given by its ID, to an administrator.
             *
             * @param userId The ID of the user to promote
             */
            function promoteUser(userId) {
                UserService.promoteUser(userId).then(function (response) {
                    // Find the affected user in the list and update it
                    for (let i = 0; i < userList.length; i++) {
                        if (userId === userList[i].id) {
                            userList[i].isAdmin = true;
                            break;
                        }
                    }

                    //Notify user
                    NotificationService.notify("The user was updated successfully.", "success")
                });
            }

            /**
             * [Public]
             *
             * Performs a server request in order to degrade a user, given by its ID, to an ordinary user without
             * admin privileges.
             *
             * @param userId The ID of the user to degrade
             */
            function degradeUser(userId) {
                UserService.degradeUser(userId).then(function (response) {
                    // Find the affected user in the list and update it
                    for (let i = 0; i < userList.length; i++) {
                        if (userId === userList[i].id) {
                            userList[i].isAdmin = false;
                            break;
                        }
                    }
                    //Notify user
                    NotificationService.notify("The user was updated successfully.", "success")
                });
            }

            /**
             * [Public]
             *
             * Shows a modal dialog in which a new password for a user can be entered.
             */
            function showPasswordModal() {
                //Show modal for password change
                $("#changePasswordModal").modal('show');
            }

            /**
             * [Public]
             *
             * Performs a server request in order to change the password of the current set user.
             */
            function changePassword() {
                //Perform server request
                UserService.changeUserPassword(vm.newPassword.userId, vm.newPassword.password).then(function () {
                    //Notify user
                    NotificationService.notify("The user was updated successfully.", "success")

                    //Hide modal for password change
                    $("#changePasswordModal").modal('hide');
                });
            }


            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain user.
             *
             * @param data A data object that contains the id of the user  that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let userId = data.id;
                let userName = "";

                //Determines the user's name by checking all users in the list
                for (let i = 0; i < userList.length; i++) {
                    if (userId === userList[i].id) {
                        userName = userList[i].username;
                        break;
                    }
                }

                //Show confirm prompt to the user and return the resulting promise
                return Swal.fire({
                    title: 'Delete user',
                    icon: 'warning',
                    html: "Are you sure you want to delete the user \"<strong>" + userName + "</strong>\"? All the corresponding data owned by the user will also be deleted, including models, devices, operators, rules, policies, etc.",
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
                    }),
                promoteUser: promoteUser,
                degradeUser: degradeUser,
                showPasswordModal: showPasswordModal,
                changePassword: changePassword
            });

            // Watch delete controller and remove users from list
            $scope.$watch(
                function () {
                    // Watch the delete controller
                    return vm.deleteUserCtrl.result;
                },
                function () {
                    //Get ID of the affected user
                    let id = vm.deleteUserCtrl.result;

                    //Remove user from list
                    vm.userListCtrl.removeItem(id);
                }
            );
        }
    ]);