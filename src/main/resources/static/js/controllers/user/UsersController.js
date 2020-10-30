(function() {
  'use strict';

  angular
    .module('app')
    .controller('UsersController', UsersController);

  UsersController.$inject = ['$location', 'UserService', 'FlashService'];

  function UsersController($location, UserService, FlashService) {
    var vm = this;
    vm.user = {};

    vm.deleteUser = deleteUser;
    vm.loadUsers = loadUsers;
    vm.createUpdateUser = createUpdateUser;
    vm.changeUpdate = changeUpdate;
    vm.changeRegister = changeRegister;

    vm.processName = "Register";
    vm.update = false;

    (function initController() {
      loadUsers();
    })();

    function loadUsers() {
      vm.dataLoading = true;
      UserService.GetAll()
        .then(function(response) {
          if (response.success) {
            vm.users = response;
            vm.dataLoading = false;
          } else {
            if (response.status === 403) {
              FlashService.Error("Authorization error!");
            } else {
              FlashService.Error(response.message);
            }
            vm.dataLoading = false;
          }
        });
    };

    function deleteUser(username) {
      vm.dataLoading = true;
      UserService.Delete(username)
        .then(function(response) {
          if (response.success) {
            FlashService.Success(response.message, false);
            loadUsers();
          } else {
            if (response.status === 403) {
              FlashService.Error("Authorization error!");
            } else {
              FlashService.Error(response.message);
            }
            vm.dataLoading = false;
          }
        });
    };

    function createUpdateUser() {
      vm.dataLoading = true;
      if (vm.update) {
        UserService.Update(vm.user)
          .then(function(response) {
            if (response.success) {
              FlashService.Success(response.message, false);
              loadUsers();
            } else {
              if (response.status === 403) {
                FlashService.Error("Authorization error!");
              } else {
                FlashService.Error(response.message);
              }
              vm.dataLoading = false;
            }
          });
      } else {
        UserService.Create(vm.user)
          .then(function(response) {
            if (response.success) {
              FlashService.Success(response.message, false);
              loadUsers();
            } else {
              if (response.status === 403) {
                FlashService.Error("Authorization error!");
              } else {
                FlashService.Error(response.message);
              }
              vm.dataLoading = false;
            }
          });
      }
    };

    function changeUpdate(user) {
      vm.processName = "Update";
      vm.update = true;
      vm.user.id = user.id;
      vm.user.firstName = user.firstName;
      vm.user.lastName = user.lastName;
      vm.user.username = user.username;
    };

    function changeRegister() {
      vm.processName = "Register";
      vm.update = false;
      vm.user = {};
    };

  }

})();