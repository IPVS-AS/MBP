/* global app */

/**
 * Provides services related to the discovery of devices.
 */
app.factory('DiscoveryService', ['ENDPOINT_URI', 'HttpService', function (ENDPOINT_URI, HttpService) {
    //Default URI for discovery
    const URI_DISCOVERY = ENDPOINT_URI + '/discovery/';

    //Base URL for managing location templates
    const URL_LOCATION_TEMPLATES = URI_DISCOVERY + '/location-templates/';


    //Expose public functions
    return {};
}]);
