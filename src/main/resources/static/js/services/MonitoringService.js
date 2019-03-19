/* global app */

/**
 * Provides services for monitoring and the retrieval of monitoring adapters.
 */
app.factory('MonitoringService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URL under which the compatible monitoring adapters for a device can be retrieved
        const URL_GET_COMPATIBLE_ADAPTERS = ENDPOINT_URI + '/monitoring-adapters/by-device/';

        const URL_MONITORING_PREFIX = ENDPOINT_URI + '/monitoring/';
        const URL_MONITORING_SUFFIX = '?adapter=';

        //Performs a server request in order to retrieve a list of all compatible monitoring adapters for a device.
        function getCompatibleMonitoringAdapters(deviceId) {
            return $http.get(URL_GET_COMPATIBLE_ADAPTERS + deviceId);
        }

        function isMonitoringActive(deviceId, monitoringAdapterId) {
            return $http.get(generateMonitoringURL(deviceId, monitoringAdapterId));
        }

        function enableMonitoring(deviceId, monitoringAdapterId, parameterList) {
            return $http({
                method: 'POST',
                url: generateMonitoringURL(deviceId, monitoringAdapterId),
                data: parameterList,
                headers: {'Content-Type': 'application/json'}
            });
        }

        function disableMonitoring(deviceId, monitoringAdapterId) {
            return $http({
                method: 'DELETE',
                url: generateMonitoringURL(deviceId, monitoringAdapterId)
            });
        }

        /**
         * [Private]
         * Generates the monitoring URL that can be used for monitoring server requests.
         *
         * @param deviceId Id of the affected device
         * @param monitoringAdapterId Id of the affected monitoring adapter
         */
        function generateMonitoringURL(deviceId, monitoringAdapterId) {
            return URL_MONITORING_PREFIX + deviceId + URL_MONITORING_SUFFIX + monitoringAdapterId;
        }

        return {
            getCompatibleMonitoringAdapters: getCompatibleMonitoringAdapters,
            isMonitoringActive: isMonitoringActive,
            enableMonitoring: enableMonitoring,
            disableMonitoring: disableMonitoring
        }
    }
]);

