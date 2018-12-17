/* global app */

app.controller('AdapterListController',
        ['$scope', '$controller', '$q', 'adapterList', 'addAdapter', 'deleteAdapter', 'FileReader',
            function ($scope, $controller, $q, adapterList, addAdapter, deleteAdapter, FileReader) {
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

                // private
                function readService(service) {
                    if (service) {
                        return FileReader.readAsText(service, $scope);
                    } else {
                        // reject
                        return '';
                    	//return $q.reject('Service file must not be empty.');
                    }
                }

                function readRoutines(routines) {
                    if ((routines !== undefined) && (routines.constructor === Array)) {
                        //Read routines files
                        return FileReader.readMultipleAsDataURL(routines, $scope);
                    } else {
                        //Return empty promise (no routine files)
                        return $q.all([]);
                    }
                }

                // expose controller ($controller will auto-add to $scope)
                angular.extend(vm, {
                    adapterListCtrl: $controller('ItemListController as adapterListCtrl',
                            {
                                $scope: $scope,
                                list: adapterList
                            }),
                    addAdapterCtrl: $controller('AddItemController as addAdapterCtrl',
                            {
                                $scope: $scope,
                                addItem: function (data) {

                                    return readRoutines(data.routineFiles)
                                    .then(function (response) {
                                        console.log('readRoutines: ', response);
                                        data.routines = response;
                                        return addAdapter(data);
                                    }, function (response) {
                                        return $q.reject(response);
                                    });

//                                    return readService(data.serviceFile).then(
//                                            function (response) {
//                                                console.log('readService: ', response);
//                                                data.service = response;
//                                                return readRoutines(data.routineFiles)
//                                                        .then(function (response) {
//                                                            console.log('readRoutines: ', response);
//                                                            data.routines = response;
//                                                            return addAdapter(data);
//                                                        }, function (response) {
//                                                            return $q.reject(response);
//                                                        });
//                                            }, function (response) {
//                                        return $q.reject(response);
//                                    });
                                }
                            }),
                    deleteAdapterCtrl: $controller('DeleteItemController as deleteAdapterCtrl',
                            {
                                $scope: $scope,
                                deleteItem: deleteAdapter
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
            }]);