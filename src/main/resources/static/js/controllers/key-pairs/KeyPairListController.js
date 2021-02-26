/* global app */

app.controller('KeyPairListController',
    ['$scope', '$controller', '$q', 'keyPairList', 'addKeyPair', 'deleteKeyPair', 'KeyPairService', 'NotificationService',
        function ($scope, $controller, $q, keyPairList, addKeyPair, deleteKeyPair, KeyPairService, NotificationService) {
            const SHOW_PUBLIC_KEY_MODAL = $('#showPublicKeyModal');

            let vm = this;

            vm.generation = {
                name: "",
                error: false
            };

            vm.publicKeyDisplay = "";
            vm.copiedPublicKey = false;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {

            })();

            /**
             * [Public]
             * Allows to download the public key of a key pair with a certain ID.
             * @param keyPairId the ID of the key pair
             */
            function downloadPublicKey(keyPairId) {
                //Search for key pair with matching ID
                let keyPair = null;
                for (let i = 0; i < keyPairList.length; i++) {
                    //Check for matching ID
                    if (keyPairList[i].id === keyPairId) {
                        keyPair = keyPairList[i];
                        break;
                    }
                }

                //Check if key pair could be found
                if (keyPair == null) {
                    return;
                }

                //Prepare download file as blob
                let filename = keyPair.name + ".pubk";
                let blob = new Blob([keyPair.publicKey], {
                    type: "text/plain;charset=utf-8"
                });

                //Initiate download
                saveAs(blob, filename);
            }

            /**
             * [Public]
             * Displays the public key of a key pair with a certain ID in a modal dialog.
             * @param keyPairId the ID of the key pair
             */
            function showPublicKey(keyPairId) {
                //Unset copy flag
                vm.copiedPublicKey = false;

                //Search for key pair with matching ID
                let keyPair = null;
                for (let i = 0; i < keyPairList.length; i++) {
                    //Check for matching ID
                    if (keyPairList[i].id === keyPairId) {
                        keyPair = keyPairList[i];
                        break;
                    }
                }

                //Check if key pair could be found
                if (keyPair == null) {
                    return;
                }

                //Set public key to display
                vm.publicKeyDisplay = keyPair.publicKey;

                //Show modal
                SHOW_PUBLIC_KEY_MODAL.modal('show');
            }

            /**
             * [Public]
             * Copies the currently displayed public key to the clipboard.
             */
            function copyPublicKeyToClipboard() {
                //Find textarea of the modal and select it
                let textArea = SHOW_PUBLIC_KEY_MODAL.find('textarea');
                textArea.select();

                //Copy content of the textarea to clipboard
                document.execCommand('copy');

                //Set copy flag
                vm.copiedPublicKey = true;
            }

            /**
             * [Public]
             * Called when the user submitted the form form generating a key pair.
             */
            function generateKeyPair() {
                KeyPairService.generate(vm.generation.name).then(function (response) {
                    //Sanity check
                    if (!response) {
                        return;
                    }
                    vm.keyPairListCtrl.pushItem(response);
                    vm.generation.error = false;

                    NotificationService.notify("Successfully generated a new key.", "success");

                    $("#generateKeyPairModal").modal('hide');
                }, function (response) {
                    if (response.status === 400) {
                        vm.generation.error = "Invalid name or a key pair with this name already exists.";
                    }
                    //Failure
                    NotificationService.notify("Could not generate a new key.", "error");
                });
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain key pair. It also
             * shows a list of all devices that are affected by this deletion.
             *
             * @param data A data object that contains the id of the key pair that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                let keyPairID = data.id;
                let keyPairName = "";

                //Determines the key pair's name by checking all key pairs in the list
                for (let i = 0; i < keyPairList.length; i++) {
                    if (keyPairID === keyPairList[i].id) {
                        keyPairName = keyPairList[i].name;
                        break;
                    }
                }

                //Ask the server for all devices that use this key pair
                return KeyPairService.getUsingDevices(keyPairID).then(function (result) {
                    //Check if list is empty
                    if (result.length > 0) {
                        //Not empty, entity cannot be deleted
                        let errorText = "The key pair <strong>" + keyPairName + "</strong> is still used by the " +
                            "following devices and thus cannot be deleted:<br/><br/>";

                        //Iterate over all affected entities
                        for (let i = 0; i < result.length; i++) {
                            errorText += "- " + result[i].name + "<br/>";
                        }

                        // Show error message
                        Swal.fire({
                            icon: 'error',
                            title: 'Deletion impossible',
                            html: errorText
                        })

                        // Return new promise as result
                        return Promise.resolve({value: false});
                    }

                    //Show confirm prompt to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete key pair',
                        icon: 'warning',
                        html: "Are you sure you want to delete the key pair \"<strong>" + keyPairName + "</strong>\"?",
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            //Expose controllers
            angular.extend(vm, {
                keyPairListCtrl: $controller('ItemListController as keyPairListCtrl',
                    {
                        $scope: $scope,
                        list: keyPairList
                    }),
                addKeyPairCtrl: $controller('AddItemController as addKeyPairCtrl',
                    {
                        $scope: $scope,
                        entity: 'key pair',
                        addItem: addKeyPair
                    }),
                deleteKeyPairCtrl: $controller('DeleteItemController as deleteKeyPairCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteKeyPair,
                        confirmDeletion: confirmDelete
                    }),
                generateKeyPair: generateKeyPair,
                showPublicKey: showPublicKey,
                copyPublicKeyToClipboard: copyPublicKeyToClipboard,
                downloadPublicKey: downloadPublicKey
            });

            // Watch addition of new items
            $scope.$watch(() => vm.addKeyPairCtrl.result,
                function () {
                    //Callback
                    let data = vm.addKeyPairCtrl.result;

                    //Sanity check
                    if (!data) {
                        return;
                    }

                    //Close modal on success
                    $("#addKeyPairModal").modal('toggle');

                    //Add new item to list
                    vm.keyPairListCtrl.pushItem(data);
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteKeyPairCtrl.result;
                },
                function () {
                    var id = vm.deleteKeyPairCtrl.result;
                    vm.keyPairListCtrl.removeItem(id);
                }
            );
        }
    ]
);
