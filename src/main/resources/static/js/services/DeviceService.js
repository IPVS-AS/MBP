/* global app */

/**
 * Provides services for dealing with device objects and retrieving a list of components which use a certain device.
 */
app.factory('DeviceService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {
    //URL under which the using components can be retrieved
    const URL_GET_USING_COMPONENTS = ENDPOINT_URI + '/components/by-device/';

    //URL under which the availability state of all devices can be retrieved
    const URL_GET_ALL_DEVICE_STATES = ENDPOINT_URI + '/devices/state';

    //URL under which the availability state of a certain device can be retrieved
    const URL_GET_DEVICE_STATE = ENDPOINT_URI + '/devices/{0}/state/';

    //Performs a server request in order to retrieve a list of all using components.
    function getUsingComponents(adapterId) {
        return HttpService.getRequest(URL_GET_USING_COMPONENTS + adapterId);
    }

    //Performs a server request in order to retrieve the availability states of all devices.
    function getAllDeviceStates() {
        return HttpService.getRequest(URL_GET_ALL_DEVICE_STATES);
    }

    //Performs a server request in order to retrieve the availability state of a certain device.
    function getDeviceState(deviceId) {
        return HttpService.getRequest(URL_GET_DEVICE_STATE.format(deviceId));
    }

    //public
    function formatMacAddress(address) {
        if (address) {
            return address.match(/.{1,2}/g).join('-').toUpperCase();
        }
        return address;
    }

    //public
    function normalizeMacAddress(address) {
        if (address) {
            norm = address.replace(new RegExp('-', 'g'), '');
            return norm.toLowerCase();
        }
        return address;
    }

    //Expose public functions
    return {
        getUsingComponents: getUsingComponents,
        getDeviceState: getDeviceState,
        getAllDeviceStates: getAllDeviceStates,
        formatMacAddress: formatMacAddress,
        normalizeMacAddress: normalizeMacAddress
    };
}]);
