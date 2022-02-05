package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import io.swagger.annotations.*;
import de.ipvs.as.mbp.domain.units.PredefinedQuantity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Arrays;
import java.util.List;

/**
 * Rest controller for requests related to units.
 */
@RestController
@RequestMapping(Constants.BASE_PATH)
@Api(tags = {"Units"}, description = "Provides means for working with physical units")
public class RestUnitController {

    /**
     * Replies with a list of predefined units, wrapped in objects of predefined quantities that might
     * be used as unit suggestions.
     *
     * @return A response containing a list of predefined quantities, each containing a list of units.
     */
    @GetMapping(value = "/units")
    @ApiOperation(value = "Returns a list of supported quantities and the associated units", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<PredefinedQuantity>> getSupportedQuantities() {
        //Get all enum objects as list
        List<PredefinedQuantity> quantitiesList = Arrays.asList(PredefinedQuantity.values());

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
    @GetMapping(value = "/units/compatible", params = {"compatible"})
    @ApiOperation(value = "Returns all predefined units that are compatible with a given unit", notes = "The returned compatible units are wrapped into objects of the quantities to which they belong.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Provided unit could not be parsed")})
    public ResponseEntity<List<PredefinedQuantity>> getSupportedCompatibleQuantities(
            @RequestParam("compatible") @ApiParam(value = "Specifies the unit to search compatible units for", example = "m/s^2", required = true) String compatibleUnit) {
        try {
            //Try to get compatible quantities
            List<PredefinedQuantity> quantitiesList = PredefinedQuantity.getCompatibleQuantities(compatibleUnit);

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
    @GetMapping(value = "/units/check", params = {"first", "second"})
    @ApiOperation(value = "Checks if two given units are compatible to each other", notes = "If both units are compatible, conversion between these is possible.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "The provided units could not be parsed")})
    public ResponseEntity<Boolean> checkUnitsForCompatibility(@RequestParam("first") @ApiParam(value = "Specifies the first unit to compare", example = "m", required = true) String firstUnitString,
                                                              @RequestParam("second") @ApiParam(value = "Specifies the second unit to compare", example = "mm", required = true) String secondUnitString) {
        //Objects to hold the parsed units
        Unit<? extends Quantity> firstUnit, secondUnit;

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
