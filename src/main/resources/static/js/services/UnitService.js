'use strict';

/*
 * Provides services for executing unit-related server requests.
 */
app.factory('UnitService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the unit operations are available
    const URL_UNITS = ENDPOINT_URI + '/units';

    /**
     * Performs a server request in order to retrieve a list of all predefined units. If a unit string is passed as
     * parameter, only units are retrieved which are compatible to the given one.
     *
     * @param compatibleUnit A string specifying a unit to which all retrieved units need to be compatible (optional)
     * @returns {*}
     */
    function getPredefinedUnits(compatibleUnit) {
        var parameters = {};

        //Check if a compatible unit was provided
        if (compatibleUnit) {
            parameters.compatible = compatibleUnit;
        }

        return $http.get(URL_UNITS, {params: parameters});
    }

    /**
     * Performs a server request in order to check whether two units - given as strings - are compatible to each
     * other. In this case, true is returned by the server; false otherwise
     *
     * @param firstUnit A string specifying the first unit
     * @param secondUnit A string specifying the second unit
     * @returns {*}
     */
    function checkUnitsForCompatibility(firstUnit, secondUnit) {
        //Create corresponding parameter object
        var parameters = {
            first: firstUnit,
            second: secondUnit
        };

        return $http.get(URL_UNITS, {params: parameters});
    }


    //Expose
    return {
        getPredefinedUnits: getPredefinedUnits,
        checkUnitsForCompatibility: checkUnitsForCompatibility
    };
}]);