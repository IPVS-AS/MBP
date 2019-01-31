/* global app */

app.factory('AdapterService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the list of parameter types is available
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-adapter/';

    //Performs a server request in order to retrieve a list of all parameter types.
    function getUsingComponents(adapterId) {
        return $http.get(URL_GET_USING_COMPONENTS + adapterId).then(handleSuccess, handleError);
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
        getUsingComponents: getUsingComponents
    };
}]);