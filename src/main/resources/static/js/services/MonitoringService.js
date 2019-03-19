/* global app */

/**
 * Provides services for monitoring and the retrieval of monitoring adapters.
 */
app.factory('MonitoringService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URL under which the compatible monitoring adapters for a device can be retrieved
        const URL_GET_COMPATIBLE_ADAPTERS = ENDPOINT_URI + '/monitoring-adapters/by-device/';

        //Performs a server request in order to retrieve a list of all compatible monitoring adapters for a device.
        function getCompatibleMonitoringAdapters(deviceId) {
            return $http.get(URL_GET_COMPATIBLE_ADAPTERS + deviceId);
        }

        return {
            getCompatibleMonitoringAdapters: getCompatibleMonitoringAdapters
        }
    }
]);

