/* global app */

/**
 * Provides services for dealing with SSH key pairs.
 */
app.factory('KeyPairService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the using rules can be retrieved
    const URL_GENERATE = ENDPOINT_URI + '/key-pairs/generate';

    //Performs a server request in order to generate a new key pair with a given name
    function generateKeyPair(name) {
        return $http.post(URL_GENERATE, name);
    }

    //Expose public functions
    return {
        generate: generateKeyPair
    };

}]);
