/* global app */

/**
 * Provides services for HTTP requests.
 */
app.factory('HttpService', ['$rootScope', 'ENDPOINT_URI', 'NotificationService',
    function ($rootScope, ENDPOINT_URI, NotificationService) {
        //Enables or disables the debug mode
        const debugMode = true;

        /**
         * Returns a string of user attributes.
         *
         * @returns {string} The user attributes string
         */
        function getUserAttributes() {
            //Check if user data is available
            if (typeof $rootScope.globals.currentUser === 'undefined') {
                return "";
            }

            //Put header string together
            return "requesting-entity-firstname=" + $rootScope.globals.currentUser.userData.firstName + ";;"
                + "requesting-entity-lastname=" + $rootScope.globals.currentUser.userData.lastName + ";;"
                + "requesting-entity-username=" + $rootScope.globals.currentUser.username;
        }

        /**
         * [Private]
         * Generates the header that is supposed to be used for HTTP requests.
         * @returns The header object (key-value)
         */
        function generateHeader() {
            //Create header object
            let headers = {
                'Content-Type': 'application/json;charset=UTF8',
                'X-MBP-Access-Request': getUserAttributes()
            }

            //Check if authorization can be added to the header
            if ($rootScope.globals.currentUser) {
                headers['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata;
            }

            return headers;
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
         * Performs a POST request with a given payload and returns the resulting promise.
         * @param url The URl of the request
         * @param payload The payload of the POST request
         * @returns {*|void} The resulting promise
         */
        function postRequest(url, payload) {
            //Sanitize payload
            payload = payload || {};

            //Debug message
            debug("Initiating POST request at " + url + " with payload:", payload);

            //Perform request
            return $.ajax({
                type: "POST",
                url: url,
                data: JSON.stringify(payload),
                dataType: "json",
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
         * Performs a PUT request with a given payload and returns the resulting promise.
         * @param url The URl of the request
         * @param payload The payload of the PUT request
         * @returns {*|void} The resulting promise
         */
        function putRequest(url, payload) {
            //Sanitize payload
            payload = payload || {};

            //Debug message
            debug("Initiating PUT request at " + url + " with payload:", payload);

            //Perform request
            return $.ajax({
                type: "PUT",
                url: url,
                data: JSON.stringify(payload),
                dataType: "json",
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
         * Performs a DELETE request without payload and returns the resulting promise.
         * @param url The URl of the request
         * @returns {*|void} The resulting promise
         */
        function deleteRequest(url) {
            //Debug message
            debug("Initiating DELETE request at " + url);

            //Perform request
            return $.ajax({
                type: "DELETE",
                url: url,
                dataType: "json",
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
            /**
             * Extracts an error message from a given response object.
             * @param response To response object to extract the message from
             * @return The extracted error message or null in case there is none
             */
            function extractMessage(response) {
                //Sanity check for object
                if (typeof response !== 'object') {
                    return null;
                }

                //Check for message field
                if ((response.hasOwnProperty('message'))
                    && ((typeof response.message === 'string') || (response.message instanceof String))) {
                    return response.message;
                }

                //Check for responseJSON
                if ((response.hasOwnProperty('responseJSON')) &&
                    (typeof response.responseJSON.message === 'string') || (response.responseJSON.message instanceof String)) {
                    return response.responseJSON.message;
                }

                return null;
            }

            //Debug messages
            debug("Request failed, response:", response);

            //Extract error message if available
            let errorMessage = extractMessage(response);

            //Check if response is available
            if (errorMessage != null) {
                NotificationService.showError(errorMessage);
            } else if (response.status === 0) {
                NotificationService.showError("Request to backend failed. Is it online?");
            } else {
                NotificationService.showError("Request was not successful.");
            }

            return response;
        }

        /**
         * [Private]
         * Prints debug messages to the console, if debug mode is enabled.
         */
        function debug() {
            //Check if debugging is desired
            if (!debugMode) {
                return;
            }

            //Iterate over all arguments and log them
            for (let i = 0; i < arguments.length; i++) {
                console.log(arguments[i]);
            }
        }

        /**
         * [Public]
         * Retrieves one entity of a certain id from a given category and returns the resulting promise.
         * @param category The category of the entity
         * @param id The ID of the entity to retrieve
         * @returns {*} The resulting promise
         */
        function getOne(category, id) {
            //Perform GET request
            return getRequest(ENDPOINT_URI + "/" + category + "/" + id).then(function (data) {
                //Sanitize and return entity
                return data || {};
            });
        }

        /**
         * [Public]
         * Retrieves all entities for a given category and returns the resulting promise.
         * @param category The category to retrieve the entities for
         * @returns {*} The resulting promise
         */
        function getAll(category) {
            //Perform GET request
            return getRequest(ENDPOINT_URI + "/" + category).then(function (data) {
                //Extend received object for empty list if none available
                data._embedded = data._embedded || {};

                //Iterate over all properties of the embedded objects
                for (let key in data._embedded) {
                    //Check property
                    if (!data._embedded.hasOwnProperty(key)) {
                        continue;
                    }

                    //Compare key to category
                    if (key.replaceAll('-', '').toLowerCase() === category.replaceAll('-', '').toLowerCase()) {
                        //Key matches to category, return list
                        return data._embedded[key] || [];
                    }
                }

                //No matching key found
                return [];
            });
        }

        /**
         * [Public]
         * Retrieves the number of entities for a given category and returns the resulting promise.
         * @param category The category to count the entities for
         * @returns {*} The resulting promise
         */
        function count(category) {
            //Perform GET request for counting the objects
            return getRequest(ENDPOINT_URI + "/" + category).then(function (data) {
                if ((typeof data !== 'object') || (typeof data.page !== 'object')) {
                    return 0;
                }
                return data.page.totalElements || 0;
            });
        }

        /**
         * [Public]
         * Adds one entity of a certain category with specific data and returns the resulting promise.
         * @param category The category of the entity
         * @param data The data of the entity to add
         * @returns {*} The resulting promise
         */
        function addOne(category, data) {
            //Sanitize data
            if (typeof data === 'undefined') {
                data = {};
            }

            //Perform POST request
            return postRequest(ENDPOINT_URI + "/" + category, data).then(function (data) {
                //Sanitize and return entity
                return data || {};
            });
        }

        /**
         * [Public]
         * Updates one entity of a certain category with specific data and returns the resulting promise.
         * @param category The category of the entity to update
         * @param data The data of the entity to update
         * @returns {*} The resulting promise
         */
        function updateOne(category, data) {
            //Sanitize data
            if (typeof data === 'undefined') {
                data = {};
            }

            //Perform PUT request
            return putRequest(ENDPOINT_URI + "/" + category + "/" + data.id).then(function (data) {
                //Sanitize and return entity
                return data || {};
            });
        }

        /**
         * [Public]
         * Deletes one entity of a certain id from a given category and returns the resulting promise.
         * @param category The category of the entity
         * @param id The ID of the entity to delete
         * @returns {*} The resulting promise
         */
        function deleteOne(category, id) {
            //Perform DELETE request
            return deleteRequest(ENDPOINT_URI + "/" + category + "/" + id).then(function (data) {
                return id;
            });
        }

        //Expose public functions
        return {
            getRequest: getRequest,
            postRequest: postRequest,
            putRequest: putRequest,
            deleteRequest: deleteRequest,
            getOne: getOne,
            getAll: getAll,
            count: count,
            addOne: addOne,
            updateOne: updateOne,
            deleteOne: deleteOne
        };
    }])
;
