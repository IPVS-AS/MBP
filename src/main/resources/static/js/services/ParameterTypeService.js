'use strict';

app.factory('ParameterTypeService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    const URL_GET_ALL = ENDPOINT_URI + '/adapter/parameter-types';

    //public
    function getAll() {
        return $http.get(URL_GET_ALL).then(handleSuccess, handleError);
    }

    //private
    function handleSuccess(response) {
        return {
            success: true,
            data: response.data
        };
    }

    //private
    function handleError(res) {
        return {
            success: false
        };

    }

    //Expose
    return {
        getAll: getAll
    };
}]);