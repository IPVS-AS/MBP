'use strict';

/*
 * Provides services for executing server requests related to the approval of useres for user entities.
 */
app.factory('UserApprovalService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {
    //URLs for server requests
    const URL_SEARCH_USERS = ENDPOINT_URI + '/users/contain?query=';
    const URL_APPROVE_USER_PREFIX = ENDPOINT_URI + '/';
    const URL_APPROVE_USER_SUFFIX = '/approve';

    /**
     * [Public]
     * Builds the URL that may be used to issue server requests for searching users by username.
     *
     * @param queryString The query string for searching users
     * @returns {*}
     */
    function buildUsersSearchURL(queryString) {
        return URL_SEARCH_USERS + queryString;
    }

    /**
     * [Public]
     * Performs a server request in order to search for all users whose usernames contain a given query string.
     *
     * @param queryString The query string for searching users
     * @returns {*}
     */
    function searchUsers(queryString) {
        return $http.get(buildUsersSearchURL(queryString));
    }

    /**
     * [Public]
     * Performs a server request in order to approve a user (given by its username) for a certain entity
     * (given by its entity id) of a certain category.
     *
     * @param category The category of the entity
     * @param entityId The id of the entity
     * @param username The username of the user to approve
     */
    function approveUser(category, entityId, username) {
        //Build request URL and parameters object
        let url = URL_APPROVE_USER_PREFIX + category + 's/' + entityId + URL_APPROVE_USER_SUFFIX;

        //Perform request
        return $http.post(url, username);
    }

    //Expose
    return {
        buildUsersSearchURL: buildUsersSearchURL,
        searchUsers: searchUsers,
        approveUser: approveUser
    };
}]);