(function() {
  'use strict';

  angular
    .module('app')
    .factory('GamificationUserService', GamificationUserService);

  GamificationUserService.$inject = ['$http', 'ENDPOINT_URI', 'BASE_URI'];

  function GamificationUserService($http, ENDPOINT_URI, BASE_URI) {
    var service = {};

    service.createGamificationUser = createGamificationUser;
    service.TEST_createGamificationUser = TEST_createGamificationUser;
    service.getGamificationUserByUser = getGamificationUserByUser; 
    service.updateGamificationUser = updateGamificationUser;
    service.getGamificationQuestDB = getGamificationQuestDB;
    //service.deleteGamificationUser = deleteGamificationUser;

    return service;

    function createGamificationUser(gamificationUser) {
      return $http.post(ENDPOINT_URI + '/gamificationUser', gamificationUser).then(handleSuccess, handleError);
    }
    
    function getGamificationUserByUser() {
      return $http.get(ENDPOINT_URI + '/gamificationUser').then(handleSuccess, handleError);
    }

    function updateGamificationUser(gamificationUser) {
      return $http.put(ENDPOINT_URI + '/gamificationUser/', gamificationUser).then(handleSuccess, handleError);
    }
    
    function getGamificationQuestDB() {
      return $http.get(ENDPOINT_URI + '/gamificationQuestDB').then(handleSuccess, handleError);
    }

    /*function deleteGamificationUser(gamificationUser) {
      return $http.delete(ENDPOINT_URI + '/gamificationUsers/' + gamificationUser).then(handleSuccess, handleError);
    }*/

	function TEST_createGamificationUser() {
      return $http.post(ENDPOINT_URI + '/TEST_createGamificationUser', "").then(handleSuccess, handleError);
    }

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
        message: res.headers('X-MBP-error'),
        status: res.status
      };

    }
  }

})();