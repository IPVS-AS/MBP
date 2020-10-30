/* global app */

app.controller('AdapterListController',
    ['$scope', '$controller', '$q', 'adapterList', 'adapterPreprocessing', 'addAdapter', 'deleteAdapter', 'FileReader', 'parameterTypesList', 'AdapterService', 'NotificationService',
        function ($scope, $controller, $q, adapterList, adapterPreprocessing, addAdapter, deleteAdapter, FileReader, parameterTypesList, AdapterService, NotificationService) {
            var vm = this;

            vm.dzServiceOptions = {
                paramName: 'serviceFile',
                maxFilesize: '100',
                maxFiles: 1
            };

            vm.dzServiceCallbacks = {
                'addedfile': function (file) {
                    vm.addAdapterCtrl.item.serviceFile = file;
                }
            };
            vm.dzRoutinesOptions = {
                paramName: 'routinesFile',
                addRemoveLinks: true,
                previewTemplate: document.querySelector('#tpl').innerHTML,
                createImageThumbnails: false,
                maxFilesize: '100',
                maxFiles: 99
            };

            vm.dzRoutinesCallbacks = {
                'addedfile': function (file) {
                    if (!vm.addAdapterCtrl.item.routineFiles) {
                        vm.addAdapterCtrl.item.routineFiles = [];
                    }
                    vm.addAdapterCtrl.item.routineFiles.push(file);
                },
                'removedfile': function (file) {
                    vm.addAdapterCtrl.item.routineFiles.splice(vm.addAdapterCtrl.item.routineFiles.indexOf(file), 1);
                },

            };

            vm.dzMethods = {};

            /**
             * The device code parameter is necessary for every adapter.
             * @type {{unit: string, name: string, type: string, mandatory: boolean}}
             */
            var deviceCodeParameter = {
                name: "device_code",
                type: "Text",
                unit: "",
                mandatory: true
            };
            vm.parameters = [deviceCodeParameter];
            vm.parameterTypes = parameterTypesList;

            /**
             * Initializing function, sets up basic things.
             */
            (function initController() {
                //Validity check for parameter types
                if (parameterTypesList.length < 1) {
                    NotificationService.notify("Could not load parameter types.", "error");
                }

                //Modify each adapter according to the preprocessing function (if provided)
                if (adapterPreprocessing) {
                    for (var i = 0; i < adapterList.length; i++) {
                        adapterPreprocessing(adapterList[i]);
                    }
                }
            })();

            //public
            function addParameter() {
                var parameter = {
                    name: "",
                    type: "",
                    unit: "",
                    mandatory: false
                };
                vm.parameters.push(parameter);
            }

            //public
            function deleteParameter(index) {
                vm.parameters.splice(index, 1);
            }

            /**
             * Reads given files from the user's disk and returns a promise
             * containing the combined promises for all asynchronous 
             * file read operations.
             *
             * @param files The files to read
             * @returns {*} The combined result promises
             */
            function readFiles(files) {
                //Sanity check
                if ((files === undefined) || files.constructor !== Array) {
                    return $q.all([]);
                }

                return FileReader.readMultipleAsDataURL(files, $scope);
            }

            /**
             * [Public]
             * Shows an alert that asks the user if he is sure that he wants to delete a certain adapter. It also
             * shows a list of all components that are affected by this deletion.
             *
             * @param data A data object that contains the id of the adapter that is supposed to be deleted
             * @returns A promise of the user's decision
             */
            function confirmDelete(data) {
                var adapterId = data.id;
                var adapterName = "";

                //Determines the adapter's name by checking all adapters in the adapter list
                for (var i = 0; i < adapterList.length; i++) {
                    if (adapterId === adapterList[i].id) {
                        adapterName = adapterList[i].name;
                        break;
                    }
                }

                //Ask the server for all components that use this adapter
                return AdapterService.getUsingComponents(data.id).then(function (result) {
                    var affectedWarning = "";

                    //If the list is not empty, create a message that contains the names of all affected components
                    if (result.length > 0) {

                        affectedWarning = "<br/><br/>The following components are currently " +
                            "using this operator and <strong>will be deleted as well</strong>:<br/>";

                        //Iterate over all affected components
                        for (var i = 0; i < result.length; i++) {
                            affectedWarning += "- ";
                            affectedWarning += result[i].name;
                            affectedWarning += " (" + result[i].component + ")";
                            affectedWarning += "<br/>";
                        }
                    }

                    //Show the alert to the user and return the resulting promise
                    return Swal.fire({
                        title: 'Delete operator',
                        type: 'warning',
                        html: "Are you sure you want to delete the operator <strong>" +
                            adapterName + "</strong>?" + affectedWarning,
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        confirmButtonClass: 'bg-red',
                        focusConfirm: false,
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                addParameter: addParameter,
                deleteParameter: deleteParameter,
                adapterListCtrl: $controller('ItemListController as adapterListCtrl',
                    {
                        $scope: $scope,
                        list: adapterList
                    }),
                addAdapterCtrl: $controller('AddItemController as addAdapterCtrl',
                    {
                        $scope: $scope,
                        addItem: function (data) {
                            //Extend request parameters for routines, their hashes and parameters
                            return readFiles(data.routineFiles)
                                .then(function (response) {
                                    data.unit = data.unit || "";
                                    data.routines = response;
                                    data.parameters = vm.parameters;
                                    return addAdapter(data);
                                }, function (response) {
                                    return $q.reject(response);
                                });
                        }
                    }),
                deleteAdapterCtrl: $controller('DeleteItemController as deleteAdapterCtrl',
                    {
                        $scope: $scope,
                        deleteItem: deleteAdapter,
                        confirmDeletion: confirmDelete
                    }),
            });

            // $watch 'addItem' result and add to 'itemList'
            $scope.$watch(
                function () {
                    //Value being watched
                    return vm.addAdapterCtrl.result;
                },
                function () {
                    //Callback
                    var data = vm.addAdapterCtrl.result;
                    if (data) {
                        //Close modal on success
                        $("#addAdapterModal").modal('toggle');

                        //Call pre processing function
                        if (adapterPreprocessing) {
                            adapterPreprocessing(data);
                        }

                        //Add new item to list
                        vm.adapterListCtrl.pushItem(data);

                        //Clear parameter array
                        vm.parameters.length = 0;
                    }
                }
            );

            // $watch 'deleteItem' result and remove from 'itemList'
            $scope.$watch(
                function () {
                    // value being watched
                    return vm.deleteAdapterCtrl.result;
                },
                function () {
                    var id = vm.deleteAdapterCtrl.result;
                    vm.adapterListCtrl.removeItem(id);
                }
            );

            //Enable popovers
            $(document).ready(function () {
                $('[data-toggle="popover"]').popover();
            });
        }
    ]
);
