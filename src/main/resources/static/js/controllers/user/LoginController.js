(function() {
  'use strict';

  angular
    .module('app')
    .controller('LoginController', LoginController);

  LoginController.$inject = ['$location', 'AuthenticationService', 'FlashService'];

  function LoginController($location, AuthenticationService, FlashService) {
    var vm = this;

    vm.login = login;

    (function initController() {
      // reset login status
      AuthenticationService.ClearCredentials();
      AuthenticationService.Logout();
    })();

    function login() {
      vm.dataLoading = true;
      AuthenticationService.Login(vm.username, vm.password, function(response) {
        console.log(response);
        if (response.status === 200) {
          //Get user object
          let userData = response.data || {};

          //Enable authorization locally
          AuthenticationService.SetCredentials(vm.username, vm.password, userData);

          //Redirect
          $location.path('/');

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
  }

})();