/* global app */

app.controller('DeviceListController',
    ['$scope', '$controller', 'DeviceService', 'deviceList', 'addDevice', 'deleteDevice', 'ComponentTypeService',
        function ($scope, $controller, DeviceService, deviceList, addDevice, deleteDevice, ComponentTypeService) {
            var vm = this;

            (function initController() {
                loadDeviceTypes();
            })();

            for (var i in deviceList) {
                deviceList[i].formattedMacAddress =
                    DeviceService.formatMacAddress(deviceList[i].macAddress);
            }

            function confirmDelete(data) {
                var deviceId = data.id;
                var deviceName = "";

                for(var i = 0; i < deviceList.length; i++){
                    if(deviceId == deviceList[i].id){
                        deviceName = deviceList[i].name;
                        break;
                    }
                }

                return DeviceService.getUsingComponents(data.id).then(function(result) {
                    var affectedWarning = "";

                    if (result.success && (result.data.length > 0)) {
                        affectedWarning = "<br/><br/><strong>Beware</strong>: " +
                            "The following components are currently using this device" +
                            " and will be deleted as well:<br/>";

                        for(var i = 0; i < result.data.length; i++){
                            affectedWarning += "- ";
                            affectedWarning += result.data[i].name;
                            affectedWarning += " (" + result.data[i].component + ")";
                            affectedWarning += "<br/>";
                        }
                    }

                    return Swal.fire({
                        title: 'Delete device',
                        type: 'warning',
                        html: "Are you sure you want to delete device \"" +
                        deviceName + "\"?" + affectedWarning,
                        showCancelButton: true,
                        confirmButtonText: 'Delete',
                        cancelButtonText: 'Cancel'
                    });
                });
            }

            // expose controller ($controller will auto-add to $scope)
            angular.extend(vm, {
                deviceListCtrl: $controller('ItemListController as deviceListCtrl', {
                    $scope: $scope,
                    list: deviceList
                }),
                addDeviceCtrl: $controller('AddItemController as addDeviceCtrl', {
                    $scope: $scope,
                    addItem: function (data) {
                        var deviceObject = {};

                        for (var property in data) {
                            if (data.hasOwnProperty(property)) {
                                deviceObject[property] = data[property];
                            }
                        }
                        delete deviceObject.formattedMacAddress;
                        deviceObject.macAddress = DeviceService.normalizeMacAddress(data.formattedMacAddress);

                        return addDevice(deviceObject);
                    }
                }),
                deleteDeviceCtrl: $controller('DeleteItemController as deleteDeviceCtrl', {
                    $scope: $scope,
                    deleteItem: deleteDevice,
                    confirmDeletion: confirmDelete
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
                function () {
                    var id = vm.deleteDeviceCtrl.result;

                    vm.deviceListCtrl.removeItem(id);
                }
            );

            function loadDeviceTypes() {
                ComponentTypeService.GetByComponent('DEVICE')
                    .then(function (response) {
                        if (response.success) {
                            vm.deviceTypes = response.data;
                        } else {
                            console.log("Error loading device types!");
                        }
                    });
            };

        }
    ]);