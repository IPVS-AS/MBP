/* global app */

app.controller('TypeListController',
        ['$scope', '$controller', '$q', 'typeList', 'addType', 'deleteType', 'FileReader',
            function ($scope, $controller, $q, typeList, addType, deleteType, FileReader) {
                var vm = this;

                vm.dzServiceOptions = {
                    paramName: 'serviceFile',
                    maxFilesize: '100',
                    acceptedFiles: 'text/plain',
                    maxFiles: 1
                };

                vm.dzServiceCallbacks = {
                    'addedfile': function (file) {
                        console.log(file);
                        vm.addTypeCtrl.item.serviceFile = file;
                    }
                };

                vm.dzRoutinesOptions = {
                    paramName: 'routinesFile',
                    maxFilesize: '100',
                    acceptedFiles: 'text/plain',
                    maxFiles: 99
                };

                vm.dzRoutinesCallbacks = {
                    'addedfile': function (file) {
                        if (!vm.addTypeCtrl.item.routineFiles) {
                            vm.addTypeCtrl.item.routineFiles = [];
                        }
                        vm.addTypeCtrl.item.routineFiles.push(file);
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
                    if (routines || routines.constructor === Array) {
                        // read routines files in form
                        //var read = FileReader.readMultipleAsText(routines, $scope);
                        return FileReader.readMultipleAsDataURL(routines, $scope);
                    } else {
                        // reject
                    	return $q.reject('Routines files must not be empty.');
                    }
                }

                // expose controller ($controller will auto-add to $scope)
                angular.extend(vm, {
                    typeListCtrl: $controller('ItemListController as typeListCtrl',
                            {
                                $scope: $scope,
                                list: typeList
                            }),
                    addTypeCtrl: $controller('AddItemController as addTypeCtrl',
                            {
                                $scope: $scope,
                                addItem: function (data) {
                                	
                                    return readRoutines(data.routineFiles)
                                    .then(function (response) {
                                        console.log('readRoutines: ', response);
                                        data.routines = response;
                                        return addType(data);
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
//                                                            return addType(data);
//                                                        }, function (response) {
//                                                            return $q.reject(response);
//                                                        });
//                                            }, function (response) {
//                                        return $q.reject(response);
//                                    });
                                }
                            }),
                    deleteTypeCtrl: $controller('DeleteItemController as deleteTypeCtrl',
                            {
                                $scope: $scope,
                                deleteItem: deleteType
                            }),
                });

                // $watch 'addItem' result and add to 'itemList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.addTypeCtrl.result;
                        },
                        function () {
                            // callback
                            console.log('addTypeCtrl.result modified.');

                            var data = vm.addTypeCtrl.result;
                            if (data) {
                                console.log('pushItem.');
                                vm.typeListCtrl.pushItem(data);
                            }
                        }
                );

                // $watch 'deleteItem' result and remove from 'itemList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.deleteTypeCtrl.result;
                        },
                        function() {
                          var id = vm.deleteTypeCtrl.result;
                          
                          vm.typeListCtrl.removeItem(id);
                        }
                );
            }]);