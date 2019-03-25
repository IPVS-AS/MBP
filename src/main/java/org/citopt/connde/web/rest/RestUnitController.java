package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.units.PredefinedQuantities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;

/**
 * Rest controller for requests related to units.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestUnitController {

    /**
     * Replies with a list of predefined units, wrapped in objects of predefined quantities that might
     * be used as unit suggestions.
     *
     * @return A response containing a list of predefined quantities, each containing a list of units.
     */
    @GetMapping(value = "/units")
    public ResponseEntity<List<PredefinedQuantities>> getSupportedQuantities() {
        //Get all enum objects as list
        List<PredefinedQuantities> quantitiesList = Arrays.asList(PredefinedQuantities.values());

        return new ResponseEntity<>(quantitiesList, HttpStatus.OK);
    }

    /**
     * Replies with a list of predefined units, wrapped in objects of predefined quantities that are
     * compatible with a given unit.
     *
     * @param compatibleUnit A string specifying the unit for which other compatible units are supposed
     *                       to be retrieved
     * @return A response containing a list of predefined quantities, each containing a list of units or a BAD REQUEST
     * reply in case the string does not represent a valid unit
     */
    @GetMapping(value = "/units", params = {"compatible"})
    public ResponseEntity<List<PredefinedQuantities>> getSupportedCompatibleQuantities(
            @RequestParam("compatible") String compatibleUnit) {
        try {
            //Try to get compatible quantities
            List<PredefinedQuantities> quantitiesList = PredefinedQuantities.getCompatibleQuantities(compatibleUnit);

            return new ResponseEntity<>(quantitiesList, HttpStatus.OK);
        } catch (Exception e) {
            //Catch parsing exceptions
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Replies a boolean value that states if two given units are compatible to each other and
     * thus allow the conversion of values into each other.
     *
     * @param firstUnitString  A string specifying the first unit
     * @param secondUnitString A string specifying the second unit
     * @return True, if the units are compatible; false otherwise. In case at least one of the strings
     * does not represent a valid unit, a BAD REQUEST is replied.
     */
    @GetMapping(value = "/units", params = {"first", "second"})
    public ResponseEntity<Boolean> checkUnitsForCompatibility(@RequestParam("first") String firstUnitString,
                                                              @RequestParam("second") String secondUnitString) {
        //Objects to hold the parsed units
        Unit firstUnit, secondUnit;

        //Try to convert both strings to units
        try {
            firstUnit = Unit.valueOf(firstUnitString);
            secondUnit = Unit.valueOf(secondUnitString);
        } catch (Exception e) {
            //Catch parsing exceptions
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Check compatibility
        boolean compatible = firstUnit.isCompatible(secondUnit);

        return new ResponseEntity<>(compatible, HttpStatus.OK);
    }
}
