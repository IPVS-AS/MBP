/* global app */

/**
 * Provides services for monitoring and the retrieval of monitoring operators.
 */
app.factory('MonitoringService', ['HttpService', '$resource', '$q', 'ENDPOINT_URI', 'ComponentService',
    function (HttpService, $resource, $q, ENDPOINT_URI, ComponentService) {
        //URLs for server requests
        const URL_GET_COMPATIBLE_OPERATORS = ENDPOINT_URI + '/monitoring-operators/by-device/';
        const URL_MONITORING_PREFIX = ENDPOINT_URI + '/monitoring/';
        const URL_GET_STATE = URL_MONITORING_PREFIX + 'state/';
        const URL_STATS_SUFFIX = '/stats';
        const URL_VALUE_LOGS_SUFFIX = '/valueLogs';
        const URL_OPERATOR_SUFFIX = '?monitoringOperatorId=';

        /**
         * [Public]
         * Performs a server request in order to retrieve the monitoring state for all operators that are compatible to a certain device
         *
         * @param deviceId The id of the device for whose operators the monitoring state should be retrieved
         * @returns {*}
         */
        function getDeviceMonitoringState(deviceId) {
            return HttpService.getRequest(URL_GET_STATE + deviceId);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the monitoring state for a certain operator and device.
         *
         * @param deviceId The id of the device for which the state should be retrieved
         * @param monitoringOperatorId The id of the monitoring operator in question
         * @returns {*}
         */
        function getMonitoringState(deviceId, monitoringOperatorId) {
            return HttpService.getRequest(URL_GET_STATE + deviceId + URL_OPERATOR_SUFFIX + monitoringOperatorId);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve a list of all compatible monitoring operators for a given device.
         *
         * @param deviceId The id of the device for which the monitoring operators should be retrieved
         * @returns {*}
         */
        function getCompatibleMonitoringOperators(deviceId) {
            return HttpService.getRequest(URL_GET_COMPATIBLE_OPERATORS + deviceId);
        }

        /**
         * [Public]
         * Performs a server request in order to check whether the monitoring for a given device and operator is active
         *
         * @param deviceId The id of the device to check
         * @param monitoringOperatorId The id of the monitoring operator in question
         * @returns {*}
         */
        function isMonitoringActive(deviceId, monitoringOperatorId) {
            return HttpService.getRequest(generateMonitoringURL(deviceId, monitoringOperatorId));
        }

        /**
         * [Public]
         * Performs a server request in order to enable monitoring of a given device with a given operator and parameters
         *
         * @param deviceId The id if the device to monitor
         * @param monitoringOperatorId The id of the operator to use for the monitoring
         * @param parameterList A list of parameters that might be required by the operator
         * @returns {*}
         */
        function enableMonitoring(deviceId, monitoringOperatorId, parameterList) {
            return HttpService.postRequest(generateMonitoringURL(deviceId, monitoringOperatorId), parameterList);
        }


        /**
         * [Public]
         * Performs a server request in order to disable monitoring of a given device with a given operator
         *
         * @param deviceId The id of the device that is currently monitored
         * @param monitoringOperatorId The id of the monitoring operator that should no longer be used
         * @returns {*}
         */
        function disableMonitoring(deviceId, monitoringOperatorId) {
            return HttpService.deleteRequest(generateMonitoringURL(deviceId, monitoringOperatorId));
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the value log stats of a certain monioring component.
         * Optionally, a unit may be provided in which the values are supposed to be displayed.
         *
         * @param deviceId The id of the device for which the stats are supposed to be retrieved
         * @param monitoringOperatorId The id of the monitoring operator for which the stats are supposed to be retrieved
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getMonitoringValueLogStats(deviceId, monitoringOperatorId, unit) {
            var parameters = {};

            //Extend parameters for operator id
            parameters.monitoringOperatorId = monitoringOperatorId;

            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            //Execute request
            return HttpService.getRequest(URL_MONITORING_PREFIX + deviceId + URL_STATS_SUFFIX, parameters);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve monitoring value logs for a certain monitoring component.
         *
         * @param deviceId The id of the device for which monitoring data is supposed to be retrieved
         * @param monitoringOperatorId The id of the monitoring operator for which data is supposed to be retrieved
         * @param pageDetails Page details object (size, order etc.) that specifies the logs to retrieve
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getMonitoringValueLogs(deviceId, monitoringOperatorId, pageDetails, unit) {
            var parameters = pageDetails;

            //Extend parameters for operator id
            parameters.monitoringOperatorId = monitoringOperatorId;

            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            //Execute request
            return HttpService.getRequest(URL_MONITORING_PREFIX + deviceId + URL_VALUE_LOGS_SUFFIX, parameters).then(function (response) {
                //Process received logs in order to be able to display them in a chart
                return ComponentService.processValueLogs(response.content);
            });
        }


        /**
         * [Public]
         * Performs a server request in order to delete all recorded value logs of a certain monitoring component.
         *
         * @param componentId The id of the component whose value logs are supposed to be deleted
         * @param component The type of the component
         * @param deviceId The id of the device that is part of the monitoring component whose value logs
         * are supposed to be deleted
         * @param monitoringOperatorId The id of the monitoring operator that is part of the monitoring component
         * whose value logs are supposed to be deleted
         * @returns {*}
         */
        function deleteMonitoringValueLogs(deviceId, monitoringOperatorId) {
            //Execute request
            return HttpService.deleteRequest(URL_MONITORING_PREFIX + deviceId + URL_VALUE_LOGS_SUFFIX + '?operatorId=' + monitoringOperatorId);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve all available monitoring components. Each monitoring
         * component consists out of a device and a compatible monitoring operator.
         *
         * @returns {*}
         */
        function getMonitoringComponents() {
            return HttpService.getRequest(URL_MONITORING_PREFIX);
        }


        /**
         * [Private]
         * Generates the monitoring URL that can be used for monitoring server requests.
         *
         * @param deviceId Id of the affected device
         * @param monitoringOperatorId Id of the affected monitoring operator
         */
        function generateMonitoringURL(deviceId, monitoringOperatorId) {
            return URL_MONITORING_PREFIX + deviceId + URL_OPERATOR_SUFFIX + monitoringOperatorId;
        }

        //Expose public methods
        return {
            getDeviceMonitoringState: getDeviceMonitoringState,
            getMonitoringState: getMonitoringState,
            getCompatibleMonitoringOperators: getCompatibleMonitoringOperators,
            isMonitoringActive: isMonitoringActive,
            enableMonitoring: enableMonitoring,
            disableMonitoring: disableMonitoring,
            getMonitoringValueLogStats: getMonitoringValueLogStats,
            getMonitoringValueLogs: getMonitoringValueLogs,
            deleteMonitoringValueLogs: deleteMonitoringValueLogs,
            getMonitoringComponents: getMonitoringComponents
        }
    }
]);

