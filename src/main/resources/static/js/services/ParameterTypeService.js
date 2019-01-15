'use strict';

/*
 * This service retrieves a list of all available parameter types from the backend.
 */
app.factory('ParameterTypeService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the list of parameter types is available
    const URL_GET_ALL = ENDPOINT_URI + '/adapter/parameter-types';

    //Performs a server request in order to retrieve a list of all parameter types.
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