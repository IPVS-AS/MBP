/* global app */

app.controller('SensorListController',
        ['$scope', '$controller', 'sensorList', 'addSensor',
            'deviceList', 'addDevice', 'typeList',
            function ($scope, $controller, sensorList, addSensor,
                    deviceList, addDevice, typeList) {
                var vm = this;
                console.log('SensorListController init');
                
                // public
                $scope.detailsLink = function (sensor) {
                    if(sensor.id) {
                        return "view/sensors/" + sensor.id;
                    }
                    return "#";
                };

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
                                addDevice: addDevice
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
                                vm.sensorListCtrl.push(vm.addSensorCtrl.result);
                            }
                        }
                );

                // $watch 'addSensor' result and add to 'sensorList'
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
                            }
                        }
                );
            }
        ]);