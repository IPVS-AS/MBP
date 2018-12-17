(function() {
  'use strict';

  angular
    .module('app')
    .factory('ComponentTypeService', ComponentTypeService);

  ComponentTypeService.$inject = ['$http', 'ENDPOINT_URI'];

  function ComponentTypeService($http, ENDPOINT_URI) {
    var service = {};

    service.Create = Create;
    service.GetAll = GetAll;
    service.GetByComponent = GetByComponent;

    return service;

    function Create(user) {
      return $http.post(ENDPOINT_URI + '/component-types', componentType).then(handleSuccess, handleError);
    }

    function GetAll() {
      return $http.get(ENDPOINT_URI + '/component-types').then(handleSuccess, handleError);
    }

    function GetByComponent(component) {
      return $http.get(ENDPOINT_URI + '/component-types/' + component).then(handleSuccess, handleError);
    }

    // private functions

    function handleSuccess(res) {
      return {
        success: true,
        message: res.headers('X-MBP-alert'),
        data: res.data
      };
    }

    function handleError(res) {
      return {
        success: false,
        message: res.headers('X-MBP-error')
      };

    }
  }

})();