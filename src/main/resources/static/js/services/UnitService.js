'use strict';

/*
 * Provides services for executing unit-related server requests.
 */
app.factory('UnitService', ['HttpService', 'ENDPOINT_URI', function (HttpService, ENDPOINT_URI) {

    //URL at which predefined quantities and their units may be requested
    const URL_UNITS = ENDPOINT_URI + '/units';
    //URL under which units may be requested that are compatible to a given unit
    const URL_UNITS_COMPATIBLE = ENDPOINT_URI + '/units/compatible';
    //URL under which two units may be checked for compatibility
    const URL_UNITS_COMPATIBLE_CHECK = ENDPOINT_URI + '/units/check';

    /**
     * Performs a server request in order to retrieve a list of all predefined units. If a unit string is passed as
     * parameter, only units are retrieved which are compatible to the given one.
     *
     * @param compatibleUnit A string specifying a unit to which all retrieved units need to be compatible (optional)
     * @returns {*}
     */
    function getPredefinedUnits(compatibleUnit) {
        var parameters = {};

        //URL to use
        let url = URL_UNITS;

        //Check if a unit parameter was provided
        if (compatibleUnit) {
            parameters.compatible = compatibleUnit;
            url = URL_UNITS_COMPATIBLE;
        }

        return HttpService.getRequest(url, parameters);
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

        return HttpService.getRequest(URL_UNITS_COMPATIBLE_CHECK, parameters);
    }


    //Expose
    return {
        getPredefinedUnits: getPredefinedUnits,
        checkUnitsForCompatibility: checkUnitsForCompatibility
    };
}]);