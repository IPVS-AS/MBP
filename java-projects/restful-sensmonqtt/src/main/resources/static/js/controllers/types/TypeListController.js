/* global app */

app.controller('TypeListController',
        ['$scope', '$controller', '$q', 'typeList', 'addType', 'FileReader',
            function ($scope, $controller, $q, typeList, addType, FileReader) {
                var vm = this;
                // private
                function readService(service) {
                    if (service) {
                        return FileReader.readAsText(service, $scope);
                    } else {
                        // reject
                        return $q.reject('Service file must not be empty.');
                    }
                }

                function readRoutines(routines) {
                    if (routines || routines.constructor === Array) {
                        // read routines files in form
                        return FileReader.readMultipleAsText(routines, $scope);
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
                                    return readService(data.service).then(
                                            function (response) {
                                                console.log('readService: ', response);
                                                data.service = response;
                                                return readRoutines(data.routines)
                                                        .then(function (response) {
                                                            console.log('readRoutines: ', response);
                                                            data.routines = response;
                                                            return addType(data);
                                                        }, function (response) {
                                                            return $q.reject(response);
                                                        });
                                            }, function (response) {
                                        return $q.reject(response);
                                    });
                                }
                            })
                });
            }]);