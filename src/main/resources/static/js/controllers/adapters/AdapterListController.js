/* global app */

app.controller('AdapterListController',
        ['$scope', '$controller', '$q', 'adapterList', 'addAdapter', 'deleteAdapter', 'FileReader', 'ParameterTypeService', 'AdapterService',
            function ($scope, $controller, $q, adapterList, addAdapter, deleteAdapter, FileReader, ParameterTypeService, AdapterService) {
                var vm = this;

                vm.dzServiceOptions = {
                    paramName: 'serviceFile',
                    maxFilesize: '100',
                    maxFiles: 1
                };

                vm.dzServiceCallbacks = {
                    'addedfile': function (file) {
                        console.log(file);
                        vm.addAdapterCtrl.item.serviceFile = file;
                    }
                };

                vm.dzRoutinesOptions = {
                    paramName: 'routinesFile',
                    maxFilesize: '100',
                    maxFiles: 99
                };

                vm.dzRoutinesCallbacks = {
                    'addedfile': function (file) {
                        if (!vm.addAdapterCtrl.item.routineFiles) {
                            vm.addAdapterCtrl.item.routineFiles = [];
                        }
                        vm.addAdapterCtrl.item.routineFiles.push(file);
                    }
                };

                vm.dzMethods = {};

                vm.parameterTypes = [];

                vm.parameters = [];

                //public
                function addDeploymentParameter(){
                    var parameter = {
                        name: "",
                        type: "",
                        unit: "",
                        mandatory: false
                    };
                    vm.parameters.push(parameter);
                }

                //public
                function deleteDeploymentParameter(index){
                    vm.parameters.splice(index, 1);
                }

                //private
                function readRoutines(routines) {
                    if ((routines !== undefined) && (routines.constructor === Array)) {
                        //Read routines files
                        return FileReader.readMultipleAsDataURL(routines, $scope);
                    } else {
                        //Return empty promise (no routine files)
                        return $q.all([]);
                    }
                }

                //private
                function loadParameterTypes() {
                    ParameterTypeService.getAll().then(function(response) {
                        if (response.success) {
                            vm.parameterTypes = response.data;
                        } else {
                            console.log("Error while loading parameter types.");
                        }
                    });
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
                    for(var i = 0; i < adapterList.length; i++){
                        if(adapterId == adapterList[i].id){
                            adapterName = adapterList[i].name;
                            break;
                        }
                    }

                    //Ask the server for all components that use this adapter
                    return AdapterService.getUsingComponents(data.id).then(function(result) {
                        var affectedWarning = "";

                        //If the list is not empty, create a message that contains the names of all affected components
                        if (result.success && (result.data.length > 0)) {

                            affectedWarning = "<br/><br/><strong>The following components are currently " +
                                "using this adapter and will be deleted as well:</strong><br/>";

                            //Iterate over all affected components
                            for(var i = 0; i < result.data.length; i++){
                                affectedWarning += "- ";
                                affectedWarning += result.data[i].name;
                                affectedWarning += " (" + result.data[i].component + ")";
                                affectedWarning += "<br/>";
                            }
                        }

                        //Show the alert to the user and return the resulting promise
                        return Swal.fire({
                            title: 'Delete adapter',
                            type: 'warning',
                            html: "Are you sure you want to delete the adapter \"" +
                             adapterName + "\"?" + affectedWarning,
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
                    addDeploymentParameter: addDeploymentParameter,
                    deleteDeploymentParameter : deleteDeploymentParameter,
                    adapterListCtrl: $controller('ItemListController as adapterListCtrl',
                            {
                                $scope: $scope,
                                list: adapterList
                            }),
                    addAdapterCtrl: $controller('AddItemController as addAdapterCtrl',
                            {
                                $scope: $scope,
                                addItem: function (data) {
                                    //Extend request parameters for routines and deployment parameters
                                    return readRoutines(data.routineFiles)
                                    .then(function (response) {
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
                            // value being watched
                            return vm.addAdapterCtrl.result;
                        },
                        function () {
                            // callback
                            console.log('addAdapterCtrl.result modified.');

                            var data = vm.addAdapterCtrl.result;
                            if (data) {
                                console.log('pushItem.');
                                vm.adapterListCtrl.pushItem(data);
                            }
                        }
                );

                // $watch 'deleteItem' result and remove from 'itemList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.deleteAdapterCtrl.result;
                        },
                        function() {
                          var id = vm.deleteAdapterCtrl.result;
                          vm.adapterListCtrl.removeItem(id);
                        }
                );

                //Load parameter types for select
                loadParameterTypes();
            }]);