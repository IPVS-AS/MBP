/* global app */

/**
 * Provides services for dealing with SSH key pairs.
 */
app.factory('KeyPairService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {
    //URL under which the using rules can be retrieved
    const URL_GENERATE = ENDPOINT_URI + '/key-pairs/generate';
    //URL under which the using devices can be retrieved
    const URL_GET_USING_DEVICES = ENDPOINT_URI + '/devices/by-key/';

    //Performs a server request in order to generate a new key pair with a given name
    function generateKeyPair(name) {
        return HttpService.postRequest(URL_GENERATE, name);
    }

    //Performs a server request in order to retrieve a list of all using devices for a key pair.
    function getUsingDevices(keyPairId) {
        return HttpService.getRequest(URL_GET_USING_DEVICES + keyPairId);
    }

    //Expose public functions
    return {
        generate: generateKeyPair,
        getUsingDevices: getUsingDevices
    };

}]);
