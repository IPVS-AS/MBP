/* global app */

/**
 * Provides services for managing tests.
 */
app.factory('TestService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_TEST_START = ENDPOINT_URI + '/test-details/test/';
        const URL_TEST_STOP = ENDPOINT_URI + 'test-details/test/stop/';
        const URL_REPORT_LIST = ENDPOINT_URI + '/test-details/pdfList/';

        /**
         * [Public]
         * Performs a server request in order to execute a test given by its id.
         * @param testId The id of the test to be executed
         * @returns {*}
         */
        function executeTest(testId) {
            return $http.post(URL_TEST_START + testId);
        }

        /**
         * [Public]
         * Performs a server request in order to stop a test and its components given by its id.
         * @param testId The id of the test to be stopped
         * @returns {*}
         */
        function stopTest(testId) {
            return $http.post(URL_TEST_STOP + testId)

        }


        /**
         * [Public]
         * Performs a server request to get a list of all generated Test Reports regarding to a test given by its id.
         */
        function getPDFList(testId) {
            return $http.get(URL_REPORT_LIST + testId).then(function (response) {
                const pdfList = {};
                let pdfDetails = [];
                let responseArray = [];

                if (Object.keys(response.data).length > 0) {
                    angular.forEach(response.data, function (value, key) {
                        pdfDetails.push({
                            "date": key,
                            "path": value
                        });
                    });
                    pdfList.pdfTable = pdfDetails;
                    return pdfList.pdfTable;
                } else {
                    document.getElementById("pdfTable").innerHTML = "There is no Test Report for this Test yet.";
                }
            });
        }


        /**
         * [Public]
         *
         * Sends a server request in order to edit the configurations of the test "useNewData",
         * so that the latest values of a specific test are reused in the new execution or not
         *
         * @param testId The id of the test the config should be edited
         * @param useNewData boolean if the test should generate new data
         * @returns {*}
         */
        function editConfig(testId, useNewData) {
            if (useNewData === true) {
                return $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "false").success(function success(response) {
                    return response.success; // Update the list of sensors included in the test for the Chart view
                });
            } else if (useNewData === false) {
                return $http.post(ENDPOINT_URI + '/test-details/editConfig/' + testId, "true").success(function success(response) {
                    return response.success; // Update the list of sensors included in the test for the Chart view
                });
            }

        }

        //Expose public methods
        return {
            executeTest: executeTest,
            stopTest: stopTest,
            getPDFList: getPDFList,
            editConfig: editConfig
        }
    }
]);

