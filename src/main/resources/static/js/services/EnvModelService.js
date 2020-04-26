/* global app */

/**
 * Provides services for managing environment models.
 */
app.factory('EnvModelService', ['$http', '$resource', '$q', '$timeout', 'ENDPOINT_URI',
    function ($http, $resource, $q, $timeout, ENDPOINT_URI) {
        //URLs for server requests
        const URL_BASE = ENDPOINT_URI + '/env-models/';
        const URL_SUFFIX_SUBSCRIBE = '/subscribe';
        const URL_SUFFIX_REGISTER = '/register';

        //Names of events that occur after subscription
        const MODEL_EVENT_REGISTER = 'entity_registered';
        const MODEL_EVENT_DEPLOY = 'component_deploy';
        const MODEL_EVENT_UNDEPLOY = 'component_undeploy';
        const MODEL_EVENT_START = 'component_start';
        const MODEL_EVENT_STOP = 'component_stop';

        //Time after which reconnect is supposed to be tried
        const RECONNECT_TIME = 5 * 1000;

        /**
         * [Public]
         * Subscribes to events of an environment model by creating an event source object. Callback functions
         * for the individual events may be registered.
         *
         * @param modelID The ID of the model to subscribe to
         * @param onRegister Callback for entity registration events
         * @param onDeploy Callback for component deployment events
         * @param onUndeploy Callback for component undeployment events
         * @param onStart Callback for component start events
         * @param onStop Callback for component stop events
         * @returns {EventSource} The created event source object
         */
        function subscribeModel(modelID, onRegister, onDeploy, onUndeploy, onStart, onStop) {
            //Create event source
            let eventSource = new EventSource(URL_BASE + modelID + URL_SUFFIX_SUBSCRIBE);

            //Handle loss of connection
            eventSource.onerror = function (event) {
                console.log("Model subscription error occurred");

                //Close source
                eventSource.close();

                //Try to re.-subscribe after some time
                $timeout(function () {
                    subscribeModel(modelID, onRegister, onDeploy, onUndeploy, onStart, onStop);
                }, RECONNECT_TIME);
            };

            //Add event listeners
            eventSource.addEventListener(MODEL_EVENT_REGISTER, onRegister || new Function());
            eventSource.addEventListener(MODEL_EVENT_DEPLOY, onDeploy || new Function());
            eventSource.addEventListener(MODEL_EVENT_UNDEPLOY, onUndeploy || new Function());
            eventSource.addEventListener(MODEL_EVENT_START, onStart || new Function());
            eventSource.addEventListener(MODEL_EVENT_STOP, onStop || new Function());

            return eventSource;
        }

        /**
         * [Public]
         * Performs a server request in order to register all components of a given model.
         * @param modelID The ID of the affected model
         * @returns {*}
         */
        function registerComponents(modelID) {
            return $http.post(URL_BASE + modelID + URL_SUFFIX_REGISTER);
        }

        //Expose public methods
        return {
            subscribeModel: subscribeModel,
            registerComponents: registerComponents,
        }
    }
]);

