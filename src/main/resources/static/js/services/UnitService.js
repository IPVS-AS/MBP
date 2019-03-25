'use strict';

/*
 * This service retrieves a list of all available predefined units.
 */
app.factory('UnitService', ['$http', 'ENDPOINT_URI', function ($http, ENDPOINT_URI) {

    //URL under which the list of predefined units is available
    const URL_GET_PREDEFINED_UNITS = ENDPOINT_URI + '/units';

    //URL under which a conversion factor for two units is available
    const URL_GET_CONVERSION_FACTOR = ENDPOINT_URI + '/units/factor';

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

        return $http.get(URL_GET_PREDEFINED_UNITS, {params: parameters});
    }

    /**
     * Retrieves a conversion factor with which values of the startUnit may be converted to values of the targetUnit.
     *
     * @param startUnit A string specifying the base unit
     * @param targetUnit A string specifying the target unit to convert to
     * @returns {*}
     */
    function getConversionFactor(startUnit, targetUnit) {
        var parameters = {
            startUnit: startUnit,
            targetUnit: targetUnit
        };

        return $http.get(URL_GET_CONVERSION_FACTOR, {params: parameters});
    }

    //Expose
    return {
        getPredefinedUnits: getPredefinedUnits,
        getConversionFactor: getConversionFactor
    };
}]);