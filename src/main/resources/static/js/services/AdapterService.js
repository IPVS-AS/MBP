
/**
 * Provides services for dealing with adapter objects and retrieving a list of components which use a certain adapter.
 */
app.factory('AdapterService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {
    //URL under which the using components can be retrieved
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-adapter/';

    //Performs a server request in order to retrieve a list of all using components.
    function getUsingComponents(adapterId) {
        return $http.get(URL_GET_USING_COMPONENTS + adapterId);
    }

    //Expose
    return {
        getUsingComponents: getUsingComponents
    };
}]);