(function() {
  'use strict';

  angular
    .module('app')
    .controller('RegisterController', RegisterController);

  RegisterController.$inject = ['UserService', '$location', '$rootScope', 'FlashService'];

  function RegisterController(UserService, $location, $rootScope, FlashService) {
    var vm = this;

    vm.register = register;

    function register() {
      vm.dataLoading = true;
      UserService.Create(vm.user)
        .then(function(response) {
          if (response.success) {
            FlashService.Success(response.message, true);
            $location.path('/login');
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
  }

})();