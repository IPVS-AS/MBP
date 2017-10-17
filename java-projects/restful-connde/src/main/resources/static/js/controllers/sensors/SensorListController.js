/* global app */

app.controller('SensorListController',
        ['$scope', '$controller', 'sensorList', 'addSensor',
            'deviceList', 'addDevice', 'deleteDevice', 'typeList',
            function ($scope, $controller, sensorList, addSensor,
                    deviceList, addDevice, deleteDevice, typeList) {
                var vm = this;

                // public
                $scope.detailsLink = function (sensor) {
                    if (sensor.id) {
                        return "view/sensors/" + sensor.id;
                    }
                    return "#";
                };

                // expose variables

                angular.extend(vm, {
                    registeringDevice: false
                });

                // expose controller ($controller will auto-add to $scope)
                angular.extend(vm, {
                    sensorListCtrl: $controller('ItemListController as sensorListCtrl',
                            {
                                $scope: $scope,
                                list: sensorList
                            }),
                    addSensorCtrl: $controller('AddItemController as addSensorCtrl',
                            {
                                $scope: $scope,
                                addItem: addSensor
                            }),
                    deviceCtrl: $controller('DeviceListController as deviceCtrl',
                            {
                                $scope: $scope,
                                deviceList: deviceList,
                                addDevice: addDevice,
                                deleteDevice: deleteDevice
                            }),
                    typeListCtrl: $controller('ItemListController as typeListCtrl',
                            {
                                $scope: $scope,
                                list: typeList
                            })
                });

                // $watch 'addSensor' result and add to 'sensorList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.addSensorCtrl.result;
                        },
                        function () {
                            // callback
                            console.log('addSensorCtrl.result modified.');

                            var data = vm.addSensorCtrl.result;
                            if (data) {
                                vm.sensorListCtrl.pushItem(vm.addSensorCtrl.result);
                            }
                        }
                );

                // $watch 'addDevice' result and select on sensor form
                $scope.$watch(
                        function () {
                            // value being watched
                            return $scope.addDeviceCtrl.result;
                        },
                        function () {
                            // callback
                            console.log('addDeviceCtrl.result modified.');

                            var data = $scope.addDeviceCtrl.result;
                            if (data) {
                                $scope.addSensorCtrl.item.device = data._links.self.href;
                                vm.registeringDevice = false;
                            }
                        }
                );
            }
        ]);