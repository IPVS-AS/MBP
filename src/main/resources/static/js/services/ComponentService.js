/* global app */

/**
 * Provides services for dealing with components, retrieving their states and calculating stats.
 */
app.factory('ComponentService', ['HttpService', '$resource', '$q', 'ENDPOINT_URI',
    function (HttpService, $resource, $q, ENDPOINT_URI) {

        //URL prefix for requests
        const URL_PREFIX = ENDPOINT_URI + '/';
        //URL suffix for starting components
        const URL_START_COMPONENT = URL_PREFIX + 'start/';
        //URL suffix for stopping components
        const URL_STOP_COMPONENT = URL_PREFIX + 'stop/';
        //URL suffix for deploying and undeploying components
        const URL_DEPLOY_COMPONENT = URL_PREFIX + 'deploy/';
        //URL suffix under which the deployment state of all components can be retrieved
        const URL_GET_ALL_COMPONENT_STATES_SUFFIX = '/state';
        //URL suffix under which the deployment state of a certain component can be retrieved
        const URL_GET_COMPONENT_STATE_SUFFIX = '/state/';
        //URL suffix under which the deployment state of a certain component can be retrieved
        const URL_GET_VALUE_LOG_STATS_SUFFIX = '/stats';
        //URL suffix under which the value logs of a certain component can be retrieved
        const URL_VALUE_LOGS_SUFFIX = '/valueLogs';
        // URL suffix under which the active visualizations of a component can be retrieved or updated
        const URL_ACTIVE_VISUALIZATION_SUFFIX = 'component-vis';

        /**
         * [Public]
         * Performs a server request in order to start a certain component (in case it has been stopped before)m
         * optionally with given list of parameters.
         *
         * @param componentId The id of the component to start
         * @param componentType The type of the component to start
         * @param parameterList A list of parameters to use
         * @returns {*}
         */
        function startComponent(componentId, componentType, parameterList) {
            return HttpService.postRequest(URL_START_COMPONENT + componentType + '/' + componentId, parameterList, null);
        }


        /**
         * [Public]
         * Performs a server request in order to stop a certain component.
         *
         * @param componentId The id of the component to stop
         * @param componentType The type of the component to stop
         * @returns {*}
         */
        function stopComponent(componentId, componentType) {
            return HttpService.postRequest(URL_STOP_COMPONENT + componentType + '/' + componentId, null, null);
        }

        /**
         * [Public]
         * Performs a server request in order to deploy a certain component.
         *
         * @param componentId The id of the component to deploy
         * @param componentType The type of the component to deploy
         * @returns {*}
         */
        function deployComponent(componentId, componentType) {
            return HttpService.postRequest(URL_DEPLOY_COMPONENT + componentType + '/' + componentId, {}, null);
        }

        /**
         * [Public]
         * Performs a server request in order to undeploy a certain component.
         *
         * @param componentId The id of the component to undeploy
         * @param componentType The type of the component to undeploy
         * @returns {*}
         */
        function undeployComponent(componentId, componentType) {
            return HttpService.deleteRequest(URL_DEPLOY_COMPONENT + componentType + '/' + componentId);
        }

        /**
         * [Public]
         *  Performs a server request in order to retrieve the deployment states of all components of a certain type.
         *
         * @param component The component type for which the states of all components should be retrieved
         * @returns {*}
         */
        function getAllComponentStates(component) {
            return HttpService.getRequest(URL_PREFIX + component + URL_GET_ALL_COMPONENT_STATES_SUFFIX);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the deployment state of a certain component.
         *
         * @param componentId The id of the affected component
         * @param component The type of the component
         * @returns {*}
         */
        function getComponentState(componentId, component) {
            return HttpService.getRequest(URL_PREFIX + component + URL_GET_COMPONENT_STATE_SUFFIX + componentId);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the value log stats of a certain component. Optionally,
         * a unit may be provided in which the values are supposed to be displayed.
         *
         * @param componentId The id of the component for which the stats are supposed to be retrieved
         * @param component The type of the component
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getValueLogStats(componentId, component, unit) {
            let parameters = {};

            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            //Execute request
            return HttpService.getRequest(URL_PREFIX + component + 's/' + componentId + URL_GET_VALUE_LOG_STATS_SUFFIX, parameters);
        }


        /**
         * [Public]
         * Performs a server request in order to retrieve value logs for a certain component.
         *
         * @param componentId The id of the component for which the logs are supposed to be retrieved
         * @param component The type of the component
         * @param pageDetails Page details object (size, order etc.) that specifies the logs to retrieve
         * @param unit The unit in which the values are supposed to be retrieved
         * @returns {*}
         */
        function getValueLogs(componentId, component, pageDetails, unit) {
            let parameters = pageDetails;
            //Check if unit was provided
            if (unit) {
                parameters.unit = unit;
            }

            //Execute request
            return HttpService.getRequest(URL_PREFIX + component + 's/' + componentId + URL_VALUE_LOGS_SUFFIX, parameters).then(function (response) {
                //Process received logs in order to be able to display them in a chart
                return processValueLogs(response.content);
            });
        }


        /**
         * [Public]
         * Performs a server request in order to delete all recorded value logs of a certain component.
         *
         * @param componentId The id of the component whose value logs are supposed to be deleted
         * @param component The type of the component
         * @returns {*}
         */
        function deleteValueLogs(componentId, component) {
            //Execute request
            return HttpService.deleteRequest(URL_PREFIX + component + 's/' + componentId + URL_VALUE_LOGS_SUFFIX, null, null);
        }


        /**
         * [Public]
         * Processes an array of value logs as they are retrieved from the server. As a result, an array of
         * value logs is returned that can be used for charts (if in a second step a parsing with a
         * json path is proceeded).
         *
         * @param receivedLogs An array of value log objects as returned by the server
         */
        function processValueLogs(receivedLogs) {
            //Array that stores the finally formatted value logs
            let finalValues = [];

            //Iterate over all received value logs
            for (let i = 0; i < receivedLogs.length; i++) {
                //Extract value and time for the current log and format them
                let value = receivedLogs[i].value;
                let time = receivedLogs[i].time * 1000;

                //Create a (time, value) tuple and add it to the array
                let tuple = [time, value];
                finalValues.push(tuple);
            }

            //Return final value log array so that it is accessible in the promise
            return finalValues;
        }

        /**
         * [Public]
         * Performs a server put request to update the active visualizations of one component.
         *
         * @param componentId The id of the component to update
         * @param activeVisualization Object which holds all needed active visualization data
         * @return The updated sensor document (if successful)
         */
        function addNewActiveVisualization(componentId, activeVisualization) {
            return HttpService.putRequest(URL_PREFIX + URL_ACTIVE_VISUALIZATION_SUFFIX + "/" + componentId, activeVisualization);
        }

        /**
         * [Public]
         * Performs a server delete request to remove an active visualization from a components document.
         * @param componentId The component to which the visualization belongs.
         * @param visInstanceId The instance id of the visualization which should be removed.
         * @return The server answer.
         */
        function deleteActiveVisualization(componentId, visInstanceId) {
            return HttpService.deleteRequest(URL_PREFIX + URL_ACTIVE_VISUALIZATION_SUFFIX + "/" + componentId + "/" + visInstanceId);
        }

        /**
         * [Private]
         * Converts a javascript date object to a human-readable date string in the "dd.mm.yyyy hh:mm:ss" format.
         *
         * @param date The date object to convert
         * @returns The generated date string in the corresponding format
         */
        function dateToString(date) {
            //Retrieve all properties from the date object
            let year = date.getFullYear();
            let month = '' + (date.getMonth() + 1);
            let day = '' + date.getDate();
            let hours = '' + date.getHours();
            let minutes = '' + date.getMinutes();
            let seconds = '' + date.getSeconds();

            //Add a leading zero (if necessary) to all properties except the year
            let values = [day, month, hours, minutes, seconds];
            for (let i = 0; i < values.length; i++) {
                if (values[i].length < 2) {
                    values[i] = '0' + values[i];
                }
            }

            //Generate and return the date string
            return ([values[0], values[1], year].join('.')) +
                ' ' + ([values[2], values[3], values[4]].join(':'));
        }

        //Expose
        return {
            COMPONENT: {
                SENSOR: 'SENSOR',
                ACTUATOR: 'ACTUATOR'
            },
            processValueLogs: processValueLogs,
            getAllComponentStates: getAllComponentStates,
            getComponentState: getComponentState,
            getValueLogStats: getValueLogStats,
            getValueLogs: getValueLogs,
            deleteValueLogs: deleteValueLogs,
            startComponent: startComponent,
            stopComponent: stopComponent,
            deployComponent: deployComponent,
            undeployComponent: undeployComponent,
            addNewActiveVisualization: addNewActiveVisualization,
            deleteActiveVisualization: deleteActiveVisualization
        };
    }
]);

