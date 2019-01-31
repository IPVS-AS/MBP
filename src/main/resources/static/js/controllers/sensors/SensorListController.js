/* global app */

app.controller('SensorListController',
  ['$scope', '$controller', 'sensorList', 'addSensor', 'deleteSensor',
    'deviceList', 'addDevice', 'deleteDevice', 'adapterList', 'ComponentTypeService',
    function($scope, $controller, sensorList, addSensor, deleteSensor,
      deviceList, addDevice, deleteDevice, adapterList, ComponentTypeService) {
      var vm = this;

      (function initController() {
        loadSensorTypes();
      })();

      // public
      $scope.detailsLink = function(sensor) {
        if (sensor.id) {
          return "view/sensors/" + sensor.id;
        }
        return "#";
      };

      function confirmDelete(data) {
        var sensorId = data.id;
        var sensorName = "";

        for(var i = 0; i < sensorList.length; i++){
          if(sensorId == sensorList[i].id){
            sensorName = sensorList[i].name;
            break;
          }
        }

        return swal("Delete sensor",
            "Are you sure you want to delete sensor \"" + sensorName + "\"?", "warning",
            {
              buttons: ["Cancel", "Delete sensor"]
            });
      }

      // expose variables

      angular.extend(vm, {
        registeringDevice: false
      });

      // expose controller ($controller will auto-add to $scope)
      angular.extend(vm, {
        sensorListCtrl: $controller('ItemListController as sensorListCtrl', {
          $scope: $scope,
          list: sensorList
        }),
        addSensorCtrl: $controller('AddItemController as addSensorCtrl', {
          $scope: $scope,
          addItem: addSensor
        }),
        deleteSensorCtrl: $controller('DeleteItemController as deleteSensorCtrl', {
          $scope: $scope,
          deleteItem: deleteSensor,
          confirmDeletion: confirmDelete
        }),
        deviceCtrl: $controller('DeviceListController as deviceCtrl', {
          $scope: $scope,
          deviceList: deviceList,
          addDevice: addDevice,
          deleteDevice: deleteDevice
        }),
        adapterListCtrl: $controller('ItemListController as adapterListCtrl', {
          $scope: $scope,
          list: adapterList
        })
      });

      // $watch 'addSensor' result and add to 'sensorList'
      $scope.$watch(
        function() {
          // value being watched
          return vm.addSensorCtrl.result;
        },
        function() {
          // callback
          console.log('addSensorCtrl.result modified.');

          var data = vm.addSensorCtrl.result;
          if (data) {
            vm.sensorListCtrl.pushItem(vm.addSensorCtrl.result);
          }
        }
      );

      // $watch 'deleteItem' result and remove from 'itemList'
      $scope.$watch(
        function() {
          // value being watched
          return vm.deleteSensorCtrl.result;
        },
        function() {
          var id = vm.deleteSensorCtrl.result;

          vm.sensorListCtrl.removeItem(id);
        }
      );

      // $watch 'addDevice' result and select on sensor form
      $scope.$watch(
        function() {
          // value being watched
          return $scope.addDeviceCtrl.result;
        },
        function() {
          // callback
          console.log('addDeviceCtrl.result modified.');

          var data = $scope.addDeviceCtrl.result;
          if (data) {
            $scope.addSensorCtrl.item.device = data._links.self.href;
            vm.registeringDevice = false;
          }
        }
      );

      function loadSensorTypes() {
        ComponentTypeService.GetByComponent('SENSOR')
          .then(function(response) {
            if (response.success) {
              vm.sensorTypes = response.data;
            } else {
              console.log("Error loading sensor types!");
            }
          });
      };

    }
  ]);