/* global app */

app.controller('DeviceListController',
        ['$scope', '$controller', 'DeviceService', 'deviceList', 'addDevice',
            function ($scope, $controller, DeviceService, deviceList, addDevice) {
                var vm = this;
                console.log('DeviceListController init');

                for (var i in deviceList) {
                    deviceList[i].formattedMacAddress =
                            DeviceService.formatMacAddress(deviceList[i].macAddress);
                }

                // expose controller ($controller will auto-add to $scope)
                angular.extend(vm, {
                    deviceListCtrl: $controller('ItemListController as deviceListCtrl',
                            {
                                $scope: $scope,
                                list: deviceList
                            }),
                    addDeviceCtrl: $controller('AddItemController as addDeviceCtrl',
                            {
                                $scope: $scope,
                                addItem: function (data) {
                                    data.macAddress = DeviceService.normalizeMacAddress(data.formattedMacAddress);
                                    return addDevice(data);
                                }
                            })
                });

                // $watch 'addItem' result and add to 'itemList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.addDeviceCtrl.result;
                        },
                        function () {
                            // callback
                            console.log('addDeviceCtrl.result modified.');

                            var data = vm.addDeviceCtrl.result;
                            if (data) {
                                data.formattedMacAddress = DeviceService.formatMacAddress(data.macAddress);
                                vm.deviceListCtrl.pushItem(data);
                            }
                        }
                );
            }
        ]);