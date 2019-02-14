/* global app */

app.controller('ActuatorListController',
    ['$scope', '$controller', '$interval', 'actuatorList', 'addActuator', 'deleteActuator',
      'deviceList', 'addDevice', 'deleteDevice', 'adapterList', 'ComponentService',
      'ComponentTypeService', 'NotificationService',
      function ($scope, $controller, $interval, actuatorList, addActuator, deleteActuator,
                deviceList, addDevice, deleteDevice, adapterList, ComponentService,
                ComponentTypeService, NotificationService) {
        var vm = this;

        (function initController() {
          loadActuatorTypes();
          $interval(function () {
            loadActuatorStates();
          }, 5 * 60 * 1000);
          loadActuatorStates();
        })();

        //Extend each actuator in actuatorList for a state and a reload function
        for (var i in actuatorList) {
          actuatorList[i].state = 'LOADING';
          actuatorList[i].reloadState = createReloadStateFunction(i);
        }

        /**
         * [Public]
         * @param actuator
         * @returns {*}
         */
        $scope.detailsLink = function (actuator) {
          if (actuator.id) {
            return "view/actuators/" + actuator.id;
          }
          return "#";
        };

        /**
         * [Public]
         * Shows an alert that asks the user if he is sure that he wants to delete a certain actuator.
         *
         * @param data A data object that contains the id of the actuator that is supposed to be deleted
         * @returns A promise of the user's decision
         */
        function confirmDelete(data) {
          var actuatorId = data.id;
          var actuatorName = "";

          //Determines the actuator's name by checking all actuators in the actuator list
          for (var i = 0; i < actuatorList.length; i++) {
            if (actuatorId == actuatorList[i].id) {
              actuatorName = actuatorList[i].name;
              break;
            }
          }

          //Show the alert to the user and return the resulting promise
          return Swal.fire({
            title: 'Delete actuator',
            type: 'warning',
            html: "Are you sure you want to delete actuator \"" + actuatorName + "\"?",
            showCancelButton: true,
            confirmButtonText: 'Delete',
            confirmButtonClass: 'bg-red',
            focusConfirm: false,
            cancelButtonText: 'Cancel'
          });
        }

        /**
         * [Private]
         * Returns a function that retrieves the state for a actuator at a certain index in actuatorList.
         * @param index The index of the actuator in the actuatorList
         * @returns {Function}
         */
        function createReloadStateFunction(index) {
          //Create function and return it
          return function () {
            getActuatorState(index);
          };
        }

        /**
         * [Private]
         * Sends a server request in order to retrieve the deployment state of a actuator at a certain index
         * in actuatorList. The state is then stored in the corresponding actuator object in actuatorList.
         *
         * @param index The index of the actuator whose state is supposed to be retrieved in actuatorList
         */
        function getActuatorState(index) {
          //Enable spinner
          actuatorList[index].state = 'LOADING';

          //Perform server request and set state of the actuator object accordingly
          ComponentService.getComponentState(actuatorList[index].id, 'actuators').then(function (response) {
            actuatorList[index].state = response.data;
          }, function (response) {
            actuatorList[index].state = 'UNKNOWN';
            NotificationService.notify("Could not retrieve the actuator state.", "error");
          });
        }

        /**
         * [Private]
         * Sends a server request in order to retrieve the deployment states of all registered actuators.
         * The states are then stored in the corresponding actuator objects in actuatorList.
         */
        function loadActuatorStates() {//Perform server request

          ComponentService.getAllComponentStates('actuators').then(function (response) {
            var statesMap = response.data;

            //Iterate over all actuators in actuatorList and update the states of all actuators accordingly
            for (var i in actuatorList) {
              var actuatorId = actuatorList[i].id;
              actuatorList[i].state = statesMap[actuatorId];
            }
          }, function (response) {
            for (var i in actuatorList) {
              actuatorList[i].state = 'UNKNOWN';
            }
            NotificationService.notify("Could not retrieve actuator states.", "error");
          });
        }

        //Expose
        angular.extend(vm, {
          registeringDevice: false
        });

        // expose controller ($controller will auto-add to $scope)
        angular.extend(vm, {
          actuatorListCtrl: $controller('ItemListController as actuatorListCtrl', {
            $scope: $scope,
            list: actuatorList
          }),
          addActuatorCtrl: $controller('AddItemController as addActuatorCtrl', {
            $scope: $scope,
            addItem: addActuator
          }),
          deleteActuatorCtrl: $controller('DeleteItemController as deleteActuatorCtrl', {
            $scope: $scope,
            deleteItem: deleteActuator,
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

        // $watch 'addActuator' result and add to 'actuatorList'
        $scope.$watch(
            function () {
              // value being watched
              return vm.addActuatorCtrl.result;
            },
            function () {
              // callback
              console.log('addActuatorCtrl.result modified.');

              var data = vm.addActuatorCtrl.result;
              if (data) {
                vm.actuatorListCtrl.pushItem(vm.addActuatorCtrl.result);
              }
            }
        );

        // $watch 'deleteItem' result and remove from 'itemList'
        $scope.$watch(
            function () {
              // value being watched
              return vm.deleteActuatorCtrl.result;
            },
            function () {
              var id = vm.deleteActuatorCtrl.result;

              vm.actuatorListCtrl.removeItem(id);
            }
        );

        // $watch 'addDevice' result and select on actuator form
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
                $scope.addActuatorCtrl.item.device = data._links.self.href;
                vm.registeringDevice = false;
              }
            }
        );

        function loadActuatorTypes() {
          ComponentTypeService.GetByComponent('ACTUATOR')
              .then(function (response) {
                if (response.success) {
                  vm.actuatorTypes = response.data;
                } else {
                  console.log("Error loading actuator types!");
                }
              });
        };

      }
    ]);