'use strict';

/*
 * This service retrieves a list of all available predefined units.
 */
app.factory('UnitService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the list of predefined units is available
    const URL_GET_PREDEFINED_UNITS = ENDPOINT_URI + '/units';

    //Performs a server request in order to retrieve a list of all predefined units.
    function getPredefinedUnits() {
        return $http.get(URL_GET_PREDEFINED_UNITS);
    }

    //Expose
    return {
        getPredefinedUnits: getPredefinedUnits
    };
}]);