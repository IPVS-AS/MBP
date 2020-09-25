/* global app */

/**
 * Provides services for HTTP requests.
 */
app.factory('HttpService', ['$http', 'ENDPOINT_URI', 'UserService', 'NotificationService',
    function ($http, ENDPOINT_URI, UserService, NotificationService) {

        const debugMode = true;

        /**
         * [Private]
         * Generates the header that is supposed to be used for HTTP requests.
         * @returns The header object (key-value)
         */
        function generateHeader() {
            return {
                'Content-Type': 'application/json;charset=UTF8',
                'X-MBP-Access-Request': UserService.getUserAttributes()
            }
        }

        /**
         * [Private]
         * Performs a GET request with given URL parameters and returns the resulting promise.
         * @param url The URl of the request
         * @param params The URL parameters
         * @returns {*|void} The resulting promise
         */
        function getRequest(url, params) {
            //Debug message
            debug("Initiating GET request at " + url + " with parameters:", params);

            //Sanity check
            if (typeof params === 'undefined') {
                params = [];
            }

            //Perform request
            return $.ajax({
                url: url,
                data: params,
                headers: generateHeader()
            }).done(function (response) {
                //Debug message
                debug("Request succeeded, response:", response);

                //Propagate response
                return response;
            }).fail(handleError).always(function () {
                debug("Request processing completed.");
            });
        }

        /**
         * [Private]
         * Performs a POST request with a given JSON payload and returns the resulting promise.
         * @param url The URl of the request
         * @param jsonPayload The JSON payload of the POST request
         * @returns {*|void} The resulting promise
         */
        function postRequest(url, jsonPayload) {
            //Debug message
            debug("Initiating POST request at " + url + " with payload:", jsonPayload);

            //Perform request
            return $.ajax({
                type: "POST",
                url: url,
                data: jsonPayload,
                headers: generateHeader()
            }).done(function (response) {
                //Debug message
                debug("Request succeeded, response:", response);

                //Propagate response
                return response;
            }).fail(handleError).always(function () {
                debug("Request processing completed.");
            });
        }

        /**
         * [Private]
         * Handles errors that occur during requests or are sent as response to a request.
         * @param response The error response (if available)
         */
        function handleError(response) {
            //Debug messages
            debug("Request failed, response:", response);

            //Check if response is available
            if ((typeof response === 'object') &&
                ((typeof response.message === 'string') || (response.message instanceof String))
                && response.message.trim().length > 3) {
                NotificationService.showError(response.message);
            } else {
                NotificationService.showError("Request to backend failed. Is it online?");
            }
        }

        /**
         * [Private]
         * Prints debug messages to the console, if debug mode is enabled.
         * @param messages The debug messages to print
         */
        function debug(messages) {
            if (debugMode) {
                console.log(messages);
            }
        }

        /**
         * [Public]
         * Returns all entities f
         * @param category
         * @returns {*}
         */
        function getAll(category) {
            return getRequest(ENDPOINT_URI + "/" + category).then(function (data) {
                //Extend received object for empty list if none available
                data._embedded = data._embedded || {};
                data._embedded[category] = data._embedded[category] || [];

                //Extract entity list
                return data._embedded[category];
            });
        }

        /**
         * [Public]
         *
         * @param category
         * @param id
         * @returns {*}
         */
        function getOne(category, id) {
            //Perform get request
            return getRequest(ENDPOINT_URI + "/" + category + "/" + id).then(function (data) {
                //Sanitize and return entity
                return data || {};
            });
        }

        function addOne(category, data) {
            //Sanitize data
            if (typeof data === 'undefined') {
                data = {};
            }

            //Perform post request
            return postRequest(ENDPOINT_URI + "/" + category, data).then(function (data) {
                //Sanitize and return entity
                return data || {};
            });
        }

        //Expose public functions
        return {
            getOne: getOne,
            getAll: getAll,
            addOne: addOne
        };
    }]);
