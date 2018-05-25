/* global app */

app.controller('DeviceListController',
        ['$scope', '$controller', 'DeviceService', 'deviceList', 'addDevice', 'deleteDevice',
            function ($scope, $controller, DeviceService, deviceList, addDevice, deleteDevice) {
                var vm = this;

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
                            }),
                     deleteDeviceCtrl: $controller('DeleteItemController as deleteDeviceCtrl',
                            {
                                $scope: $scope,
                                deleteItem: function (data) {
                                    // get ID here
                                    return deleteDevice(data);
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
                
                // $watch 'deleteItem' result and remove from 'itemList'
                $scope.$watch(
                        function () {
                            // value being watched
                            return vm.deleteDeviceCtrl.result;
                        },
                        function() {
                          var id = vm.deleteDeviceCtrl.result;
                          
                          vm.deviceListCtrl.removeItem(id);
                        }
                );
            }
        ]);