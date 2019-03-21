(function() {
  'use strict';

  angular
    .module('app')
    .factory('ComponentTypeService', ComponentTypeService);

  ComponentTypeService.$inject = ['$http', 'ENDPOINT_URI'];

  function ComponentTypeService($http, ENDPOINT_URI) {

    const RESOURCE_LOCATION = "/component-types";

    var service = {};

    service.Create = Create;
    service.GetAll = GetAll;
    service.GetByComponent = GetByComponent;

    return service;

    function Create(user) {
      return $http.post(ENDPOINT_URI + RESOURCE_LOCATION, componentType).then(handleSuccess, handleError);
    }

    function GetAll() {
      return $http.get(ENDPOINT_URI + RESOURCE_LOCATION).then(handleSuccess, handleError);
    }

    function GetByComponent(component) {
      return $http.get(ENDPOINT_URI + RESOURCE_LOCATION +  '/' + component).then(handleSuccess, handleError);
    }

    // private functions
    function handleSuccess(res) {
      //Check if component types were returned
      if(Array.isArray(res.data)){
        //Expand types for resource URIs
        for(var i = 0; i < res.data.length; i++){
          res.data[i].href = ENDPOINT_URI + RESOURCE_LOCATION + '/' + res.data[i].id;
        }
      }

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