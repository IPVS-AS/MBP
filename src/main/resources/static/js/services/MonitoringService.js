/* global app */

/**
 * Provides services for monitoring and the retrieval of monitoring adapters.
 */
app.factory('MonitoringService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_GET_COMPATIBLE_ADAPTERS = ENDPOINT_URI + '/monitoring-adapters/by-device/';
        const URL_MONITORING_PREFIX = ENDPOINT_URI + '/monitoring/';
        const URL_MONITORING_STATE = ENDPOINT_URI + '/monitoring/state/';
        const URL_MONITORING_ADAPTER_SUFFIX = '?adapter=';

        /**
         * Performs a server request in order to retrieve the monitoring state for all adapters that are compatible to a certain device
         *
         * @param deviceId The id of the device for whose adapters the monitoring state should be retrieved
         * @returns {*}
         */
        function getDeviceMonitoringState(deviceId) {
            return $http.get(URL_MONITORING_STATE + deviceId);
        }

        /**
         * Performs a server request in order to retrieve the monitoring state for a certain adapter and device.
         *
         * @param deviceId The id of the device for which the state should be retrieved
         * @param monitoringAdapterId The id of the monitoring adapter in question
         * @returns {*}
         */
        function getMonitoringState(deviceId, monitoringAdapterId) {
            return $http.get(URL_MONITORING_STATE + deviceId + URL_MONITORING_ADAPTER_SUFFIX + monitoringAdapterId);
        }

        /**
         * Performs a server request in order to retrieve a list of all compatible monitoring adapters for a given device.
         *
         * @param deviceId The id of the device for which the monitoring adapters should be retrieved
         * @returns {*}
         */
        function getCompatibleMonitoringAdapters(deviceId) {
            return $http.get(URL_GET_COMPATIBLE_ADAPTERS + deviceId);
        }

        /**
         * Performs a server request in order to check whether the monitoring for a given device and adapter is active
         *
         * @param deviceId The id of the device to check
         * @param monitoringAdapterId The id of the monitoring adapter in question
         * @returns {*}
         */
        function isMonitoringActive(deviceId, monitoringAdapterId) {
            return $http.get(generateMonitoringURL(deviceId, monitoringAdapterId));
        }

        /**
         * Performs a server request in order to enable monitoring of a given device with a given adapter and parameters
         *
         * @param deviceId The id if the device to monitor
         * @param monitoringAdapterId The id of the adapter to use for the monitoring
         * @param parameterList A list of parameters that might be required by the adapter
         * @returns {*}
         */
        function enableMonitoring(deviceId, monitoringAdapterId, parameterList) {
            return $http({
                method: 'POST',
                url: generateMonitoringURL(deviceId, monitoringAdapterId),
                data: parameterList,
                headers: {'Content-Type': 'application/json'}
            });
        }


        /**
         * [Public]
         * Performs a server request in order to disable monitoring of a given device with a given adapter
         *
         * @param deviceId The id of the device that is currently monitored
         * @param monitoringAdapterId The id of the monitoring adapter that should no longer be used
         * @returns {*}
         */
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
            return URL_MONITORING_PREFIX + deviceId + URL_MONITORING_ADAPTER_SUFFIX + monitoringAdapterId;
        }

        //Expose public methods
        return {
            getDeviceMonitoringState: getDeviceMonitoringState,
            getMonitoringState: getMonitoringState,
            getCompatibleMonitoringAdapters: getCompatibleMonitoringAdapters,
            isMonitoringActive: isMonitoringActive,
            enableMonitoring: enableMonitoring,
            disableMonitoring: disableMonitoring
        }
    }
]);

