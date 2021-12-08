/**
 * Provides services for dealing with data model objects and retrieving a list of operators which use a certain data
 * model.
 */
app.factory('DataModelService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {
    //URL under which the using components can be retrieved
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-data-model/';

    //Performs a server request in order to retrieve a list of all using operators.
    function getUsingOperators(dataModelId) {
        return HttpService.getRequest(URL_GET_USING_COMPONENTS + dataModelId);
    }

    //Expose
    return {
        getUsingOperators: getUsingOperators
    };
}]);