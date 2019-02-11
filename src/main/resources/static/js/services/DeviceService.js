/* global app */

/**
 * Provides services for dealing with device objects and retrieving a list of components which use a certain device.
 */
app.factory('DeviceService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {
    //URL under which the using components can be retrieved
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-device/';

    //Performs a server request in order to retrieve a list of all using components.
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

    return {
        getUsingComponents: getUsingComponents,
        formatMacAddress: function (address) {
            if (address) {
                return address.match(/.{1,2}/g).join('-').toUpperCase();
            }
            return address;
        },
        normalizeMacAddress: function (address) {
            if (address) {
                norm = address.replace(new RegExp('-', 'g'), '');
                return norm.toLowerCase();
            }
            return address;
        }
    };
}]);