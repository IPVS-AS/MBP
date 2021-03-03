/**
 * Provides services for dealing with operator objects and retrieving a list of components which use a certain operator.
 */
app.factory('OperatorService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {
    //URL under which the using components can be retrieved
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-operator/';
    const URL_GET_USING_TESTS = ENDPOINT_URI + '/test/by-sensor/';

    //Performs a server request in order to retrieve a list of all using components.
    function getUsingComponents(operatorId) {
        return HttpService.getRequest(URL_GET_USING_COMPONENTS + operatorId);
    }

    //Performs a server request in order to retrieve a list of all tests using a given sensor
    function getUsingTests(sensorId) {
        return HttpService.getRequest(URL_GET_USING_TESTS + sensorId);
    }

    //Expose
    return {
        getUsingComponents: getUsingComponents,
        getUsingTests: getUsingTests
    };
}]);