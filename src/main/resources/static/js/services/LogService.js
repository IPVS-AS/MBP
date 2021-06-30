/* global app */

/**
 * Provides services for retrieving and deleting logs.
 */
app.factory('LogService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {
    //URLs for server requests
    const URL_EXCEPTION_LOGS = ENDPOINT_URI + '/exception-logs';

    /**
     * [Public]
     *
     * Performs a server request in order to retrieve a page of the currently available exception logs, based on
     * a given page configuration.
     * @param pageSize The desired size of the page to retrieve
     * @param pageNumber The desired number of the page to retrieve
     * @param sortOrder The desired sorting order within the page as string
     * @returns @returns {*|void} The promise of the resulting request
     */
    function getExceptionLogs(pageSize, pageNumber, sortOrder) {
        return HttpService.getRequest(URL_EXCEPTION_LOGS, {
            'size': pageSize,
            'page': pageNumber,
            'sort': sortOrder
        });
    }

    /**
     * [Public]
     *
     * Performs a server request in order to delete all currently available exception logs.
     *
     * @returns @returns {*|void} The promise of the resulting request
     */
    function deleteExceptionLogs() {
        return HttpService.deleteRequest(URL_EXCEPTION_LOGS);
    }

    //Expose public methods
    return {
        getExceptionLogs: getExceptionLogs,
        deleteExceptionLogs: deleteExceptionLogs
    }
}
]);

