/* global app */

/**
 * Provides services for managing environment models.
 */
app.factory('EnvModelService', ['HttpService', '$resource', '$q', '$timeout', 'ENDPOINT_URI',
    function (HttpService, $resource, $q, $timeout, ENDPOINT_URI) {
        //URLs for server requests
        const URL_BASE = ENDPOINT_URI + '/env-models/';
        const URL_SUFFIX_SUBSCRIBE = '/subscribe';
        const URL_SUFFIX_REGISTER = '/register';
        const URL_SUFFIX_DEPLOY = '/deploy';
        const URL_SUFFIX_UNDEPLOY = '/undeploy';
        const URL_SUFFIX_START = '/start';
        const URL_SUFFIX_STOP = '/stop';
        const URL_SUFFIX_ENTITY_STATES = '/states';

        //Names of events that occur after subscription
        const MODEL_EVENT_ENTITY_UPDATE = 'entity_update';
        const MODEL_EVENT_VALUE = 'component_value';

        //Time after which reconnect is supposed to be tried
        const RECONNECT_TIME = 5 * 1000;

        /**
         * [Public]
         * Subscribes to events of an environment model by creating an event source object. Callback functions
         * for the individual events may be registered.
         *
         * @param modelID The ID of the model to subscribe to
         * @param onEntityUpdate Callback for entity state update events
         * @param onValue Callback for received component values
         * @returns {EventSource} The created event source object
         */
        function subscribeModel(modelID, onEntityUpdate, onValue) {
            //Create event source
            let eventSource = new EventSource(URL_BASE + modelID + URL_SUFFIX_SUBSCRIBE);

            //Handle loss of connection
            eventSource.onerror = function (event) {
                console.log("Model subscription error occurred");

                //Close source
                eventSource.close();

                //Try to re.-subscribe after some time
                $timeout(function () {
                    subscribeModel(modelID, onEntityUpdate, onValue);
                }, RECONNECT_TIME);
            };

            //Add event listeners
            eventSource.addEventListener(MODEL_EVENT_ENTITY_UPDATE, onEntityUpdate || new Function());
            eventSource.addEventListener(MODEL_EVENT_VALUE, onValue || new Function());

            return eventSource;
        }

        /**
         * [Public]
         * Performs a server request in order to register all components of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function registerComponents(modelID) {
            return HttpService.postRequest(URL_BASE + modelID + URL_SUFFIX_REGISTER);
        }

        /**
         * [Public]
         * Performs a server request in order to deploy all components of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function deployComponents(modelID) {
            return HttpService.postRequest(URL_BASE + modelID + URL_SUFFIX_DEPLOY);
        }

        /**
         * [Public]
         * Performs a server request in order to undeploy all components of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function undeployComponents(modelID) {
            return HttpService.postRequest(URL_BASE + modelID + URL_SUFFIX_UNDEPLOY);
        }

        /**
         * [Public]
         * Performs a server request in order to start all components of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function startComponents(modelID) {
            return HttpService.postRequest(URL_BASE + modelID + URL_SUFFIX_START);
        }

        /**
         * [Public]
         * Performs a server request in order to stop all components of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function stopComponents(modelID) {
            return HttpService.postRequest(URL_BASE + modelID + URL_SUFFIX_STOP);
        }

        /**
         * [Public]
         * Performs a server request in order to retrieve the states of all registered entities of a given model.
         * @param modelID The ID of the model
         * @returns {*}
         */
        function getEntityStates(modelID) {
            return HttpService.getRequest(URL_BASE + modelID + URL_SUFFIX_ENTITY_STATES);
        }

        //Expose public methods
        return {
            subscribeModel: subscribeModel,
            registerComponents: registerComponents,
            deployComponents: deployComponents,
            undeployComponents: undeployComponents,
            startComponents: startComponents,
            stopComponents: stopComponents,
            getEntityStates: getEntityStates
        }
    }
]);

